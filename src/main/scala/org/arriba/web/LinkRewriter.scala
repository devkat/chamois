package org.arriba.web

import scala.xml.transform.RuleTransformer
import scala.xml.transform.RewriteRule
import scala.xml._

case class Rewriteable(label:String, attr:String) {
  
  def matches(n:Node, hrefMatch: String => Boolean) = n match {
    case e @ Elem(_, l, attrs, _, _*) if l == label => attrs.get(attr) match {
      case Some(s) if hrefMatch(s.text) => true
      case _ => false
    }
    case _ => false
  }
  
  def rewrite(e:Elem, rewriteFunc: String => String) = {
    val attrs = e.attributes
    val otherAttrs = attrs.filter(_.key != attr)
    val href = attrs(attr).text
    val newHref = rewriteFunc(href)
    println("rewriting " + href + " to " + newHref)
    val newAttrs = MetaData.concatenate(otherAttrs, Attribute(attr, Text(newHref), Null))
    e.asInstanceOf[Elem].copy(attributes = newAttrs)
  }
    
}

object Rewriteable {
  implicit def unapply(t: (String, String)) = Rewriteable(t._1, t._2)
}

trait LinkRewriter {
  
  def rewriteLink(href:String): String
  
  def matches(href:String): Boolean
  
  def rewriteables:List[Rewriteable]

  object transformer extends RuleTransformer(new RewriteRule {
    override def transform(n: scala.xml.Node): Seq[scala.xml.Node] = {
      rewriteables.find(_.matches(n, matches _)) match {
        case Some(rewr) => rewr.rewrite(n.asInstanceOf[Elem], rewriteLink _)
        case None => n
      }
    }
  })

  def rewriteLinks(nodes:NodeSeq) = transformer.transform(nodes)

}

trait HtmlLinkRewriter extends LinkRewriter {
  
  def rewriteables:List[Rewriteable] = List(
    ("a", "href"),
    ("link", "href"),
    ("img", "src"),
    ("object", "data"),
    ("form", "action"),
    ("script", "src")
  )

}