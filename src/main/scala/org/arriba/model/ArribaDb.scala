package org.arriba.model

import org.squeryl.Schema
import org.squeryl.annotations.Column
import net.liftweb.squerylrecord.UuidRecordTypeMode._
import net.liftweb.squerylrecord.KeyedRecord
import org.squeryl.Table

object ArribaDb extends Schema {
  
  val users = table[User]("users")
  val roles = table[Role]("role")
  val resources = table[Resource]("resource")
  val versions = table[Version]("version")

  on(users)(u => declare(
      u.email defineAs(unique, indexed)
  ))
  
  for (tab <- List(users, roles, resources)) {
    val keyed = tab.asInstanceOf[Table[KeyedRecord[Long]]]
    on(keyed)(t => declare(
          t.idField defineAs(autoIncremented(tab.name + "_id_seq"))
    ))
  }
  
  val resourceToChildren = oneToManyRelation(resources, resources).
    via((parent, child) => child.parentId === parent.id)
    
  val resourceToVersions = oneToManyRelation(resources, versions).
    via((res, v) => res.id === v.resourceId)
    
  val userToRole =
    manyToManyRelation(users, roles, "user_to_role").
    via[UserToRole]((u, r, ur) => (ur.userId === u.id, ur.roleId === r.id))

}