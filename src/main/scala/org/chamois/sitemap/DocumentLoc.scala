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

object DocumentLoc extends DocumentLocBase("document")
object MercuryLoc extends DocumentLocBase("mercury")

class DocumentLocBase(val prefix:String) extends Loc[NodeInfo] {
  
  override def rewrite = Full(inTransaction {
    case RewriteRequest(ParsePath(p :: path, "", true, false), _, _)
        if p == prefix && path.size > 0 => inTransaction {
      val nodeInfo = Node.findByPath(path) match {
        case Some(node) => FullNodeInfo(node)
        case None => NoSuchNode
      }
      (RewriteResponse(prefix :: Nil), nodeInfo)
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
  
  def withDoc(node:Node, f: Document => (NodeSeq => NodeSeq)) = node.document match {
    case Some(doc) => f(doc)
    case None => {ignore: NodeSeq => Text("Node has no document.")}
  }
  
  override def snippets = {
  case ("breadcrumb", Full(FullNodeInfo(node))) => Nodes.breadcrumb(node) _
  case ("tree", Full(FullNodeInfo(node))) => Nodes.tree(node)
  case ("content", Full(FullNodeInfo(node))) => withDoc(node, Documents.content _)
  case ("meta", Full(FullNodeInfo(node))) => withDoc(node, Documents.meta _)
  case ("versions", Full(FullNodeInfo(node))) => withDoc(node, Documents.versions _)
  case ("editLink", Full(FullNodeInfo(node))) => withDoc(node, Documents.editLink _)
  }
  
  override def params = Nil
  
  override def defaultValue = Empty
  
  override def text = new Loc.LinkText[NodeInfo](_ => Nil)
  
  override def link = new Loc.Link[NodeInfo](List(prefix), false) {
    override def createLink(info: NodeInfo) =
      info match {
      case FullNodeInfo(doc) => Full(Text(calcHref(info)))
      case _ => Empty
    }
  }
  
  override def calcHref(info:NodeInfo) =
    info match {
      case FullNodeInfo(node) => (prefix ++ node.path).mkString("/")
    }
  
  override def name = prefix

}

