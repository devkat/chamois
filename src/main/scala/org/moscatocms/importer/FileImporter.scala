package org.moscatocms.importer

import java.io.File
import org.moscatocms.repo.Version
import java.io.FileInputStream

trait FileImporter {

  def importContent(file:File)
  
  def read(f:File) = {
    val in = new FileInputStream(f)
    val arr = new Array[Byte](f.length.toInt)
    in.read(arr)
    in.close()
    arr
  }
  
  
}