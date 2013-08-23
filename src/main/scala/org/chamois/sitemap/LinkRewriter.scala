package org.chamois.sitemap

import scala.xml.NodeSeq
import scala.xml.transform.RuleTransformer
import scala.xml.Elem
import scala.xml.transform.RewriteRule
import org.chamois.model.Document
import scala.xml.Attribute
import scala.xml.Text
import scala.xml.MetaData
import scala.xml.Null

class LinkRewriter {
  
  val urnPrefix = "urn:uuid:"
  val uuidRegex = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}".r
  val langRegex = "lang=([a-z]{2})".r
  
  val cache = scala.collection.mutable.Map.empty[String, String]
  
  def resolve(uuid:String) = {
    cache.getOrElseUpdate(uuid, Document.findByUuid(uuid) match {
      case Some(doc) => "/document/" + doc.node.path.mkString("/")
      case None => urnPrefix + uuid + "[unresolved]"
    })
  }
  
  def rewriteLink(href:String) = href match {
    
    case h if h.startsWith(urnPrefix) => {
      val s = h.substring(urnPrefix.length())
      uuidRegex findFirstIn s match {
        case Some(uuid) => resolve(uuid)
        case None => h + "[invalid]"
      }
    }
    case h => h
  }

  object transformer extends RuleTransformer(new RewriteRule {
    override def transform(n: scala.xml.Node): Seq[scala.xml.Node] = {
      n match {
        case e @ Elem(_, "a", attrs, _, _*) => {
          attrs.get("href") match {
            case Some(Text(href)) if href.startsWith(urnPrefix) => {
              val otherAttrs = attrs.filter(_.key != "href")
              val newHref = rewriteLink(href)
              println("rewriting " + href + " to " + newHref)
              val newAttrs = MetaData.concatenate(otherAttrs, Attribute("href", Text(newHref), Null))
              e.asInstanceOf[Elem].copy(attributes = newAttrs)
            }
            case _ => e
          }
        }
        case n => n
      }
    }
  })

  def rewriteLinks(nodes:NodeSeq) = transformer.transform(nodes)

}