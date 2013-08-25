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
import org.chamois.web.HtmlLinkRewriter

case class Doc(val uuid: UUID, val lang: String)
  
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
      val res = Resource.createRecord
      res.slug.set(lang)
      resources.insert(res)
      (site \ "node") foreach (importNode(area, lang, Some(res)))
    }
  }
  
  def getDocs(area:File)(xmlNode:scala.xml.Node): Set[Doc] = {
    val docs = (xmlNode \ "@uuid") match {
      case NodeSeq.Empty => Set.empty[Doc]
      case n => {
        val uuid = UUID.fromString(n.text)
        (xmlNode \ "label").toSet.map { l:scala.xml.Node =>
          Doc(uuid, (l \ "@{http://www.w3.org/XML/1998/namespace}lang").text)
        }
      }
    }
    
    val children = (xmlNode \ "node").toSet.flatMap(getDocs(area) _)
    docs ++ children
  }
  
  def matchesLang(label:scala.xml.Node, lang:String) =
    (label \ "@{http://www.w3.org/XML/1998/namespace}lang").text == lang
  
  def importNode(area:File, lang:String, parent:Option[Resource] = None)(xmlNode:scala.xml.Node)(implicit doc2uuid:Map[Doc, UUID]) {
    val slug = (xmlNode \ "@id").text
    println("Importing node %s".format(slug))
    
    val descendants = (xmlNode \\ "label").find(matchesLang(_, lang))
    
    if (descendants.isDefined) {
      val uuid = UUID.fromString((xmlNode \ "@uuid").text)
      val newUuid = doc2uuid(Doc(uuid, lang))
      val res = Resource.createRecord
      res.uuid.set(newUuid)
      res.slug.set(slug)
      parent foreach {p => res.parentId.set(Some(p.id))}
      resources.insert(res)

      (xmlNode \ "label") foreach { label =>
        if ((label \ "@{http://www.w3.org/XML/1998/namespace}lang").text == lang) {
          importDoc(res, area, uuid, lang)
        }
      }
      (xmlNode \ "node") foreach importNode(area, lang, Some(res)) _
    }
  }
  
  def importDoc(res:Resource, area:File, uuid:UUID, lang:String)(implicit doc2uuid:Map[Doc, UUID]): Option[Resource] = {
    println("  Importing resource %s [%s]".format(uuid, lang))
    val docFolder = new File(area, uuid.toString)
    val backupVersions = docFolder.listFiles(new FilenameFilter() {
      override def accept(f:File, name:String) =
        name.matches("""^""" + lang + """\.\d+\.bak$""")
    })
    
    val docVersions = backupVersions.toList ::: new File(docFolder, lang) :: Nil
    
    if (docVersions.length > 0) {
      docVersions sortBy(_.getName) foreach { v =>
        importVersion(res, docFolder, lang, v.getName)
      }
      Some(res)
    }
    else {
      None
    }
  }
  
  def importVersion(doc:Resource, docFolder:File, lang:String, fileName:String)(implicit doc2uuid:Map[Doc, UUID]) = {
    val contentFile = new File(docFolder, fileName)
    val metaFile = new File(docFolder, lang + ".meta")//fileName.replaceAll("""^""" + lang, lang + ".meta"))
    
    var mediaType:String = null
    
    println("      Reading meta file " + metaFile.getAbsolutePath)
    val meta = XML.loadFile(metaFile)
    (meta \\ "element") foreach { e =>
      val key = (e \ "@key").text
      val value = (e \ "value").text
      if (key == "mimeType") mediaType = value
    }
    
    println("      Reading content file " + contentFile.getAbsolutePath + ", media type " + mediaType)
    //val content = Source.fromFile(contentFile).map(_.toByte).toArray
    
    def newVersion(init: Version => Unit) {
      val version = Version.newVersion(doc.id)
      version.mediaTypeString.set(mediaType)
      println("    Importing version %d (%s)".format(version.number.get, fileName))
      init(version)
      versions.insert(version)
    }
    
    MediaType.parse(mediaType) match {
      case Some(MediaType("application", "xhtml+xml")) => newVersion { v =>
        val content = <html lang={lang}>
          <head>
            <title>{doc.uuid.get}</title>
          </head>
          <body>
            {new LinkRewriter(lang).rewriteLinks(XML.loadFile(contentFile) \ "body" \ "_")}
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

class LinkRewriter(val fromLang:String)(implicit doc2uuid:Map[Doc, UUID]) extends HtmlLinkRewriter {
  
  val lenyaDocPrefix = "lenya-document:"
  val urnPrefix = "urn:uuid:"
  val uuidRegex = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}".r
  val langRegex = "lang=([a-z]{2})".r
  
  def resolve(uuid:UUID, lang:String) =
    doc2uuid.getOrElse(Doc(uuid, lang), uuid + ":" + lang + "[unresolved]")
  
  def rewriteLink(href:String) = href match {
    case h if h.startsWith(lenyaDocPrefix) => {
      val s = h.substring(lenyaDocPrefix.length())
      uuidRegex findFirstIn s match {
        case Some(uuid) => {
          urnPrefix + (langRegex findFirstMatchIn s match {
            case Some(matcher) => resolve(UUID.fromString(uuid), matcher.group(1))
            case None => resolve(UUID.fromString(uuid), fromLang)
          })
        }
        case None => h + "[invalid]"
      }
    }
    case h => h
  }
  
  def matches(h:String) = h.startsWith(lenyaDocPrefix)
}