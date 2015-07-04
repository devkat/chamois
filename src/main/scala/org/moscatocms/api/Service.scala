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
  
  def toUserData(user: UserRow) = UserData(user.username, user.email)
 
  val route = {
    pathPrefix("cms") {
      path("") {
        getFromFile("static/index.html")
      } ~
      pathPrefix("api") {
        pathPrefix("v1") {
          pathPrefix("users") {
            get {
              complete(Users.list.map(_.map(toUserData _)))
            } ~
            post {
              entity(as[UserData]) { user =>
                complete(Users.add(user).map(toUserData _))
              }
            }
          }
        }
      } ~
      path("secured") {
        authenticate(BasicAuth(userPass _, realm = "moscato")) { userName =>
          complete(s"The user is '$userName'")
        }
      } ~
      pathPrefix("static") {
        getFromDirectory("static")
      }
    }
  }
  
}

