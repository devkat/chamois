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
import net.liftweb.http.LiftResponse
import org.arriba.content.MediaType
import org.arriba.http.ResourceResponse._
import java.io.ByteArrayInputStream
import net.liftweb.util._
import net.liftweb.util.Helpers._
import org.arriba.util.Path
import org.arriba.util.Path._

object ResourceLocator {
  
  implicit def unapply(path:List[String]): Option[Resource] = inTransaction {
    path match {
      case _ => {
        val r = Resource.findByPath(path)
        println("Resolved resource for path " + path + ": " + r)
        r
      }
    }
  }
  
  implicit def unapply(parsePath:ParsePath): Option[Resource] = inTransaction {
    parsePath match {
      case ParsePath(l, ext, true, false) => {
        val path:Path = l
        val suffix = if (ext == "") "" else "." + ext
        val fullPath = path.parent / (path.slugs.last + suffix)
        val r = Resource.findByPath(fullPath)
        println("Resolved resource for path " + fullPath + ": " + r)
        r
      }
    }
  }
  
}

object ResourceLoc extends Loc[Resource] {
  
  override def rewrite = Full(inTransaction {
    case RewriteRequest(ResourceLocator(r), _, _) =>
      (RewriteResponse("resource" :: Nil), r)
  })
  
  def htmlVersion = currentValue flatMap { _.currentVersion } filter { _.mediaType == MediaType("text", "html") }
  
  override def earlyResponse: Box[LiftResponse] = {
    currentValue flatMap { _.currentVersion } flatMap { v => v.mediaType match {
      case MediaType("text", "html") => Empty
      case _ => Full(response(v))
    }}
  }
  
  //lazy val insertDrawer = ("body -*") #> <div data-lift="embed?what=body"/>
  //lazy val insertDrawer = ("body -*") #> <div data-lift="surround?with=body&amp;at=content"/>
  
  lazy val insertDrawer = ("body") #> { n:NodeSeq =>
    <body>
    <div class="drawer" id="arriba-drawer">
      <div data-lift="embed?what=body"/>
      {n \ "_"}
    </div>
    </body>
  }
  
  override def template: Box[Node] = htmlVersion flatMap { v =>
    Html5.parse(new ByteArrayInputStream(v.content.get)) map { html =>
      insertDrawer(html).head
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

