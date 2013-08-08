package org.chamois.sitemap

import net.liftweb.sitemap.Loc
import net.liftweb.common._
import org.chamois.model.Document
import org.chamois.model.Node
import net.liftweb.http.RewriteRequest
import net.liftweb.http.ParsePath
import org.chamois.model.User
import net.liftweb.http.RewriteResponse
import scala.xml._
import org.chamois.snippet.Documents
import net.liftweb.squerylrecord.RecordTypeMode._
import net.liftweb.util.Helpers._
import org.chamois.snippet.Nodes

abstract class DocumentInfo
case object NoSuchDocument extends DocumentInfo
case class NodeOnly(node: Node) extends DocumentInfo
case class FullDocumentInfo(doc: Document) extends DocumentInfo

object DocumentLoc extends Loc[DocumentInfo] {
  
  override def rewrite = Full(inTransaction {
    case RewriteRequest(ParsePath("document" :: path, "", true, false), _, _) if path.size > 0 => inTransaction {
      val node = Node.findByPath(path).get
      //println("Node: " + node.path + ", " + node.documentUuid + ", " + node.document)
      val docInfo = Node.findByPath(path) match {
        case Some(node) => node.document match {
          case Some(document) => FullDocumentInfo(document)
          case None => NodeOnly(node)
        }
        case None => NoSuchDocument
      }
      (RewriteResponse("document" :: Nil), docInfo)
    }
    /*
    case RewriteRequest(ParsePath(List("set", Repository(Repository)), "", true, false), _, _) => {
         if (Repository.isPublic.is || Repository.ownerId == User.currentUserId)
             (RewriteResponse("set" :: Nil), FullRepositoryInfo(Repository))
           else
             (RewriteResponse("set" :: Nil), NoSuchRepository)
    }
       */
  })
  
  override def snippets = {
  case ("show", Full(NoSuchDocument)) => {ignore: NodeSeq =>
    Text("Document not found.")}
  case ("show", Full(FullDocumentInfo(doc))) =>
    Documents.show(doc) _
  case ("tree", Full(FullDocumentInfo(doc))) => Nodes.tree(doc.node)
  case ("tree", Full(NodeOnly(node))) => Nodes.tree(node)
  case ("content", Full(FullDocumentInfo(doc))) => Documents.content(doc)
  case ("meta", Full(FullDocumentInfo(doc))) => Documents.meta(doc)
  case ("versions", Full(FullDocumentInfo(doc))) => Documents.versions(doc)
  }
  
  override def params = Nil
  
  override def defaultValue = Empty
  
  override def text = new Loc.LinkText[DocumentInfo](_ => Nil)
  
  override def link = new Loc.Link[DocumentInfo](List("document"), false) {
    override def createLink(info: DocumentInfo) =
      info match {
      case FullDocumentInfo(doc) => Full(Text(calcHref(info)))
      case _ => Empty
    }
  }
  
  override def calcHref(info:DocumentInfo) =
    info match {
      case FullDocumentInfo(doc) => "/documents/" + doc.uuid
    }
  
  override def name = "document"

}

