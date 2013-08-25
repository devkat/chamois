package org.chamois.snippet

import net.liftweb.squerylrecord.RecordTypeMode._
import net.liftweb.http.S
import org.chamois.model.ChamoisDb._
import org.chamois.model.Resource
import org.chamois.model.Version
import java.util.UUID
import scala.xml.Text
import net.liftweb.common._
import org.chamois.util.Path
import net.liftweb.http.SHtml
import scala.xml.NodeSeq

object CreateResource extends BootstrapScreen {

  override def screenTop = <span>Create resource</span>
    
  object parentPath extends ScreenVar[Box[String]](Empty)
  object resource extends ScreenVar(Resource.createRecord)
  
  /*
  val textField = new Field { 
    type ValueType = String
    override def default = "" 
    override def toForm: Box[NodeSeq] = Some(Text(is))
    lazy val manifest = buildIt[ValueType]
    override def editable_? = false
  }
  
  val parentPathField = textField("Parent path", parentPath)
  */
  
  val parentPathField = makeField[String, Nothing](
      "Parent path",
      parentPath.get.openOr("(none)"),
      f => Some(<span class="form-control">{f.get}</span>),
      NothingOtherValueInitializer)
  
  addFields(() => resource.slug)

  override def localSetup() {
    super.localSetup()
    parentPath.set(S.param("parent"))
  }
  
  def finish() {
    
    def createResource(parent:Option[Resource] = None) {
      resource.uuid.set(UUID.randomUUID)
      resource.parentId.set(parent map (_.id))
      resources.insert(resource)
      resource.newVersion()
    }
    
    parentPath.get match {
      case Full(p) => {
        p match {
          case Path.root => createResource()
          case path => Resource.findByPath(path) match {
            case None => S.notice("Parent resource " + path + " not found.")
            case node => createResource(node)
          }
        }
      }
      case Empty => createResource()
      case Failure(msg, _, _) => S.notice("Failure: " + msg)
    }

  }
  
  /*
  val slug = field(S ? "Slug", "", trim, valMinLen(1, "Please enter a slug."))
  val name = field(S ? "Name", "", trim, valMinLen(1, "Please enter a name."))
  
  def finish() {
    
    def createDoc() = {
      val uuid = UUID.randomUUID.toString
      val doc = Document.createRecord
      doc.uuid.set(uuid)
      doc.name.set(name.get)
      documents.insert(doc)
      val version = Version.newVersion(uuid)
      versions.insert(version)
      doc
    }

    def createNode(doc:Document, parent:Option[Node] = None) = {
      val node = Node.createRecord
      node.documentUuid.set(Some(doc.uuid.get))
      node.slug.set(slug.get)
      node.parentId.set(parent map (_.id))
      nodes.insert(node)
    }
    
    parentPath.get match {
      case Full(p) => {
        p match {
          case Path.root => createNode(createDoc())
          case path => Node.findByPath(path) match {
            case None => S.notice("Parent node " + path + " not found.")
            case opt => createNode(createDoc(), opt)
          }
        }
      }
      case Empty => createDoc()
      case Failure(msg, _, _) => S.notice("Failure: " + msg)
    }
  }
  */
  
}