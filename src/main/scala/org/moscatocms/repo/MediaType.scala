package org.moscatocms.repo

import org.apache.tika.Tika
import org.apache.tika.mime.MimeTypes

case class MediaType(val topType:String, val subType:String) {
  override def toString = topType + "/" + subType
}

object MediaType {
  
  lazy val tika = new Tika
  
  def parse(s:String) = {
    s.split("/").toList match {
      case t1 :: t2 :: Nil => Some(MediaType(t1, t2))
      case _ => None
    }
  }
  
  def byExtension(ext:String):Option[MediaType] = ext match {
    case "" => Some(MediaType("text", "html"))
    case _ => {
      val mt = tika.detect("name." + ext)
      if (mt == null) None else parse(mt)
    }
  }

  def getDefaultExtension(m:MediaType) =
    MimeTypes.getDefaultMimeTypes().forName(m.toString).getExtension()
  
}