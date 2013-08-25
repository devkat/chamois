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
import net.liftweb.squerylrecord.RecordTypeMode._
import net.liftweb.util.Helpers._

abstract class NodeInfo
case object NoSuchNode extends NodeInfo
case class FullNodeInfo(node: Node) extends NodeInfo

object NodeLoc extends Loc[NodeInfo] {
  
  override def rewrite = Full(inTransaction {
    case RewriteRequest(ParsePath("node" :: path, "", true, false), _, _) => inTransaction {
      val docInfo = Node.findByPath(path) match {
      case Some(node) => FullNodeInfo(node)
      case None => NoSuchNode
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
  case ("show", Full(NoSuchNode)) => {ignore: NodeSeq =>
    Text("Node not found.")}
  case ("show", Full(FullNodeInfo(doc))) =>
    n:NodeSeq => <div/> //Nodes.show(doc) _
  }
  
  override def params = Nil
  
  override def defaultValue = Empty
  
  override def text = new Loc.LinkText[NodeInfo](_ => Nil)
  
  override def link = new Loc.Link[NodeInfo](List("node"), false) {
    override def createLink(info: NodeInfo) =
      info match {
      case FullNodeInfo(node) => Full(Text(calcHref(info)))
      case _ => Empty
    }
  }
  
  override def calcHref(info:NodeInfo) =
    info match {
      case FullNodeInfo(node) => "/node" + node.href
    }
  
  override def name = "node"

}

