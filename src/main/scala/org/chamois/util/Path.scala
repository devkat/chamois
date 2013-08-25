package org.chamois.util

case class Path(val slugs:List[String]) {
  
  override def toString = "/" + slugs.mkString("/")
  
  def startsWith(p:Path) = slugs.startsWith(p.slugs)
  
}

object Path {
  
  val root = Path(Nil)
  
  implicit def unapply(l:List[String]) = Path(l)
  implicit def unapply(s:String): Path = """\/""".r.split(s).toList
  
}