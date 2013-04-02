package org.chamois.model

import org.squeryl.Schema
import org.squeryl.annotations.Column
import net.liftweb.squerylrecord.RecordTypeMode._
import net.liftweb.squerylrecord.KeyedRecord
import org.squeryl.Table

object ChamoisDb extends Schema {
  
  val users = table[User]("users")
  val roles = table[Role]("role")
  val repositories = table[Repository]("repository")
  val documents = table[Document]("document")
  val versions = table[Version]("version")
  val folders = table[Folder]("folder")

  on(users)(u => declare(
      u.email defineAs(unique, indexed)
  ))
  
  for (tab <- List(users, roles, repositories, documents, folders)) {
    val keyed = tab.asInstanceOf[Table[KeyedRecord[Long]]]
    on(keyed)(t => declare(
          t.idField defineAs(autoIncremented(tab.name + "_id_seq"))
    ))
  }
  
  val repositoryToDocuments = oneToManyRelation(repositories, documents).
    via((r, d) => r.id === d.repositoryId)
    
  val userToRole =
    manyToManyRelation(users, roles, "user_to_role").
    via[UserToRole]((u, r, ur) => (ur.userId === u.id, ur.roleId === r.id))

  val rootFolders =
    manyToManyRelation(repositories, folders, "root_folder").
    via[RootFolder]((r, f, rf) => (rf.repositoryId === r.id, rf.folderId === f.id))

}