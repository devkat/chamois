package org.arriba.sitemap

import net.liftweb.sitemap.Loc
import net.liftweb.common._
import org.arriba.model._
import net.liftweb.http.RewriteRequest
import net.liftweb.http.ParsePath
import org.arriba.model.User
import net.liftweb.http.RewriteResponse
import scala.xml._
import net.liftweb.squerylrecord.RecordTypeMode._
import net.liftweb.util.Helpers._
import org.arriba.snippet.Resources

object ResourceLoc extends ResourceLocBase("resource")
object MercuryLoc extends ResourceLocBase("mercury")

abstract class ResourceInfo
case object NoSuchResource extends ResourceInfo
case class FullResourceInfo(resource: Resource) extends ResourceInfo

class ResourceLocBase(val prefix:String) extends Loc[ResourceInfo] {
  
  override def rewrite = Full(inTransaction {
    case RewriteRequest(ParsePath(p :: path, "", true, false), _, _)
        if p == prefix && path.size > 0 => inTransaction {
      val info = Resource.findByPath(path) match {
        case Some(resource) => FullResourceInfo(resource)
        case None => NoSuchResource
      }
      (RewriteResponse(prefix :: Nil), info)
    }
  })
  
  override def snippets = {
  case ("breadcrumb", Full(FullResourceInfo(r))) => Resources.breadcrumb(r) _
  case ("content", Full(FullResourceInfo(r))) => Resources.content(r) _
  case ("meta", Full(FullResourceInfo(r))) => Resources.meta(r) _
  case ("versions", Full(FullResourceInfo(r))) => Resources.versions(r) _
  case ("editLink", Full(FullResourceInfo(r))) => Resources.editLink(r) _
  case ("createLink", Full(FullResourceInfo(r))) => Resources.createLink(r) _
  }
  
  override def params = Nil
  
  override def defaultValue = Empty
  
  override def text = new Loc.LinkText[ResourceInfo](_ => Nil)
  
  override def link = new Loc.Link[ResourceInfo](List(prefix), false) {
    override def createLink(info: ResourceInfo) =
      info match {
      case FullResourceInfo(doc) => Full(Text(calcHref(info)))
      case _ => Empty
    }
  }
  
  override def calcHref(info:ResourceInfo) =
    info match {
      case FullResourceInfo(node) => (prefix ++ node.path.slugs).mkString("/")
    }
  
  override def name = prefix

}

