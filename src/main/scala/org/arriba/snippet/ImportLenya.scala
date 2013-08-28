package org.arriba.snippet

import net.liftweb.http.S
import java.io.File
import java.io.FileFilter
import java.io.FilenameFilter
import scala.xml.XML
import org.arriba.model._
import org.arriba.model.ArribaDb._
import java.util.UUID
import net.liftweb.squerylrecord.RecordTypeMode._
import scala.io.Source
import scala.collection.mutable.Buffer
import java.io.FileInputStream
import org.arriba.importer.LenyaImporter

class ImportLenya extends BootstrapScreen {
  
  val path = field(S ? "Path to content", "/Volumes/Data/temp/sobu/lenya/content", trim, valMinLen(1, "Please enter a path."))
  
  def finish() {
    
    List(versions, resources) foreach {_.deleteWhere(r => 1 === 1)}
    
    val file = new File(path.get)
    if (file.isDirectory()) {
      LenyaImporter.importContent(file)
    }
    else {
      S.notice("Not a directory!")
    }
    
  }

}