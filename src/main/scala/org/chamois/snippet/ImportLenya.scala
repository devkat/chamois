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
import org.chamois.importer.LenyaImporter

class ImportLenya extends BootstrapScreen {
  
  val path = field(S ? "Path to content", "/Volumes/Data/temp/sobu/lenya/content", trim, valMinLen(1, "Please enter a path."))
  
  def finish() {
    
    List(nodes, versions, documents) foreach {_.deleteWhere(r => 1 === 1)}
    
    val file = new File(path.get)
    if (file.isDirectory()) {
      LenyaImporter.importContent(file)
    }
    else {
      S.notice("Not a directory!")
    }
    
  }

}