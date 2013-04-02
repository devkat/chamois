package org.chamois.rest
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
import org.chamois.model.Repository
import org.chamois.model.ChamoisDb

object RepositoriesRest extends RestHelper {

  serve( "api" / "repository" prefix {
    
    // get repository list as JSON
    case "index" :: Nil JsonGet _ => Repository.findAll: JValue
    
    // get repository as JSON
    case Repository(repo) :: Nil JsonGet _ => repo: JValue
    
  })
    
  serve( "api" / "repository" prefix {
    
    // post repository as JSON
    case "index" :: Nil JsonPost Repository(repo) -> _ => {
      ChamoisDb.repositories.insert(repo)
      val headers = List(("Location", "/api/repository/" + repo.id))
      new JsonResponse(Repository.asJson(repo), headers, Nil, 201)
    }
    
    // POST document content
    /*
    case Repository(repo) :: Nil Post req => {
      doc.content.setFromAny(new String(req.body.openTheBox, "UTF-8"));
      doc.save
      new OkResponse
    }
    */
    
    // DELETE document
    case Repository(repo) :: Nil JsonDelete _ => JBool(repo.delete_!)
    
  })
  
}