package org.chamois.sitemap

import net.liftweb.sitemap.Loc
import net.liftweb.common._
import org.chamois.model.Repository
import net.liftweb.http.RewriteRequest
import net.liftweb.http.ParsePath
import org.chamois.model.User
import net.liftweb.http.RewriteResponse
import scala.xml._
import org.chamois.snippet.Repositories
import net.liftweb.squerylrecord.RecordTypeMode._
import net.liftweb.util.Helpers._

abstract class RepositoryInfo
case object NoSuchRepository extends RepositoryInfo
case object NotPublic extends RepositoryInfo
case class FullRepositoryInfo(set : Repository) extends RepositoryInfo

object RepositoryLoc extends Loc[RepositoryInfo] {
  
  override def rewrite = Full(inTransaction {
    case RewriteRequest(ParsePath(List("repository", slug), "", true, false), _, _) => inTransaction {
      val repoInfo = Repository.findBySlug(slug) match {
        case Some(Repository) => FullRepositoryInfo(Repository)
        case _ => NoSuchRepository
      }
      (RewriteResponse("repository" :: Nil), repoInfo)
    }
    /*
    case RewriteRequest(ParsePath(List("set", Repository(Repository)), "", true, false), _, _) => {
         if (Repository.isPublic.is || Repository.ownerId == User.currentUserId)
             (RewriteResponse("set" :: Nil), FullRepositoryInfo(Repository))
           else
             (RewriteResponse("set" :: Nil), NoSuchRepository)
    }
       */
  })
  
  override def snippets = {
  case ("show", Full(NoSuchRepository)) => {ignore: NodeSeq =>
    Text("Repository not found.")}
  case ("show", Full(FullRepositoryInfo(repo))) =>
    Repositories.show(repo) _
  }
  
  override def params = Nil
  
  override def defaultValue = Empty
  
  override def text = new Loc.LinkText[RepositoryInfo](_ => Nil)
  
  override def link = new Loc.Link[RepositoryInfo](List("repository"), false) {
    override def createLink(info: RepositoryInfo) =
      info match {
      case FullRepositoryInfo(repo) => Full(Text(calcHref(info)))
      case _ => Empty
    }
  }
  
  override def calcHref(info:RepositoryInfo) =
    info match {
      case FullRepositoryInfo(repo) => "/repository/" + urlEncode(repo.slug.is)
    }
  
  override def name = "repository"

}

