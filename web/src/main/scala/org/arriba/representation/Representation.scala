package org.arriba.representation

import scala.xml.Elem
import scala.xml.Node

trait Representation {

  def name:Option[String]
  
  def template(html:Elem): Node

}