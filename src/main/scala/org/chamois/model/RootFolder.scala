package org.chamois.model

import org.squeryl.KeyedEntity
import org.squeryl.annotations.Column
import org.squeryl.dsl.CompositeKey2
import net.liftweb.squerylrecord.RecordTypeMode._

class RootFolder(
    @Column("repository_id")
    val repositoryId: Long,
    @Column("folder_id")
    val folderId: Int)
  extends KeyedEntity[CompositeKey2[Long, Int]] {
  def id = compositeKey(repositoryId, folderId)
}
      