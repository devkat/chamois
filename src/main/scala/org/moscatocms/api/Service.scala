package org.moscatocms.api

import akka.actor.Actor
import spray.routing._
import spray.http._
import MediaTypes._
import spray.httpx.SprayJsonSupport._
import spray.routing.authentication.BasicAuth
import org.moscatocms.security.Authentication._
import spray.routing.directives.FileAndResourceDirectives._
import org.moscatocms.domain.Users
import org.moscatocms.model.Tables.UserRow

// simple actor that handles the routes.
trait Service extends HttpService {
  
  import MoscatoJsonProtocol._
  
  def toUserDataWithId(user: UserRow) =
    UserDataWithId(user.id, user.username, user.email)
 
  lazy val route = {
    pathPrefix("cms") {
      path("") {
        getFromResource("org/moscatocms/static/index.html")
      } ~
      pathPrefix("api") {
        pathPrefix("v1") {
          pathPrefix("users") {
            get {
              complete(Users.list.map(_.map(toUserDataWithId _)))
            } ~
            post {
              entity(as[UserData]) { user =>
                complete(Users.add(user).map(toUserDataWithId _))
              }
            } ~
            path(LongNumber) { userId =>
              put {
                entity(as[UserData]) { user =>
                  complete {
                    Users.update(userId, user).
                      map(userId => Users.find(userId).map(toUserDataWithId _))
                  }
                }
              }
            }
          } ~ Routes()
        }
      } ~
      path("secured") {
        authenticate(BasicAuth(userPass _, realm = "moscato")) { userName =>
          complete(s"The user is '$userName'")
        }
      } ~
      pathPrefix("static") {
        getFromResourceDirectory("org/moscatocms/static")
      }
    }
  }
  
}

