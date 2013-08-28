package org.arriba.content

import net.liftweb.squerylrecord.RecordTypeMode._
import org.arriba.model.{Resource, Version}
import org.arriba.model.ArribaDb._
import org.arriba.util.Path
import MediaType._
import net.liftweb.util.Html5
import scala.xml.Node
import scala.xml.NodeSeq
import scala.xml.Elem

object DefaultTemplates {
  
  implicit def string2bytes(s:String) = s.getBytes("utf-8")
  implicit def html2bytes(n:Elem) = string2bytes(Html5.toString(n))
  
  val templates = Map(
      
    "html" -> Template("Empty HTML page", Some(TemplateVersion(HTML, <html>
              <head>
                <title>New HTML5 document</title>
              </head>
              <body>
                <p>Your text goes here</p>
              </body>
            </html>))),
            
    "css" -> Template("CSS stylesheet", Some(TemplateVersion(CSS, "")))
    
  )

  val templatesSlug = "templates"
  val templatesPath = Path(templatesSlug :: Nil)
  
  def createResource(slug:String, name:String, parent:Option[Resource] = None)(withResource: Resource => Unit) {
    val resource = Resource.createRecord
    resource.slug.set(slug)
    resource.name.set(name)
    resources.insert(resource)
    withResource(resource)
  }
  
  def createVersion(r:Resource, mediaType:MediaType, content:Array[Byte]) = {
    val version = Version.createRecord
    version.resourceId.set(r.id)
    version.setMediaType(mediaType)
    version.content.set(content)
    version
  }
  
  def init() {
    if (Resource.findByPath(templatesPath).isEmpty) {
      createResource(templatesSlug, "Templates") { templates =>

        createResource("html5", "HTML5 document", Some(templates)) {
          createVersion(_, MediaType("text", "html"), <html>
              <head>
                <title>New HTML5 document</title>
              </head>
              <body>
                <p>Your text goes here</p>
              </body>
            </html>)
        }

        createResource("binary", "Binary resource", Some(templates)) {
          createVersion(_, MediaType("image", "png"), "")
        }

        createResource("css", "CSS stylesheet", Some(templates)) {
          createVersion(_, MediaType("text", "css"), "")
        }

      }
      
      
    }
  }

}