package org.arriba.snippet

import net.liftweb.squerylrecord.RecordTypeMode._
import net.liftweb.http.S
import org.arriba.record.ArribaDb._
import org.arriba.record.{Node, Resource, Version}
import java.util.UUID
import scala.xml.{Elem,Text}
import net.liftweb.common._
import org.arriba.model.Path
import org.arriba.model.Path._
import net.liftweb.http.SHtml
import scala.xml.NodeSeq
import net.liftweb.http.FieldBinding
import net.liftweb.util.FieldContainer
import net.liftweb.http.FieldBinding._
import org.arriba.content.Template
import net.devkat.lift.http.CssBoundBootstrapScreen
import net.liftweb.util.Html5
import org.arriba.content.TemplateVersion
import org.arriba.content.MediaType

class CreateResource extends CssBoundBootstrapScreen {
  
  def formName = "create"

  object node extends ScreenVar[Node](Node.createRecord)
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
      
  field("Parent path", node.parent.map(_.path).getOrElse(Path.root).toString, FieldBinding("parentPath", Self))
  field(node.slug, FieldBinding("slug"))
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
    val parent:Option[Node] = S.param("parent").map(Path.unapply(_)) match {
      case Full(parPath) => {
        parPath match {
          case Path.root => None
          case path => Node.findByPath(path) match {
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
    node.parentId.set(parent map (_.id))
    nodes.insert(node)
    
    resource.uuid.set(UUID.randomUUID)
    resource.nodeId.set(node.id)
  }
  
  def finish() {
    Templates.get(template.get) match {
      case Template(name, Some(vt)) => {
        resource.setMediaType(vt.mediaType)
        resource.newVersion(vt.content)
      }
    }
    resources.insert(resource)
  }
  
  override protected def redirectBack() = S.redirectTo(resource.href)

}

object DefaultTemplates {
  
  implicit def string2bytes(s:String) = s.getBytes("utf-8")
  implicit def html2bytes(n:Elem) = string2bytes(Html5.toString(n))
  
  val templates = Map(
      
    "html" -> Template("Empty HTML page", Some(TemplateVersion(MediaType("text", "html"), <html>
              <head>
                <title>New HTML5 document</title>
              </head>
              <body>
                <p>Your text goes here</p>
              </body>
            </html>))),
            
    "css" -> Template("CSS stylesheet", Some(TemplateVersion(MediaType("text", "css"), "")))
    
  )

  val templatesSlug = "templates"
  val templatesPath = Path(templatesSlug :: Nil)

}