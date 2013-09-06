package org.moscato.rest
import net.liftweb.http._
import net.liftweb.http.rest.RestHelper
import net.liftweb.json.JString
import net.liftweb.json.JArray
import net.liftweb.json.JValue
import net.liftweb.json.JBool
import net.liftweb.common._
import scala.xml.NodeSeq
import net.liftweb.util.Html5
import scala.xml.Elem
import org.moscato.repo._
import net.liftweb.squerylrecord.RecordTypeMode._
import scala.xml.XML

object MercuryRest extends RestHelper {
  
  import MoscatoDb._
  
  serve( "moscato" / "rest" / "mercury" prefix {
    
    // post document as JSON
    case path JsonPut json -> _ => {
      
      Resource.findByUrl(path.take(path.length - 1), path.last, "") match {
        case Some(resource) => {
          
          val markup = (json \ "content" \ "content" \ "value").extract[String]
          //println("#" * 20 + "\nMarkup: " + markup)
          val html = Html5.parse(markup).get
          val page = <html>
            <head><title>{resource.uuid.get}</title></head>
            <body>{html}</body>
          </html>
          
          
          val bytes = Html5.toString(page).getBytes("utf-8")
          resource.newVersion(bytes)
          JsonResponse("OK", Nil, Nil, 200)
        }
        case None => NotFoundResponse()
      }
      
      
    }
    
  })
}