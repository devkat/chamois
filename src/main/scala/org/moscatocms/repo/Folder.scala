package org.moscatocms.repo

import java.sql.Timestamp
import org.squeryl.annotations.Column
import net.liftweb.record.Record
import net.liftweb.squerylrecord.KeyedRecord
import net.liftweb.record.field._
import net.liftweb.record.MetaRecord
import net.liftweb.squerylrecord.UuidRecordTypeMode._
import MoscatoDb._
import net.liftweb.json.JsonAST
import net.liftweb.json.JField
import net.liftweb.json.JValue
import net.liftweb.json.JArray
import java.util.UUID
import net.liftweb.util.FieldIdentifier
import net.liftweb.util.FieldError
import net.liftweb.common._

case class Folder private() extends Record[Folder] with KeyedRecord[Long] {

  override def meta = Folder
  
  @Column(name="id")
  override val idField = new LongField(this)
  
  def delete_! = folders.delete(id)
  
  val slug = new StringField(this, 256) {
    def validateUniqueSlug(slug:String): List[FieldError] =
      Folder.findBySlug(slug, parentId.get) match {
        case Some(slug) => FieldError(this, "Slug already exists.") :: Nil
        case None => Nil
      }
    override def validations =
      valMinLen(1, "Slug must not be empty.") _ :: validateUniqueSlug _ :: super.validations
  }
  
  @Column(name="parent_id")
  val parentId = new OptionalLongField(this)
  
  def parent = parentId.get.map(folders.lookup(_)).flatten

  def hasChildren = from(folders)((child) =>
    where(this.id === child.parentId)
    compute(countDistinct(child.id))
    ).single.measures > 0

  def children = folderToChildren.left(this)
  
  def path:Path = parent match {
    case Some(p) => p.path.slugs ::: slug.get :: Nil
    case None => slug.get :: Nil
  }
  
  def href = resource match {
    case None => path.toString
    case Some(resource) => resource.href
  }
  
  def resource = from(resources)(r => where(r.folderId === id) select(r)).headOption

}

object Folder extends Folder with MetaRecord[Folder] {
  
  def unapply(path:List[String]) = findByPath(path)
  
  def rootFolders =
    from(folders)(n => where(n.parentId isNull) select(n))
    
  def findBySlug(slug:String, parentId:Option[Long] = None): Option[Folder] =
    parentId match {
      case Some(pid) => from(folders)(n => where(n.slug === slug and pid === n.parentId) select(n)).headOption
      case None => from(folders)(n => where(n.slug === slug and (n.parentId isNull)) select(n)).headOption
    }
  
  def findByPath(path:Path, parentId:Option[Long] = None): Option[Folder] =
    path.slugs match {
      case slug :: childPath => {
        val parent = findBySlug(slug, parentId)
        parent match {
          case parentOption @ Some(p) => childPath match {
            case Nil => parent
            case _ => findByPath(childPath, parent map {_.id})
          }
          case None => None
        }
      }
      case Nil => None
    }
  
}