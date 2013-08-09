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
import org.chamois.model.Document
import org.chamois.model.ChamoisDb
import org.chamois.model.Version
import org.apache.tika.io.IOUtils
import org.apache.tika.Tika
import net.liftweb.squerylrecord.RecordTypeMode._

object DocumentsRest extends RestHelper { //}RestService[Document]("document") {
  
  import ChamoisDb._
  
  lazy val tika = new Tika();

  /*
  serve( "api" / "document" prefix {
    
    case Document(doc) :: Nil Get Req(_, "html", _) => doc.currentVersion match {
      case None => NotFoundResponse()
      case Some(version) => AppXmlResponse(version.xmlContent)
    }
    
    // GET document
    case "index" :: Nil JsonGet _ =>
      S.param("repository") match {
        case Full(repoId) =>
            Repository.findById(augmentString(repoId).toInt) match {
              case Some(repo) => repo.documents.toList: JValue
              case None => NotFoundResponse()
            }
        case _ => Document.findAll: JValue
      }

    case Document(doc) :: Nil JsonGet _ => doc: JValue
    
  })
    
  serve( "api" / "document" prefix {
    
    // post document as JSON
    case "index" :: Nil JsonPost Document(doc) -> _ => {
      documents.insert(doc)
      val headers = List(("Location", "/api/documents/" + doc.id))
      new JsonResponse(Document.asJson(doc), headers, Nil, 201)
    }
    
    // post document
    case "index" :: Nil Post req => {
      
      val doc = Document.createRecord
      for (repoId <- req.param("repositoryId")) { doc.repositoryId.set(repoId.toLong) }
      for (name <- req.param("name")) { doc.name.set(name) }
      documents.insert(doc)
      
      val version = Version.newVersion(doc.id)
      req.uploadedFiles flatMap (fileHolder  => {
        var bytes = IOUtils.toByteArray(fileHolder.fileStream)
        version.content.set(bytes)
        version.contentLength.set(fileHolder.length)
        version.mediaType.set(tika.detect(bytes))
      })
      
      ChamoisDb.versions.insert(version)
      val headers = List(("Location", "/api/documents/" + doc.id))
      new PlainTextResponse("Document created", headers, 201)
    }
    
    // POST document JSON
    /*
    case Nil JsonPost Document(doc) -> _ => {
      if (doc.save) {
        val headers = List(("Location", "/api/document/" + doc.id))
        new JsonResponse(Document.toJson(doc), headers, Nil, 201)
      }
      else {
        new BadResponse
      }
    }
    */
    
    // POST document content
    /*
    case Document(doc) :: Nil Post req => {
      doc.content.setFromAny(new String(req.body.openTheBox, "UTF-8"));
      doc.save
      new OkResponse
    }
    */
    
    // DELETE document
    case Document(doc) :: Nil JsonDelete _ => JBool(doc.delete_!)
    
  })
 */ 
}