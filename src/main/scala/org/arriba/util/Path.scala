package org.arriba.util

case class Path(val slugs:List[String]) {
  
  override def toString = "/" + slugs.mkString("/")
  
  def startsWith(p:Path) = slugs.startsWith(p.slugs)
  
  def /(slug:String) = Path(slugs ::: slug :: Nil)
  
  def /(p:Path) = Path(slugs ::: p.slugs)
  
  def parent = slugs match {
    case Nil => Path(Nil)
    case l => Path(l.dropRight(1))
  }
  
}

object Path {
  
  val root = Path(Nil)
  
  implicit def unapply(l:List[String]) = Path(l)
  implicit def unapply(s:String): Path = parse(s)
  
  def parse(s:String) = """\/""".r.split("""^\/""".r.replaceFirstIn(s, "")).toList
  
}