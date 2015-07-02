package org.moscatocms.api

import akka.actor.Actor
import spray.routing._
import spray.http._
import MediaTypes._
import spray.httpx.SprayJsonSupport._
import spray.routing.authentication.BasicAuth
import org.moscatocms.security.Authentication._
import spray.routing.directives.FileAndResourceDirectives._

// simple actor that handles the routes.
trait Service extends HttpService {
 
  val route = {
    pathPrefix("cms") {
      path("") {
        getFromFile("static/index.html")
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