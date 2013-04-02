package org.chamois.model

import java.sql.Timestamp
import org.squeryl.annotations.Column
import net.liftweb.record.Record
import net.liftweb.squerylrecord.KeyedRecord
import net.liftweb.record.field._
import net.liftweb.record.MetaRecord
import net.liftweb.squerylrecord.RecordTypeMode._
import ChamoisDb._
import net.liftweb.json.JsonAST
import net.liftweb.json.JField
import net.liftweb.json.JValue
import net.liftweb.json.JArray

case class Document private() extends Record[Document] with KeyedRecord[Long] {

  override def meta = Document
  
  @Column(name="id")
  override val idField = new LongField(this)
    
  val name = new StringField(this, 256)
  
  @Column(name="repository_id")
  val repositoryId = new LongField(this);
  
  def unapply(idstr:String): Option[Document] =
    try {
      Document.findById(idstr.toInt)
    } catch {
      case _ => None
    }
  
  def unapply(json: JValue): Option[Document] =
    Document.fromJValue(json)

  def currentVersion =
    Version.findLatestVersion(idField.get)
    
  def delete_! = documents.delete(this.id)
  
}

object Document extends Document with MetaRecord[Document] {
  
  def findById(id:Long): Option[Document] =
    from(documents)(d => where(d.id === id) select(d)).headOption

  def findAll: List[Document] =
    from(documents)(d => select(d)).toList

  protected def encodeAsJSON_! (toEncode: Document): JsonAST.JObject = {
    toEncode.runSafe {
      JsonAST.JObject(
        List(JField("id", toEncode.idField.asJValue))
      )
    }
  }

  implicit def asJson(doc:Document): JValue =
    //= encodeAsJSON_!(doc)
    doc.asJValue

  implicit def asJson(docs:List[Document]): JValue =
    JArray(docs map asJson)
  
}