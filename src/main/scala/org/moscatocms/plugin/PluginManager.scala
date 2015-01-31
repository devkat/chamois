package org.moscatocms.plugin

import org.clapper.classutil.ClassFinder
import net.liftweb.common.Logger
import java.io.File

object PluginManager extends Logger {

  lazy val pluginMap = {
    val classpath = List(".").map(new File(_))
    val finder = ClassFinder(classpath)
    val classes = finder.getClasses
    val classMap = ClassFinder.classInfoMap(classes)
    val plugins = ClassFinder.concreteSubclasses("org.moscatocms.plugin.MoscatoPlugin", classMap)
 
    plugins.map {
      pluginClassInfo =>
        val plugin = Class.forName(pluginClassInfo.name).newInstance().asInstanceOf[MoscatoPlugin]
        (pluginClassInfo.name -> plugin)
    } toMap
  }
 
  def init() {
    pluginMap.values foreach (_.init())
  }
 
  def getPlugin(name: String) = pluginMap.get(name)
}