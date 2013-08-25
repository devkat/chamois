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
import org.chamois.util.Path._
import net.liftweb.http.SHtml
import scala.xml.NodeSeq

object CreateResource extends BootstrapScreen {

  override def screenTop = <span>Create resource</span>
    
  object resource extends ScreenVar[Resource](Resource.createRecord)
  
  val parentPathField = makeField[String, Nothing](
      "Parent path",
      resource.parent.map(_.path).getOrElse(Path.root).toString,
      f => Some(<span class="form-control">{f.get}</span>),
      NothingOtherValueInitializer)
  
  addFields(() => resource.slug)

  override def localSetup() {
    super.localSetup()
    val parent:Option[Resource] = S.param("parent").map(Path.unapply(_)) match {
      case Full(parPath) => {
        parPath match {
          case Path.root => None
          case path => Resource.findByPath(path) match {
            case None => {
              S.notice("Parent resource " + path + " not found, creating root resource.")
              None
            }
            case r => r
          }
        }
      }
      case Empty => None
      case Failure(msg, _, _) => {
        S.error(msg)
        None
      }
    }
    
    resource.uuid.set(UUID.randomUUID)
    resource.parentId.set(parent map (_.id))
  }
  
  def finish() {
    resources.insert(resource)
  }
  
  override protected def redirectBack() = S.redirectTo("/resource" + resource.href)

}