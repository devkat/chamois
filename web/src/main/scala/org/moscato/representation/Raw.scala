package org.moscato.representation

import scala.xml.Elem

object Raw extends Representation {
  
  def name = Some("raw")
  
  def template(html:Elem) = html

}