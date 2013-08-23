package org.chamois.web

import scala.xml.NodeSeq
import scala.xml.transform.RuleTransformer
import scala.xml.Elem
import scala.xml.transform.RewriteRule
import org.chamois.model.Document
import scala.xml.Attribute
import scala.xml.Text
import scala.xml.MetaData
import scala.xml.Null

class UuidLinkRewriter extends HtmlLinkRewriter {
  
  val urnPrefix = "urn:uuid:"
  val uuidRegex = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}".r
  val langRegex = "lang=([a-z]{2})".r
  
  val cache = scala.collection.mutable.Map.empty[String, String]
  
  def resolve(uuid:String) = {
    cache.getOrElseUpdate(uuid, Document.findByUuid(uuid) match {
      case Some(doc) => "/api/document" + doc.node.href
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
  
  def matches(href:String) = href.startsWith(urnPrefix)

}