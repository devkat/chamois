package org.chamois.importer

import java.io.File
import scala.xml.XML
import org.chamois.model._
import org.chamois.model.ChamoisDb._
import java.io.FilenameFilter
import java.io.FileInputStream
import java.util.UUID
import scala.xml.NodeSeq
import scala.xml.transform.RuleTransformer
import scala.xml.transform.RewriteRule
import scala.xml.Elem
import scala.xml.MetaData
import scala.xml.Attribute
import scala.xml.Text
import org.chamois.util.MediaType

case class Doc(val uuid: String, val lang: String)
  
object LenyaImporter {
  
  def importContent(file:File) {
  val areas = file.listFiles(new FilenameFilter() {
    override def accept(f:File, name:String) =
      List("authoring", "live").contains(name)
  }).toList

  println("-" * 20)
  areas.foreach({ area =>
    println("Importing area %s".format(area.getName))
    area.getName match {
      case "authoring" => importArea(area)
      //case "live" => importArea(area)
      case _ =>
    }
  })
  println("-" * 20)
  }

  def importArea(area:File) {
    val site = XML.loadFile(new File(area, "sitetree.xml"))
    
    val docs = getDocs(area)(site)
    implicit val doc2uuid = docs.map(( _ -> UUID.randomUUID )).toMap
    //val doc2uuid:Map[Doc, String] =
      // docs.toMap(doc => (doc -> UUID.randomUUID()))
      //docs map { doc => (doc -> UUID.randomUUID()) }
    
    List("en", "de", "fr", "it") foreach {lang =>
      val node = Node.createRecord
      node.slug.set(lang)
      nodes.insert(node)
      (site \ "node") foreach (importNode(area, lang, Some(node)))
    }
  }
  
  def getDocs(area:File)(xmlNode:scala.xml.Node): Set[Doc] = {
    val uuid = (xmlNode \ "@uuid").text
    val docs = (xmlNode \ "label").toSet.map { l:scala.xml.Node =>
      Doc(uuid, (l \ "@{http://www.w3.org/XML/1998/namespace}lang").text)
    }
    
    val children = (xmlNode \ "node").toSet.flatMap(getDocs(area) _)
    docs ++ children
  }
  
  def importNode(area:File, lang:String, parent:Option[Node] = None)(xmlNode:scala.xml.Node)(implicit doc2uuid:Map[Doc, UUID]) {
      val node = Node.createRecord
      val slug = (xmlNode \ "@id").text
      println("Importing node %s".format(slug))
      
      parent foreach {p => node.parentId.set(Some(p.id))}
      node.slug.set(slug)
      nodes.insert(node)
      (xmlNode \ "label") foreach { label =>
        if ((label \ "@{http://www.w3.org/XML/1998/namespace}lang").text == lang) {
          val docOption = importDoc(area, (xmlNode \ "@uuid").text, lang)
          docOption foreach (d => node.documentUuid.set(Some(d.uuid.get)))
          nodes.update(node)
        }
      }
      (xmlNode \ "node") foreach importNode(area, lang, Some(node)) _
  }
  
  def importDoc(area:File, uuid:String, lang:String)(implicit doc2uuid:Map[Doc, UUID]): Option[Document] = {
    println("  Importing document %s [%s]".format(uuid, lang))
    val docFolder = new File(area, uuid)
    val backupVersions = docFolder.listFiles(new FilenameFilter() {
      override def accept(f:File, name:String) =
        name.matches("""^""" + lang + """\.\d+\.bak$""")
    })
    
    val docVersions = backupVersions.toList ::: new File(docFolder, lang) :: Nil
    
    if (docVersions.length > 0) {
      val newUuid = doc2uuid(Doc(uuid, lang)).toString
      val doc = Document.createRecord
      doc.uuid.set(newUuid)
      documents.insert(doc)
      docVersions sortBy(_.getName) foreach { v =>
        importVersion(doc, docFolder, newUuid, lang, v.getName)
      }
      Some(doc)
    }
    else {
      None
    }
  }
  
  def importVersion(doc:Document, docFolder:File, uuid:String, lang:String, fileName:String)(implicit doc2uuid:Map[Doc, UUID]) = {
    val contentFile = new File(docFolder, fileName)
    val metaFile = new File(docFolder, lang + ".meta")//fileName.replaceAll("""^""" + lang, lang + ".meta"))
    
    var mediaType:String = null
    
    println("      Reading meta file " + metaFile.getAbsolutePath)
    val meta = XML.loadFile(metaFile)
    (meta \\ "element") foreach { e =>
      val key = (e \ "@key").text
      val value = (e \ "value").text
      if (key == "mimeType") mediaType = value
      if (key == "title" && fileName == lang) {
        doc.name.set(value)
        documents.update(doc)
      }
    }
    
    println("      Reading content file " + contentFile.getAbsolutePath + ", media type " + mediaType)
    //val content = Source.fromFile(contentFile).map(_.toByte).toArray
    
    def newVersion(init: Version => Unit) {
      val version = Version.newVersion(uuid)
      version.mediaTypeString.set(mediaType)
      println("    Importing version %d (%s)".format(version.number.get, fileName))
      init(version)
      versions.insert(version)
    }
    
    MediaType.parse(mediaType) match {
      case Some(MediaType("application", "xhtml+xml")) => newVersion { v =>
        val content = <html lang={lang}>
          <head>
            <title>{doc.name.get}</title>
          </head>
          <body>
            {LinkRewriter.rewriteLinks(XML.loadFile(contentFile) \ "body" \ "_", lang)}
          </body>
        </html>
        val bytes = content.toString.getBytes("utf-8")
        v.content.set(bytes)
      }
      // don't import old versions of media files
      case Some(mType) if doc.numVersions == 0 => newVersion { v =>
        val in = new FileInputStream(contentFile)
        val arr = new Array[Byte](contentFile.length.toInt)
        in.read(arr)
        in.close()
        v.content.set(arr)
      }
      case _ => {}
    }
    
  }
  
}

object LinkRewriter {
  
  val lenyaDocPrefix = "lenya-document:"
  val urnPrefix = "urn:uuid:"
  val uuidRegex = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}".r
  val langRegex = "lang=([a-z]{2})".r
  
  def resolve(uuid:String, lang:String)(implicit doc2uuid:Map[Doc, UUID]) =
    doc2uuid.getOrElse(Doc(uuid, lang), uuid + ":" + lang + "[unresolved]")
  
  def rewriteLink(href:String, fromLang:String)(implicit doc2uuid:Map[Doc, UUID]) = href match {
    case h if h.startsWith(lenyaDocPrefix) => {
      val s = h.substring(lenyaDocPrefix.length())
      uuidRegex findFirstIn s match {
        case Some(uuid) => {
          urnPrefix + (langRegex findFirstMatchIn s match {
            case Some(matcher) => resolve(uuid, matcher.group(1))
            case None => resolve(uuid, fromLang)
          })
        }
        case None => h + "[invalid]"
      }
    }
    case h => h
  }
  
  def rewriteLinks(nodes:NodeSeq, lang:String)(implicit doc2uuid:Map[Doc, UUID]) = {
    val transformer = new RuleTransformer(new RewriteRule {
      override def transform(n: scala.xml.Node): Seq[scala.xml.Node] = n match {
        case e:Elem if e.label == "a" && (e \ "@href").text.startsWith(lenyaDocPrefix) => {
          val newHref = rewriteLink((e \ "@href").text, lang)
          e.copy(attributes = e.attributes.remove("href").append(Attribute("href", Text(newHref), scala.xml.Null)))
        }
        case n => n
      }
    })
    transformer.transform(nodes)
  }
  
}