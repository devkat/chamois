package org.moscato.repo

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
    
  def currentVersion = MoscatoDb.versions.lookup(currentVersionId.get).get
    
  def delete_! = resources.delete(id)
  
  def newVersion(content: Array[Byte]) = {
    val version = Version.createRecord
    if (currentVersionId.get != currentVersionId.defaultValue)
      version.previousVersionId.set(Some(currentVersionId.get))
    version.content.set(content)
    MoscatoDb.versions.insert(version)
    currentVersionId.set(version.id)
    resources.update(this)
    version
  }
  
  def versions:List[Version] = versions(Some(currentVersion))
  
  protected def versions(version:Option[Version]):List[Version] =
    version match {
      case Some(v) => v :: versions(v.previousVersionId.get flatMap {MoscatoDb.versions.lookup(_)})
      case None => Nil
    }

  val name = new StringField(this, 256)
  
  @Column(name="folder_id")
  val folderId = new OptionalLongField(this)
  
  def folder = folderId.get map (folders.lookup(_).get)

  def path = folder.map(_.path).getOrElse(Path.root) / slug.get
  
  val slug = new StringField(this, 256)

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
  
  def rootResources =
    from(resources)(n => where(n.folderId isNull) select(n))

  def findByUuid(uuid:UUID): Option[Resource] =
    from(resources)(d => where(d.uuid === uuid) select(d)).headOption
  
  def folderClause(r:Resource, folder:Option[Folder]) = folder match {
    case Some(f) => r.folderId === f.id
    case None => r.folderId.isNull
  }
    
  def findByUrl(folderPath:Path, slug:String, ext:String) =
    for {
      mediaType <- MediaType.byExtension(ext)
      folderOption <- folderPath match {
        case Path.root => Some(None)
        case p => Folder.findByPath(p) map { Some(_) }
      }
      resource <- from(resources)(r => where(
          folderClause(r, folderOption) and
          r.slug === slug and
          r.mediaTypeString === mediaType.toString) select(r)).headOption
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