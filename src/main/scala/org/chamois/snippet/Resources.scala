package org.chamois.snippet

import org.chamois.model.{Resource, Version}
import scala.xml.NodeSeq
import org.chamois.model.User
import net.liftweb.common.Full
import scala.xml.XML
import java.io.ByteArrayInputStream
import org.chamois.web.UuidLinkRewriter
import org.chamois.util.MediaType
import org.chamois.util.Path

object Resources {
  
  def show(res:Resource)(n:NodeSeq): NodeSeq = {
    <h2>{res.uuid}</h2>
  }
  
  def listResources(n:NodeSeq): NodeSeq =
    <ul>
      {Resource.findAll.map(res =>
        <li><a href={"/resource/" + res.uuid}>{res.uuid}</a></li>)}
    </ul>
  
  def withCurrentVersion(res:Resource)(f: Version => NodeSeq) =
    res.currentVersion match {
      case Some(version) => f(version)
      case None => <p>No version.</p>
    }
  
  def xmlContent(version:Version) =
    new UuidLinkRewriter().rewriteLinks(version.xmlContent \ "body" \ "_")
    //version.xmlContent \ "body" \ "_"
  
  def content(res:Resource)(n:NodeSeq): NodeSeq = withCurrentVersion(res) { version =>
    version.mediaType match {
      case MediaType("image", _) => <img src={"/api/resource" + res.href}/>
      case MediaType("application", "xhtml+xml") => xmlContent(version)
      case _ => <p>Cannot display content for this media type.</p>
    }
  }

  def meta(res:Resource)(n:NodeSeq): NodeSeq = withCurrentVersion(res) { version =>
    <div class="form-group">
      <label for="mediaType">UUID</label>
      <span class="form-control">{res.uuid.get}</span>
    </div>
    <div class="form-group">
      <label for="mediaType">Media type</label>
      <span class="form-control">{version.mediaType.toString}</span>
    </div>
  }
  
  def versions(res:Resource)(n:NodeSeq): NodeSeq =
    <table class="table">
      {
        res.versions map { v =>
          <tr><td>{v.number}</td></tr>
        }
      }
    </table>
  
  def editLink(res:Resource)(n:NodeSeq): NodeSeq =
    <a class="btn btn-default" href={"/mercury" + res.href}>{n}</a>
    
  def createLink(res:Resource)(n:NodeSeq): NodeSeq =
    <a class="btn btn-default" href={"/create-resource?parent=" + res.href}>{n}</a>

  def breadcrumb(res:Resource)(n:NodeSeq) = {
    def steps(p:Path, parentPath:String = "/document"): NodeSeq = p.slugs match {
      case head :: Nil => <li class="active">{head}</li>
      case head :: tail => {
        val path = parentPath + "/" + head
        <li><a href={path}>{head}</a></li> ++ {steps(tail, path)}
      }
      case Nil => Nil
    }
    <ul class="breadcrumb">{n \ "_"}{steps(res.path)}</ul>
  }
  
  /*
  def navButton(n:NodeSeq) =
    <li class="dropdown" id="nav-dropdown">
      <button class="btn btn-default" data-toggle="dropdown"><span class="icon icon-sitemap"/> <span class="caret"/></button>
      <ul class="dropdown-menu"></ul>
    </li>
  */
  
  def navButton(n:NodeSeq) =
      <button class="btn btn-nav dropdown" data-toggle="dropdown"><span class="icon icon-sitemap"/> <span class="caret"/></button>
      <ul class="dropdown-menu"></ul>

}