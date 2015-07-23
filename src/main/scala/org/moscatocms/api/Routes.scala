package org.moscatocms.api

import scala.collection.JavaConverters._
import scala.reflect.runtime.universe
import spray.routing.Route
import spray.routing.RouteConcatenation
import org.reflections.Reflections
import collection.mutable._
import org.reflections.util.ClasspathHelper

/**
 * Marker for Moscato routes.
 */
class MoscatoRoute

object Routes extends RouteConcatenation {
  
  private var route: Option[Route] = None
  
  def +=(r: Route) {
    route = route match {
      case None => Some(r)
      case route => {
        println("Adding route")
        route.map(_ ~ r)
      }
    }
  }
  
  def apply() = route.getOrElse(throw new RuntimeException("Empty routes"))

  def loadRoute(clazz: Class[_ <: MoscatoRoute]) {
    val runtimeMirror = universe.runtimeMirror(getClass.getClassLoader)
    val module = runtimeMirror.staticModule(clazz.getName)
    val obj = runtimeMirror.reflectModule(module)
  }

  def loadRoutes() {
    val reflections = new Reflections("")
    val routes = reflections.getSubTypesOf(classOf[MoscatoRoute])
    routes.asScala.foreach(loadRoute _)
  }

  loadRoutes()

}
