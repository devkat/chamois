package org.chamois.snippet

import org.chamois.model.Node
import scala.xml.NodeSeq
import org.chamois.model.User
import net.liftweb.common.Full
import org.chamois.model.Node
import org.chamois.model.ChamoisDb._
import net.liftweb.http.S
import net.liftweb.squerylrecord.RecordTypeMode._
import org.chamois.util.Path

object Nodes {
  
  def children(node:Node) =
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
  
  def breadcrumb(node:Node)(n:NodeSeq) = {
    def steps(p:Path, parentPath:String = "/document"): NodeSeq = p.slugs match {
      case head :: Nil => <li class="active">{head}</li>
      case head :: tail => {
        val path = parentPath + "/" + head
        <li><a href={path}>{head}</a></li> ++ {steps(tail, path)}
      }
      case Nil => Nil
    }
    <ul class="breadcrumb">{n \ "_"}{steps(node.path)}</ul>
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