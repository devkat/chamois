package org.chamois.model

import java.sql.Timestamp
import org.squeryl.annotations.Column
import net.liftweb.record.Record
import net.liftweb.squerylrecord.KeyedRecord
import net.liftweb.record.field._
import net.liftweb.record.MetaRecord
import net.liftweb.squerylrecord.UuidRecordTypeMode._
import ChamoisDb._
import net.liftweb.util.Html5
import net.liftweb.common._
import org.squeryl.dsl.CompositeKey2
import org.squeryl.KeyedEntity
import org.squeryl.Query
import scala.xml.NodeSeq
import java.io.ByteArrayInputStream
import org.chamois.util.MediaType
import java.util.UUID

case class Version private() extends Record[Version]
    with KeyedEntity[CompositeKey2[LongField[Version], IntField[Version]]] {

  override def meta = Version
  
  @Column(name="resource_id")
  val resourceId = new LongField(this)
  
  val number = new IntField(this)
  
  def id = compositeKey(resourceId, number)
    
  val created = new DateTimeField(this)
  val content = new BinaryField(this)
  
  @Column(name="media_type")
  val mediaTypeString = new StringField(this, 256)
  
  def mediaType = MediaType.parse(mediaTypeString.get).get
  
  @Column(name="content_length")
  val contentLength = new LongField(this)
  
  def resource = resourceToVersions.right(this).headOption.get
  
  def htmlPage(title:String, content:NodeSeq) =
    <html>
      <head>
        <title>{title}</title>
      </head>
      <body>
        {content}
      </body>
    </html>
  
  def xmlContent = {
    this.mediaType match {
      case MediaType("application", "xhtml+xml") =>
        Html5.parse(new ByteArrayInputStream(content.get)).get
      case _ => htmlPage("Not an HTML page", <p>Not an HTML page</p>)
    }
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