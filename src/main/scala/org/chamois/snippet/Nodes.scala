package org.chamois.snippet

import org.chamois.model.Node
import scala.xml.NodeSeq
import org.chamois.model.User
import net.liftweb.common.Full
import org.chamois.model.Node
import org.chamois.model.ChamoisDb._
import net.liftweb.http.S
import net.liftweb.squerylrecord.RecordTypeMode._

object Nodes {
  
  def children(node:Node, nodes:Iterable[Node]) =
    nodes filter (_.parentId.get match {
      case Some(parentId) if parentId == node.id => true
      case _ => false
    })
  
  def show(node:Node)(n:NodeSeq): NodeSeq = {
    <h2>{node.slug}</h2> ++ (node.children.toList match {
      case Nil => <p>This node has no children.</p>
      case nodes =>
        <ul>
          {nodes.map(node => <li>{node.slug}</li>)}
        </ul>
    })
  }
  
  def treeNodes(allNodes:Iterable[Node], nodes:Iterable[Node], reqPath:List[String], path:List[String] = Nil): NodeSeq =
    if (nodes.isEmpty) Nil else {
      val collapsed = path.isEmpty || reqPath.startsWith(path)
      <ul class={List("tree", if (collapsed) "" else "collapse").mkString(" ")} id={"tree-" + path.mkString("-")}>
        {nodes.map(treeNode(allNodes, _, reqPath, path))}
      </ul>
    }
  
  def treeNode(allNodes:Iterable[Node], node:Node, reqPath:List[String], parentPath:List[String]): NodeSeq = {
    val path = parentPath ::: node.slug.get :: Nil
    val current = path == reqPath
    <li>
      <div class="tree-node">
        <span
          class={"glyphicon glyphicon-" + (if (node.children.isEmpty) "file" else "folder-close")}
          data-toggle="collapse" data-target={"#tree-" + path.mkString("-")}></span>
        {
          if (current) {
            <span>{node.slug}</span>
          }
          else {
            <a href={("/document" :: path).mkString("/")}>{node.slug}</a>
          }
        }
      </div>
      {treeNodes(allNodes, children(node, nodes), reqPath, path)}
    </li>
  }
  
  def allNodes = from(nodes)(n => select(n))
  
  def tree(node:Node)(n:NodeSeq): NodeSeq = {
    treeNodes(allNodes, allNodes filter (_.parentId.get.isEmpty), node.path)
  }
  
  def tree(n:NodeSeq): NodeSeq = {
    treeNodes(allNodes, allNodes filter (_.parentId.get.isEmpty), Nil)
  }
  
}