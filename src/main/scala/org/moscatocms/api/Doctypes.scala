package org.moscatocms.api

import org.moscatocms.doctype.Doctype
import org.reflections.Reflections
import org.reflections.util.ClasspathHelper
import org.reflections.scanners.ResourcesScanner
import java.util.regex.Pattern
import org.moscatocms.doctype.DoctypeParser._
import scala.collection.JavaConverters._
import org.reflections.util.ConfigurationBuilder
import com.typesafe.scalalogging.LazyLogging

object Doctypes extends LazyLogging {
  
  val resourcePattern = Pattern.compile(".*\\.json")
  
  lazy val doctypes: Seq[Doctype] = {
    val configBuilder = new ConfigurationBuilder().
        setUrls(ClasspathHelper.forPackage("moscato.doctypes")).
        setScanners(new ResourcesScanner)
    val reflections = new Reflections(configBuilder)
    reflections.getResources(resourcePattern).
      asScala.map(loadDoctype _).
      toSeq
  }
  
  def loadDoctype(resource: String): Doctype = {
    logger.info(s"Loading doctype $resource")
    val json = io.Source.fromInputStream(getClass.getClassLoader.getResourceAsStream(resource)).mkString
    val definition = parse(json)
    val filename = resource.split("/").last
    val table = filename.split('.')(0)
    new Doctype(table, definition)
  }
  
}