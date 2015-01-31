package org.moscatocms.snippet

import org.moscatocms.repo.{Resource, Version, MediaType, Path}
import scala.xml.NodeSeq
import net.liftweb.common.Full
import scala.xml.XML
import java.io.ByteArrayInputStream
import org.moscatocms.web.UuidLinkRewriter
import net.liftweb.common._
import net.liftweb.http.S
import net.liftweb.http.ParsePath
import net.liftweb.http.Req
import org.moscatocms.html.Html
import org.moscatocms.sitemap.ResourceLocator

object Resources {
  
  def show(res:Resource)(n:NodeSeq): NodeSeq = {
    <h2>{res.uuid}</h2>
  }
  
  def listResources(n:NodeSeq): NodeSeq =
    <ul>
      {Resource.findAll.map(res =>
        <li><a href={res.href}>{res.uuid}</a></li>)}
    </ul>
  
  def xmlContent(version:Version) =
    Html.html(version) match {
      case Full(html) => new UuidLinkRewriter().rewriteLinks(html \ "body" \ "_")
      case Failure(msg, ex, _) => errorAsHtml(msg + ex.map(": " + _.toString()).getOrElse(""))
      case Empty => errorAsHtml("Html5.parse() returned Empty, this is not supposed to happen")
    }

    
    //version.xmlContent \ "body" \ "_"
  
  def content(r:Resource)(n:NodeSeq): NodeSeq = xmlContent(r.currentVersion)
  
  def content(n:NodeSeq): NodeSeq = S.param("url") match {
    case Full(url) => Req.parsePath(url) match {
      case ResourceLocator(r) => content(r)(n)
      case _ => <p>Resource not found</p>
    } 
    case _ => <p>Request parameter "url" missing.</p>
  }

  def meta(res:Resource)(n:NodeSeq): NodeSeq =
    <div class="form-group">
      <label for="mediaType">UUID</label>
      <span class="form-control">{res.uuid.get}</span>
    </div> ++
    <div class="form-group">
      <label for="mediaType">Media type</label>
      <span class="form-control">{res.mediaType.toString}</span>
    </div>
  
  def versions(res:Resource)(n:NodeSeq): NodeSeq =
    <table class="table">
      {
        res.versions.zipWithIndex.map { case (v, i) =>
          <tr><td>{i}</td></tr>
        }
      }
    </table>
  
  def editLink(res:Resource)(n:NodeSeq): NodeSeq =
    //<a class="btn btn-default" href={"/edit?path=" + res.href}>{n}</a>
    //<a class="btn btn-default" href={"/moscato/mercury?path=" + res.href}>{n}</a>
    <a class="btn btn-default" href={res.href + "?moscato-rep=mercury"}>{n}</a>
    
  val createLink: NodeSeq => NodeSeq = createLink(None) _
    
  def createLink(parent:Option[Resource])(n:NodeSeq): NodeSeq =
    <a class="btn btn-default" href={"/moscato/create" + parent.map("?parent=" + _.href).getOrElse("")}>{n}</a>

  val breadcrumb: NodeSeq => NodeSeq = breadcrumb(List("", "")) _
  
  def breadcrumb(path:Path)(n:NodeSeq) = {
    def steps(p:Path, parentPath:String = "/document"): NodeSeq = p.slugs match {
      case head :: Nil => <li class="active">{head}</li>
      case head :: tail => {
        val path = parentPath + "/" + head
        <li><a href={path}>{head}</a></li> ++ {steps(tail, path)}
      }
      case Nil => Nil
    }
    <ul class="breadcrumb">{n \ "_"}{steps(path)}</ul>
  }
  
  /*
  def navButton(n:NodeSeq) =
    <li class="dropdown" id="nav-dropdown">
      <button class="btn btn-default" data-toggle="dropdown"><span class="icon icon-sitemap"/> <span class="caret"/></button>
      <ul class="dropdown-menu"></ul>
    </li>
  */
  
  def navButton(n:NodeSeq) = {
    <button class="btn btn-nav dropdown" data-toggle="dropdown"><span class="icon icon-sitemap"/> <span class="caret"/></button>
    <ul class="dropdown-menu"></ul>
  }

  def errorAsHtml(msg:String) =
    <html><body><pre>{msg}</pre></body></html>
}