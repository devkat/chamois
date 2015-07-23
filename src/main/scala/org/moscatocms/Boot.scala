package org.moscatocms;
 
import akka.actor.{ActorSystem, Props}
import akka.io.IO
import spray.can.Http
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import org.moscatocms.api.Api
 
object Boot extends App {

  implicit val system = ActorSystem("moscato")
  val service = system.actorOf(Props[Api], "moscato-rest-service")
 
  // IO requires an implicit ActorSystem, and ? requires an implicit timeout
  // Bind HTTP to the specified service.
  implicit val timeout = Timeout(5.seconds)
  IO(Http) ? Http.Bind(service, interface = "localhost", port = 8080)

}