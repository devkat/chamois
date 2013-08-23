package org.chamois.rest
import net.liftweb.http._
import net.liftweb.http.rest.RestHelper
import net.liftweb.json.JString
import net.liftweb.json.JArray
import org.chamois.model.Document
import net.liftweb.json.JValue
import net.liftweb.json.JBool
import net.liftweb.common._
import scala.xml.NodeSeq
import net.liftweb.util.Html5
import scala.xml.Elem
import org.chamois.model._
import net.liftweb.squerylrecord.RecordTypeMode._
import scala.xml.XML

object MercuryRest extends RestHelper {
  
  import ChamoisDb._
  
  serve( "api" / "mercury" prefix {
    
    // post document as JSON
    case path JsonPut json -> _ => {
      
      Node.findByPath(path).flatMap(_.document) match {
        case Some(doc) => {
          val version = Version.newVersion(doc.uuid.get)
          version.mediaTypeString.set("application/xhtml+xml")
          
          val markup = (json \ "content" \ "content" \ "value").extract[String]
          //println("#" * 20 + "\nMarkup: " + markup)
          val html = Html5.parse(markup).get
          val page = <html>
            <head><title>{doc.name.get}</title></head>
            <body>{html}</body>
          </html>
          
          
          val bytes = Html5.toString(page).getBytes("utf-8")
          version.content.set(bytes)
          version.contentLength.set(bytes.length)
          versions.insert(version)
          JsonResponse("OK", Nil, Nil, 200)
        }
        case None => NotFoundResponse()
      }
      
      
    }
    
  })
}