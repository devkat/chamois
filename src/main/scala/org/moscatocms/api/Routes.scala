package org.moscatocms.api

import scala.collection.JavaConverters._
import scala.reflect.runtime.universe
import spray.routing.Route
import spray.routing.RouteConcatenation
import org.reflections.Reflections
import collection.mutable._
import org.reflections.util.ClasspathHelper
import com.typesafe.scalalogging.LazyLogging
import org.reflections.util.ConfigurationBuilder
import org.reflections.scanners.SubTypesScanner

/**
 * Marker for Moscato routes.
 */
abstract class MoscatoRoute {
  def route: Route
}

object Routes extends RouteConcatenation with LazyLogging {

  def loadRoute(clazz: Class[_ <: MoscatoRoute]): Route = {
    logger.info("Loading route from class " + clazz.getName)
    val runtimeMirror = universe.runtimeMirror(getClass.getClassLoader)
    val module = runtimeMirror.staticModule(clazz.getName)
    val obj = runtimeMirror.reflectModule(module)
    obj.instance.asInstanceOf[MoscatoRoute].route
  }

  /**
   * Append these routes to an existing route.
   */
  def ~:(route: Route): Route = {
    val reflections = new Reflections(ClasspathHelper.forClassLoader, new SubTypesScanner)
    val routes = reflections.getSubTypesOf(classOf[MoscatoRoute]).asScala
    logger.info("Loaded routes " + routes.map(_.getSimpleName).mkString(", "))
    routes.map(loadRoute _).foldLeft[Route](route) { case (r1, r2) => r1 ~ r2 }
  }

}
