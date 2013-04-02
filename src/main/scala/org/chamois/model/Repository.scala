package org.chamois.model

import java.sql.Timestamp
import org.squeryl.annotations.Column
import net.liftweb.record.Record
import net.liftweb.squerylrecord.KeyedRecord
import net.liftweb.record.field._
import net.liftweb.record.MetaRecord
import net.liftweb.squerylrecord.RecordTypeMode._
import ChamoisDb._
import net.liftweb.common.Box
import net.liftweb.json.JValue
import net.liftweb.json.JArray
import net.liftweb.json.JObject
import net.liftweb.json.JField
import net.liftweb.json.JString
import java.text.SimpleDateFormat
import net.liftweb.json.DefaultFormats

class Repository private () extends Record[Repository] with KeyedRecord[Long] {

  override def meta = Repository

  @Column(name = "id")
  override val idField = new LongField(this)

  val name = new StringField(this, 256)
  val slug = new StringField(this, 256)
  
  val created = new DateTimeField(this) {
  override val formats = new DefaultFormats {
    override def dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'") 
  } 
    
  }
  
  lazy val documents = ChamoisDb.repositoryToDocuments.left(this)

  def unapply(idstr: String): Option[Repository] =
    Repository.findById(augmentString(idstr).toInt)

  def unapply(json: JValue): Option[Repository] =
    Repository.fromJson(json)

  def delete_! = repositories.delete(this.id)

}

object Repository extends Repository with MetaRecord[Repository] {

  def findAll: List[Repository] =
    from(repositories)(r => select(r) orderBy (name)).toList

  def findBySlug(slug: String): Option[Repository] =
    from(repositories)(r => where(r.slug === slug) select (r)).headOption

  def findById(id: Int): Option[Repository] =
    from(repositories)(r => where(r.id === id) select (r)).headOption

  implicit def asJson(repos: List[Repository]): JValue =
    JArray(repos map asJson)

  implicit def fromJson(jvalue: JValue): Box[Repository] =
    fromJValue(jvalue)

  implicit def asJson(toEncode: Repository): JObject =
    toEncode.asJValue
    /*
    toEncode.runSafe {
      JObject(
        List(
          JField("id", toEncode.idField.asJValue),
          JField("name", toEncode.name.asJValue),
          JField("created", toEncode.created.asJValue)
        )
      )
    }
    */

}