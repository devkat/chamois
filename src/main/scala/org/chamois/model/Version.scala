package org.chamois.model

import java.sql.Timestamp
import org.squeryl.annotations.Column
import net.liftweb.record.Record
import net.liftweb.squerylrecord.KeyedRecord
import net.liftweb.record.field._
import net.liftweb.record.MetaRecord
import net.liftweb.squerylrecord.RecordTypeMode._
import ChamoisDb._
import net.liftweb.util.Html5
import net.liftweb.common._
import org.squeryl.dsl.CompositeKey2
import org.squeryl.KeyedEntity
import org.squeryl.Query
import scala.xml.NodeSeq
import java.io.ByteArrayInputStream
import org.chamois.util.MediaType

case class Version private() extends Record[Version]
    with KeyedEntity[CompositeKey2[StringField[Version], IntField[Version]]] {

  override def meta = Version
  
  @Column(name="uuid")
  val uuid = new StringField(this, 36)
  
  val number = new IntField(this)
  
  def id = compositeKey(uuid, number)
    
  val created = new DateTimeField(this)
  val content = new BinaryField(this)
  
  @Column(name="media_type")
  val mediaTypeString = new StringField(this, 256)
  
  def mediaType = MediaType.parse(mediaTypeString.get).get
  
  @Column(name="content_length")
  val contentLength = new LongField(this)
  
  def document = documentToVersions.right(this).headOption.get
  
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
  
  def findLatestVersion(uuid:String): Option[Version] =
    from(versions)(v => where(v.uuid === uuid) select(v) orderBy(v.number).desc).page(0, 1).headOption
    
  def newVersion(uuid:String): Version = {
    val version = Version.createRecord
    version.uuid.set(uuid)
    val num = from(versions)(v => where(v.uuid === uuid) compute(max(v.number))):Option[Int]
    version.number.set(num match {
      case Some(n) => n + 1
      case None => 0
    })
    version
  }

}