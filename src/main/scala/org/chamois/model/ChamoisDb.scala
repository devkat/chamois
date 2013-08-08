package org.chamois.model

import org.squeryl.Schema
import org.squeryl.annotations.Column
import net.liftweb.squerylrecord.RecordTypeMode._
import net.liftweb.squerylrecord.KeyedRecord
import org.squeryl.Table

object ChamoisDb extends Schema {
  
  val users = table[User]("users")
  val roles = table[Role]("role")
  val documents = table[Document]("document")
  val versions = table[Version]("version")
  val nodes = table[Node]("node")

  on(users)(u => declare(
      u.email defineAs(unique, indexed)
  ))
  
  for (tab <- List(users, roles, nodes)) {
    val keyed = tab.asInstanceOf[Table[KeyedRecord[Long]]]
    on(keyed)(t => declare(
          t.idField defineAs(autoIncremented(tab.name + "_id_seq"))
    ))
  }
  
  val nodeToChildren = oneToManyRelation(nodes, nodes).
    via((parent, child) => parent.id === child.parentId)
    
  val documentToVersions = oneToManyRelation(documents, versions).
    via((doc, v) => doc.uuid === v.uuid)
    
  val userToRole =
    manyToManyRelation(users, roles, "user_to_role").
    via[UserToRole]((u, r, ur) => (ur.userId === u.id, ur.roleId === r.id))

}