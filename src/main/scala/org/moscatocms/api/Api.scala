package org.moscatocms.api

import akka.actor._
import spray._
import routing._
import http.{StatusCodes, HttpResponse}
import akka.util.Timeout
import scala.concurrent.ExecutionContext

class Api extends Actor with RouteConcatenation with Service {
  
  // required as implicit value for the HttpService
  // included from SJService
   def actorRefFactory = context.system

  // we don't create a receive function ourselves, but use
  // the runRoute function from the HttpService to create
  // one for us, based on the supplied routes.
  def receive = runRoute(route)

}