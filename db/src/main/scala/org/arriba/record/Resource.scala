package org.arriba.record

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
import org.arriba.model.Path
import java.util.UUID
import net.liftweb.util.FieldIdentifier
import net.liftweb.util.FieldError
import org.arriba.content.MediaType
import net.liftweb.common._

case class Resource private() extends Record[Resource] with KeyedRecord[Long] {

  override def meta = Resource
  
  @Column(name="id")
  override val idField = new LongField(this)
  
  val uuid = new UuidField(this)
    
  def unapply(uuid:UUID): Option[Resource] =
    Resource.findByUuid(uuid)
  
  def unapply(json: JValue): Option[Resource] =
    Resource.fromJValue(json)
  
  @Column(name="current_version_id")
  val currentVersionId = new LongField(this) {
    override def defaultValue = -1
  }
    
  def currentVersion = ArribaDb.versions.lookup(currentVersionId.get).get
    
  def delete_! = resources.delete(id)
  
  def newVersion(content: Array[Byte]) = {
    val version = Version.createRecord
    println("current version id before: " + currentVersionId.get)
    if (currentVersionId.get != currentVersionId.defaultValue)
      version.previousVersionId.set(Some(currentVersionId.get))
    version.content.set(content)
    ArribaDb.versions.insert(version)
    println("Version id:             " + version.idField.get + " - " + version.id)
    currentVersionId.set(version.id)
    println("current version id after:  " + currentVersionId.get)
    version
  }
  
  def versions:List[Version] = versions(Some(currentVersion))
  
  protected def versions(version:Option[Version]):List[Version] =
    version match {
      case Some(v) => v :: versions(v.previousVersionId.get flatMap {ArribaDb.versions.lookup(_)})
      case None => Nil
    }

  val name = new StringField(this, 256)
  
  @Column(name="node_id")
  val nodeId = new LongField(this)
  
  def node = nodes.lookup(nodeId.get).get

  def path = node.path
  
  def href = path.toString + MediaType.getDefaultExtension(mediaType) match {
    case "" => ""
    case ext => "." + ext
  }

  def validateMediaType(f:FieldIdentifier)(s:String):List[FieldError] = {
    MediaType.parse(s) match {
      case Some(t) => Nil
      case None => FieldError(f, "Invalid media type.") :: Nil
    }
  }
  
  @Column(name="media_type")
  val mediaTypeString = new StringField(this, 256) {
    override def validations =
      validateMediaType(this) _ :: super.validations
  }
  
  def mediaType = MediaType.parse(mediaTypeString.get).get
  
  def setMediaType(t:MediaType) {
    mediaTypeString.set(t.toString)
  }

}

object Resource extends Resource with MetaRecord[Resource] {
  
  def findByUuid(uuid:UUID): Option[Resource] =
    from(resources)(d => where(d.uuid === uuid) select(d)).headOption
    
  def findByUrl(path:Path, ext:String) =
    for {
      mediaType <- MediaType.byExtension(ext)
      node <- Node.findByPath(path)
      resource <- from(resources)(r => where(r.nodeId === node.id and r.mediaTypeString === mediaType.toString) select(r)).headOption
    } yield (resource)

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
  
}