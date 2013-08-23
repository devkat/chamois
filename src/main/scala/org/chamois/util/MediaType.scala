package org.chamois.util

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
  
}