package org.chamois.snippet

import org.chamois.model.{Document, Version}
import scala.xml.NodeSeq
import org.chamois.model.User
import net.liftweb.common.Full
import scala.xml.XML
import java.io.ByteArrayInputStream

object Documents {
  
  def show(doc:Document)(n:NodeSeq): NodeSeq = {
    <h2>{doc.name}</h2>
  }
  
  def listDocuments(n:NodeSeq): NodeSeq =
    <ul>
      {Document.findAll.map(doc =>
        <li><a href={"/document/" + doc.uuid}>{doc.name}</a></li>)}
    </ul>
  
  def withCurrentVersion(doc:Document)(f: Version => NodeSeq) =
    doc.currentVersion match {
      case Some(version) => f(version)
      case None => <p>No version.</p>
    }
  
  def xmlContent(version:Version) =
    version.xmlContent \ "body" \ "_"
  
  def content(doc:Document)(n:NodeSeq): NodeSeq = withCurrentVersion(doc) { version =>
    version.mediaType.get.split("/").toList match {
      case "image" :: subtype => <img src=""/>
      case "application" :: "xhtml+xml" :: Nil => xmlContent(version)
      case _ => <p>Cannot display content for this media type.</p>
    }
  }

  def meta(doc:Document)(n:NodeSeq): NodeSeq = withCurrentVersion(doc) { version =>
    <div class="form-group">
      <label for="mediaType">UUID</label>
      <span class="form-control">{doc.uuid.get}</span>
    </div>
    <div class="form-group">
      <label for="mediaType">Media type</label>
      <span class="form-control">{version.mediaType.get}</span>
    </div>
  }
  
  def versions(doc:Document)(n:NodeSeq): NodeSeq =
    <table class="table">
      {
        doc.versions map { v =>
          <tr><td>{v.number}</td></tr>
        }
      }
    </table>
  
  def editLink(doc:Document)(n:NodeSeq): NodeSeq =
    <a class="btn btn-default" href={"/mercury/" + doc.node.path.mkString("/")}>{n}</a>
}