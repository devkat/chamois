package org.chamois.snippet

import net.liftweb.http.S
import java.io.File
import java.io.FileFilter
import java.io.FilenameFilter
import scala.xml.XML
import org.chamois.model._
import org.chamois.model.ChamoisDb._
import java.util.UUID
import net.liftweb.squerylrecord.RecordTypeMode._
import scala.io.Source
import scala.collection.mutable.Buffer
import java.io.FileInputStream

class ImportLenya extends BootstrapScreen {
  
  val path = field(S ? "Path to content", "/Volumes/Data/temp/sobu/lenya/content", trim, valMinLen(1, "Please enter a path."))
  
  def importArea(area:File) {
    val site = XML.loadFile(new File(area, "sitetree.xml"))
    
    List("en", "de", "fr", "it") foreach {lang =>
      val node = Node.createRecord
      node.slug.set(lang)
      nodes.insert(node)
      (site \ "node") foreach (importNode(area, lang, Some(node)))
    }
    
  }
  
  def importNode(area:File, lang:String, parent:Option[Node] = None)(xmlNode:scala.xml.Node) {
      val node = Node.createRecord
      val slug = (xmlNode \ "@id").text
      println("Importing node %s".format(slug))
      /*
      parent match {
        case Some(p) => node.parentId.set(Some(p.id))
        case None => node.parentId.set(None)
      }
      */
      parent foreach {p => node.parentId.set(Some(p.id))}
      node.slug.set(slug)
      nodes.insert(node)
      (xmlNode \ "label") foreach { label =>
        println("@xml:lang: " + (label \ "@{http://www.w3.org/XML/1998/namespace}lang").text)
        if ((label \ "@{http://www.w3.org/XML/1998/namespace}lang").text == lang) {
          val docOption = importDoc(area, (xmlNode \ "@uuid").text, lang)
          docOption foreach (d => node.documentUuid.set(Some(d.uuid.get)))
          nodes.update(node)
        }
      }
      (xmlNode \ "node") foreach importNode(area, lang, Some(node)) _
  }
  
  def importDoc(area:File, uuid:String, lang:String): Option[Document] = {
    println("Importing document %s".format(uuid))
    val docFolder = new File(area, uuid)
    val backupVersions = docFolder.listFiles(new FilenameFilter() {
      override def accept(f:File, name:String) =
        name.matches("""^""" + lang + """\.\d+\.bak$""")
    })
    
    val docVersions = backupVersions.toList ::: new File(docFolder, lang) :: Nil
    
    if (docVersions.length > 0) {
      val newUuid = UUID.randomUUID.toString
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
  
  def importVersion(doc:Document, docFolder:File, uuid:String, lang:String, fileName:String) = {
    val version = Version.newVersion(uuid)
    println("Importing version %d (%s)".format(version.number.get, fileName))
    val contentFile = new File(docFolder, fileName)
    val metaFile = new File(docFolder, lang + ".meta")//fileName.replaceAll("""^""" + lang, lang + ".meta"))
    
    println("Reading meta file " + metaFile.getAbsolutePath)
    val meta = XML.loadFile(metaFile)
    (meta \\ "element") foreach { e =>
      val key = (e \ "@key").text
      val value = (e \ "value").text
      if (key == "mimeType" && version.mediaType.get.length == 0) version.mediaType.set(value)
      println(key + " " + (fileName == lang))
      if (key == "title" && fileName == lang) {
        doc.name.set(value)
        documents.update(doc)
      }
    }
    
    println("Reading content file " + contentFile.getAbsolutePath)
    //val content = Source.fromFile(contentFile).map(_.toByte).toArray
    val in = new FileInputStream(contentFile)
    val bytes = new Array[Byte](contentFile.length.toInt)
    in.read(bytes)
    in.close()
    version.content.set(bytes)
    
    versions.insert(version)
  }
  
  def finish() {
    
    List(nodes, versions, documents) foreach {_.deleteWhere(r => 1 === 1)}
    
    val file = new File(path.get)
    if (file.isDirectory()) {
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
    else {
      S.notice("Not a directory!")
    }
    
  }

}