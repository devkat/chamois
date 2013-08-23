package org.chamois.model

import java.sql.Timestamp
import org.squeryl.annotations.Column
import net.liftweb.record.Record
import net.liftweb.squerylrecord.KeyedRecord
import net.liftweb.record.field._
import net.liftweb.record.MetaRecord
import net.liftweb.squerylrecord.RecordTypeMode._
import ChamoisDb._
import net.liftweb.common.Full

class Node private() extends Record[Node] with KeyedRecord[Long] {

  override def meta = Node
  
  @Column(name="id")
  override val idField = new LongField(this)
    
  val slug = new StringField(this, 256)
  val created = new DateTimeField(this)
  val position = new IntField(this)
  
  @Column(name="parent_id")
  val parentId = new OptionalLongField(this)
  
  @Column(name="document_uuid")
  val documentUuid = new OptionalStringField(this, 36)
  
  def document = documentUuid.get.map(documents.lookup(_)).flatten
  def parent = parentId.get.map(nodes.lookup(_)).flatten
  
  /*
  def existsChild(n:Node) = exists(from(nodes)(child => where(child.parentId === n.id) select(child.id)))
  def hasChildren = from(nodes)(n => select(existsChild(n))).headOption.get.
  */
  def hasChildren = from(nodes)((child) =>
    where(this.id === child.parentId)
    compute(countDistinct(child.id))
    ).single.measures > 0

  /*
  def parent = parentId.get match {
    case Some(pid) => from(nodes)(n => where(n.id === pid) select(n)).headOption
    case None => None
  }
  */
  
  def children = nodeToChildren.left(this)
  
  def path:List[String] = parent match {
    case Some(p) => p.path ::: slug.get :: Nil
    case None => slug.get :: Nil
  }
  
  def href = "/" + path.mkString("/")
}

object Node extends Node with MetaRecord[Node] {
  
  def unapply(path:List[String]) = findByPath(path)
  
  def rootNodes =
    from(nodes)(n => where(n.parentId isNull) select(n))
    
  def findBySlug(slug:String, parent:Option[Node] = None) =
    parent match {
      case Some(parent) => from(nodes)(n => where(n.slug === slug and n.parentId === parent.id) select(n)).headOption
      case None => from(nodes)(n => where(n.slug === slug and (n.parentId isNull)) select(n)).headOption
    }
  
  def findByPath(path:List[String], parent:Option[Node] = None): Option[Node] =
    path match {
      case slug :: childPath => {
        val node = findBySlug(slug, parent)
        node match {
          case parentOption @ Some(p) => childPath match {
            case Nil => node
            case _ => findByPath(childPath, parentOption)
          }
          case None => None
        }
      }
      case Nil => None
    }
  
}