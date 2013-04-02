package org.chamois.model

import java.sql.Timestamp
import org.squeryl.annotations.Column
import net.liftweb.record.Record
import net.liftweb.squerylrecord.KeyedRecord
import net.liftweb.record.field._
import net.liftweb.record.MetaRecord
import net.liftweb.squerylrecord.RecordTypeMode._
import ChamoisDb._

class Folder private() extends Record[Folder] with KeyedRecord[Long] {

  override def meta = Folder
  
  @Column(name="id")
  override val idField = new LongField(this)
    
  val name = new StringField(this, 256)
  val created = new DateTimeField(this)
  
  @Column(name="repository_id")
  val repositoryId = new LongField(this);

}

object Folder extends Folder with MetaRecord[Folder] {
  
}