package org.moscato.representation

import scala.xml.Elem
import scala.xml.Node

trait Representation {

  def name:String
  
  def template(html:Elem): Node

}

object Representation {
  
  val reps = scala.collection.mutable.Map.empty[String, Representation]
  
  def register(rep:Representation) {
    reps += (rep.name -> rep)
  }
  
}