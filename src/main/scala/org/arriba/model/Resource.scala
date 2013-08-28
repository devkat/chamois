package org.arriba.model

import java.sql.Timestamp
import org.squeryl.annotations.Column
import net.liftweb.record.Record
import net.liftweb.squerylrecord.KeyedRecord
import net.liftweb.record.field._
import net.liftweb.record.MetaRecord
import net.liftweb.squerylrecord.UuidRecordTypeMode._
import ArribaDb._
import net.liftweb.json.JsonAST
import net.liftweb.json.JField
import net.liftweb.json.JValue
import net.liftweb.json.JArray
import org.arriba.util.Path
import java.util.UUID
import net.liftweb.util.FieldIdentifier
import net.liftweb.util.FieldError
import org.arriba.content.MediaType

case class Resource private() extends Record[Resource] with KeyedRecord[Long] {

  override def meta = Resource
  
  @Column(name="id")
  override val idField = new LongField(this)
  
  val uuid = new UuidField(this)
    
  def unapply(uuid:UUID): Option[Resource] =
    Resource.findByUuid(uuid)
  
  def unapply(json: JValue): Option[Resource] =
    Resource.fromJValue(json)
    
  def versions = resourceToVersions.left(this)
  
  def numVersions:Long =
    from(versions)(v => compute(count))
    //from(versions)(v => where(v.uuid === uuid) compute(max(v.number))):Option[Int]

  def currentVersion =
    Version.findLatestVersion(id)
    
  def delete_! = resources.delete(id)
  
  def newVersion(mediaType: MediaType, content: Array[Byte]) = {
    val version = Version.newVersion(id)
    version.setMediaType(mediaType)
    version.content.set(content)
    ArribaDb.versions.insert(version)
    version
  }

  val name = new StringField(this, 256)

  val slug = new StringField(this, 256) {
    def validateUniqueSlug(slug:String): List[FieldError] =
      Resource.findBySlug(slug, parent) match {
        case Some(slug) => FieldError(this, "Slug already exists.") :: Nil
        case None => Nil
      }
    override def validations =
      valMinLen(1, "Slug must not be empty.") _ :: validateUniqueSlug _ :: super.validations
  }
  
  @Column(name="parent_id")
  val parentId = new OptionalLongField(this)
  
  def parent = parentId.get.map(resources.lookup(_)).flatten

  def hasChildren = from(resources)((child) =>
    where(this.id === child.parentId)
    compute(countDistinct(child.id))
    ).single.measures > 0

  def children = resourceToChildren.left(this)
  
  def path:Path = parent match {
    case Some(p) => p.path.slugs ::: slug.get :: Nil
    case None => slug.get :: Nil
  }
  
  def href = path.toString

}

object Resource extends Resource with MetaRecord[Resource] {
  
  def findByUuid(uuid:UUID): Option[Resource] =
    from(resources)(d => where(d.uuid === uuid) select(d)).headOption

  def findAll: List[Resource] =
    from(resources)(d => select(d)).toList

  protected def encodeAsJSON_! (toEncode: Resource): JsonAST.JObject = {
    toEncode.runSafe {
      JsonAST.JObject(
        List(JField("uuid", toEncode.uuid.asJValue))
      )
    }
  }

  implicit def asJson(doc:Resource): JValue =
    //= encodeAsJSON_!(doc)
    doc.asJValue

  implicit def asJson(docs:List[Resource]): JValue =
    JArray(docs map asJson)
  
  def unapply(path:List[String]) = findByPath(path)
  
  def rootResources =
    from(resources)(n => where(n.parentId isNull) select(n))
    
  def findBySlug(slug:String, parent:Option[Resource] = None): Option[Resource] =
    parent match {
      case Some(parent) => from(resources)(n => where(n.slug === slug and parent.id === n.parentId) select(n)).headOption
      case None => from(resources)(n => where(n.slug === slug and (n.parentId isNull)) select(n)).headOption
    }
  
  def findByPath(path:Path, parent:Option[Resource] = None): Option[Resource] =
    path.slugs match {
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