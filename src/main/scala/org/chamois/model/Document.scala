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

case class Document private() extends Record[Document] with KeyedRecord[String] {

  override def meta = Document
  
  @Column(name="uuid")
  override val idField = new StringField(this, 36)
  
  def uuid = idField
    
  val name = new StringField(this, 256)
  
  def unapply(uuid:String): Option[Document] =
    Document.findByUuid(uuid)
  
  def unapply(json: JValue): Option[Document] =
    Document.fromJValue(json)
    
  def versions = documentToVersions.left(this)

  def currentVersion =
    Version.findLatestVersion(idField.get)
    
  def delete_! = documents.delete(this.id)
  
  def node = from(nodes)(n => where(n.documentUuid === Some(uuid.get)).select(n)).headOption.get
  
}

object Document extends Document with MetaRecord[Document] {
  
  def findByUuid(uuid:String): Option[Document] =
    from(documents)(d => where(d.uuid === uuid) select(d)).headOption

  def findAll: List[Document] =
    from(documents)(d => select(d)).toList

  protected def encodeAsJSON_! (toEncode: Document): JsonAST.JObject = {
    toEncode.runSafe {
      JsonAST.JObject(
        List(JField("uuid", toEncode.uuid.asJValue))
      )
    }
  }

  implicit def asJson(doc:Document): JValue =
    //= encodeAsJSON_!(doc)
    doc.asJValue

  implicit def asJson(docs:List[Document]): JValue =
    JArray(docs map asJson)
  
}