package org.arriba.snippet

import net.liftweb.squerylrecord.RecordTypeMode._
import net.liftweb.http.S
import org.arriba.model.ArribaDb._
import org.arriba.model.Resource
import org.arriba.model.Version
import java.util.UUID
import scala.xml.Text
import net.liftweb.common._
import org.arriba.util.Path
import org.arriba.util.Path._
import net.liftweb.http.SHtml
import scala.xml.NodeSeq
import org.arriba.content.DefaultTemplates
import net.liftweb.http.FieldBinding
import net.liftweb.util.FieldContainer
import net.liftweb.http.FieldBinding._
import org.arriba.content.Template
import net.devkat.lift.http.CssBoundBootstrapScreen

class CreateResource extends CssBoundBootstrapScreen {
  
  def formName = "create"

  object resource extends ScreenVar[Resource](Resource.createRecord)
  /*
  val parentPathField = makeField[String, Nothing](
      "Parent path",
      resource.parent.map(_.path).getOrElse(Path.root).toString,
      f => Some(<span class="form-control">{f.get}</span>),
      NothingOtherValueInitializer)
  */
      /*
  addFields(() => resource.slug)
  addFields(() => resource.name)
       */
      
  field("Parent path", resource.parent.map(_.path).getOrElse(Path.root).toString, FieldBinding("parentPath", Self))
  field(resource.slug, FieldBinding("slug"))
  field(resource.name, FieldBinding("name"))

  val templates = DefaultTemplates.templates map { case (key, t) => (key, t.name) } toSeq
  val template = typedRadio("Template", "", templates, valMinLen(1, "Please select a template."), FieldBinding("template"))

      /*
  addFields(() => new FieldContainer {
    def allFields =  resource.allFields.flatMap(f => field(f, FieldBinding(f.name)))
  })
  */


  override def localSetup() {
    super.localSetup()
    //DefaultTemplates.init()
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
    Templates.get(template.get) match {
      case Template(name, Some(vt)) => {
        resource.newVersion(vt.mediaType, vt.content)
      }
    }
  }
  
  override protected def redirectBack() = S.redirectTo(resource.href)

}