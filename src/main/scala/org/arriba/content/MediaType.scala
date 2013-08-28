package org.arriba.content

case class MediaType(val topType:String, val subType:String) {
  override def toString = topType + "/" + subType
}

object MediaType {
  
  def parse(s:String) = {
    s.split("/").toList match {
      case t1 :: t2 :: Nil => Some(MediaType(t1, t2))
      case _ => None
    }
  }
  
  val HTML = MediaType("text", "html")
  val CSS = MediaType("text", "css")
  val PNG = MediaType("image", "png")
  val JPEG = MediaType("image", "jpeg")
  
}