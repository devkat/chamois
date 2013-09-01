package org.arriba.content

import net.liftweb.util.ClassHelpers._
import net.liftweb.common._
import java.io.File
import org.arriba.util.FileUtil
import org.arriba.model.Version
import scala.xml.Node
import net.liftweb.util.Html5
import java.io.ByteArrayInputStream
import net.liftweb.http.RewriteResponse

trait ResourceType {
  
  def matches(mediaType: MediaType): Boolean
  
  def html(v:Version): Box[Node]

  def isHtml: Boolean
  
}

object ResourceTypeRegistry {
  
  /*
  private var resourceTypes = scala.collection.mutable.Set.empty[ResourceType]
  
  def register(types: ResourceType*) {
    for (t <- types) resourceTypes += t
  }
  
  def init() {
    register(
      HtmlResourceType
    )
  }
  */
  
  lazy val resourceTypes = Set(
    HtmlResourceType
  )
  
  def find(mediaType: MediaType): Option[ResourceType] =
    resourceTypes.find(_.matches(mediaType))
    
}

object HtmlResourceType extends ResourceType {
  
  def matches(mediaType: MediaType) = mediaType match {
    case MediaType("application", "xhtml+xml") => true
    case MediaType("text", "html") => true
    case _ => false
  }
  
  def html(v:Version) =
    Html5.parse(new ByteArrayInputStream(v.content.get))
  
  def isHtml = true
  
}