package org.arriba.sitemap

import net.liftweb.sitemap.Loc
import net.liftweb.common._
import org.arriba.record._
import net.liftweb.http.RewriteRequest
import net.liftweb.http.ParsePath
import net.liftweb.http.RewriteResponse
import net.liftweb.squerylrecord.RecordTypeMode._
import net.liftweb.util.Helpers._
import org.arriba.snippet.Resources
import net.liftweb.http.LiftResponse
import org.arriba.content.MediaType
import org.arriba.http.ResourceResponse._
import java.io.ByteArrayInputStream
import org.arriba.model.Path
import org.arriba.model.Path._
import org.arriba.representation._
import net.liftweb.http.S
import net.liftweb.util.Html5
import scala.xml.Text
import org.arriba.http.ResourceLocator

object ResourceLoc extends Loc[Resource] {
  
  override def rewrite = Full(inTransaction {
    case RewriteRequest(ResourceLocator(r), _, _) =>
      (RewriteResponse("resource" :: Nil), r)
  })
  
  def htmlVersion = currentValue filter { _.mediaType == MediaType("text", "html") } map { _.currentVersion }
  
  override def earlyResponse: Box[LiftResponse] = {
    currentValue flatMap { r => r.mediaType match {
      case MediaType("text", "html") => Empty
      case _ => Full(response(r.currentVersion))
    }}
  }
  
  lazy val representations = Map(
    View.name -> View,
    Mercury.name -> Mercury
  )
  
  override def template: Box[scala.xml.Node] = htmlVersion flatMap { v =>
    Html5.parse(new ByteArrayInputStream(v.content.get)) map { html =>
      representations(S.param("arriba-rep")).template(html).head
    }
  }
  
  override def snippets = {
  case ("breadcrumb", Full(r)) => Resources.breadcrumb(r.path) _
  case ("content", Full(r)) => Resources.content(r) _
  case ("meta", Full(r)) => Resources.meta(r) _
  case ("versions", Full(r)) => Resources.versions(r) _
  case ("editLink", Full(r)) => Resources.editLink(r) _
  case ("createLink", Full(r)) => Resources.createLink(Some(r)) _
  }
  
  override def params = Nil
  
  override def defaultValue = Empty
  
  override def text = new Loc.LinkText[Resource](_ => Nil)
  
  override def link = new Loc.Link[Resource](List("resource"), false) {
    override def createLink(r: Resource) = Full(Text(calcHref(r)))
  }
  
  override def calcHref(r:Resource) = r.href
  
  override def name = "resource"

}

