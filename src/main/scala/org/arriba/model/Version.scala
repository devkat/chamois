package org.arriba.model

import java.sql.Timestamp
import org.squeryl.annotations.Column
import net.liftweb.record.Record
import net.liftweb.squerylrecord.KeyedRecord
import net.liftweb.record.field._
import net.liftweb.record.MetaRecord
import net.liftweb.squerylrecord.UuidRecordTypeMode._
import ArribaDb._
import net.liftweb.util.Html5
import net.liftweb.common._
import org.squeryl.dsl.CompositeKey2
import org.squeryl.KeyedEntity
import org.squeryl.Query
import scala.xml.NodeSeq
import java.io.ByteArrayInputStream
import org.arriba.content.MediaType
import java.util.UUID
import net.liftweb.util.FieldError
import net.liftweb.util.FieldIdentifier
import org.arriba.content.ResourceType
import org.arriba.content.ResourceTypeRegistry
import org.arriba.content.HtmlResourceType
import scala.xml.Node

case class Version private() extends Record[Version]
    with KeyedEntity[CompositeKey2[LongField[Version], IntField[Version]]] {

  override def meta = Version
  
  @Column(name="resource_id")
  val resourceId = new LongField(this)
  
  val number = new IntField(this)
  
  def id = compositeKey(resourceId, number)
    
  val created = new DateTimeField(this)
  val content = new BinaryField(this)
  
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
  
  @Column(name="content_length")
  val contentLength = new LongField(this)
  
  def resource = resourceToVersions.right(this).headOption.get
  
  def resourceType = ResourceTypeRegistry.find(mediaType)
  
  def html:Box[Node] = resourceType match {
    case Some(t) => t.html(this)
    case None => Failure("Could not determine resource type for media type " + mediaTypeString.get)
  }
  
}

object Version extends Version with MetaRecord[Version] {
  
  def findLatestVersion(resourceId:Long): Option[Version] =
    from(versions)(v => where(v.resourceId === resourceId) select(v) orderBy(v.number).desc).page(0, 1).headOption
    
  def newVersion(resourceId:Long): Version = {
    val version = Version.createRecord
    version.resourceId.set(resourceId)
    val num = from(versions)(v => where(v.resourceId === resourceId) compute(max(v.number))):Option[Int]
    version.number.set(num match {
      case Some(n) => n + 1
      case None => 0
    })
    version
  }

}