package org.moscatocms.repo

import java.sql.Timestamp
import org.squeryl.annotations.Column
import net.liftweb.record.Record
import net.liftweb.squerylrecord.KeyedRecord
import net.liftweb.record.field._
import net.liftweb.record.MetaRecord
import net.liftweb.squerylrecord.UuidRecordTypeMode._
import MoscatoDb._
import net.liftweb.util.Html5
import net.liftweb.common._
import org.squeryl.dsl.CompositeKey2
import org.squeryl.KeyedEntity
import org.squeryl.Query
import scala.xml.NodeSeq
import java.io.ByteArrayInputStream
import java.util.UUID
import net.liftweb.util.FieldError
import net.liftweb.util.FieldIdentifier

case class Version private() extends Record[Version] with KeyedRecord[Long] {

  override def meta = Version
  
  @Column(name="id")
  override val idField = new LongField(this)
    
  val created = new DateTimeField(this)
  val content = new BinaryField(this)
  
  @Column(name="previous_version_id")
  val previousVersionId = new OptionalLongField(this)

  // TODO: don't load content for getting length
  def contentLength = content.get.length
  
  def nextVersion = from(versions)(v => where(v.previousVersionId === this.id) select(v)).headOption
  
  def resource:Resource = from(resources)(r => where(r.currentVersionId === this.id) select(r)).headOption match {
    case Some(r) => r
    case None => nextVersion.map(_.resource).get
  }
  
}

object Version extends Version with MetaRecord[Version] {
  
}