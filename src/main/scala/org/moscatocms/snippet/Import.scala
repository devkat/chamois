package org.moscatocms.snippet

import net.liftweb.http.S
import java.io.File
import java.io.FileFilter
import java.io.FilenameFilter
import scala.xml.XML
import org.moscatocms.repo._
import org.moscatocms.repo.MoscatoDb._
import java.util.UUID
import net.liftweb.squerylrecord.RecordTypeMode._
import scala.io.Source
import scala.collection.mutable.Buffer
import java.io.FileInputStream
import org.moscatocms.importer.LenyaImporter
import net.devkat.lift.http.BootstrapScreen
import org.moscatocms.importer.HtmlFileImporter
import net.liftweb.util.FieldError
import net.devkat.lift.http.CssBoundBootstrapScreen
import net.liftweb.http.FieldBinding
import scala.xml.NodeSeq

class Import extends CssBoundBootstrapScreen {
  
  def formName = "import"
  
  def validatePathExists(f:String): List[FieldError] =
    if (new File(f).isDirectory) Nil else FieldError(path, "Not a directory.") :: Nil
  
  val path = field(
      S ? "Path to content", "/Volumes/Data/becompany/website/trunk/lenya/data/content",
      trim,
      valMinLen(1, "Please enter a path."),
      validatePathExists _,
      FieldBinding("path"))
  
  val source = radio(
      S ? "Source",
      "",
      Seq("html", "lenya"),
      valMinLen(1, "Please select a source."),
      FieldBinding("source"))
  
  lazy val importers = Map(
    "html" -> HtmlFileImporter,
    "lenya" -> LenyaImporter
  )
  
  def finish() {
    
    List(resources, versions, folders) foreach {_.deleteWhere(r => 1 === 1)}
    
    val file = new File(path.get)
    if (file.isDirectory()) {
      importers(source).importContent(file)
    }
    else {
      S.notice("Not a directory!")
    }
    
  }

}