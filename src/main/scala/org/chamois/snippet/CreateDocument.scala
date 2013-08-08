package org.chamois.snippet

import net.liftweb.squerylrecord.RecordTypeMode._
import net.liftweb.http.S
import org.chamois.model.ChamoisDb._
import org.chamois.model.Document
import org.chamois.model.Node
import org.chamois.model.Version
import java.util.UUID
import scala.xml.Text

object CreateDocument extends BootstrapScreen {

  override def screenTop = <span>Create document</span>
  
  val slug = field(S ? "Slug", "", trim, valMinLen(1, "Please enter a slug."))
  val name = field(S ? "Name", "", trim, valMinLen(1, "Please enter a name."))
  
  def finish() {
    val uuid = UUID.randomUUID.toString
    val doc = Document.createRecord
    doc.uuid.set(uuid)
    doc.name.set(name.get)
    documents.insert(doc)
    
    val version = Version.newVersion(uuid)
    versions.insert(version)
    
    val node = Node.createRecord
    node.documentUuid.set(Some(uuid))
    node.slug.set(slug.get)
    nodes.insert(node)
  }
  
}