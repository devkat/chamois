package org.moscato.repo

import org.squeryl.KeyedEntity
import org.squeryl.annotations.Column
import net.liftweb.record.field._
import net.liftweb.squerylrecord.KeyedRecord
import net.liftweb.record.{Record, MetaRecord}

class Role private() extends Record[Role] with KeyedRecord[Int] {
  
  override def meta = Role

  @Column(name="id")
  override val idField = new IntField(this)
  
  val name = new StringField(this, 256)
}

object Role extends Role with MetaRecord[Role] {
  
}