package org.moscato.representation

import scala.xml.Elem
import scala.xml.Node
import net.liftweb.util._
import net.liftweb.util.Helpers._
import scala.xml.NodeSeq

object Mercury extends Representation {
  
  def name = Some("mercury")

  def template(html:Elem): Node = insertDrawer(html).head

  lazy val insertDrawer = "body" #> { n:NodeSeq =>
    <body>
      <div data-lift="embed?what=mercury"/>
      <div data-editable="true" data-mercury="full" id="content">
        {n \ "_"}
      </div>
    </body>
  } andThen
    "script" #> NodeSeq.Empty
  
}