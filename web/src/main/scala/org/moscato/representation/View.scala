package org.moscato.representation

import net.liftweb.common.Box
import scala.xml.Node
import net.liftweb.util._
import net.liftweb.util.Helpers._
import scala.xml.NodeSeq
import scala.xml.Elem
import net.liftweb.http.TemplateFinder

object View extends Representation {

  def name = None
  
  def template(html:Elem): Node = TemplateFinder(List("templates-hidden", "view")).get.head
  
  //def template(html:Elem): Node = insertDrawer(html).head

  lazy val insertDrawer = "body" #> { n:NodeSeq =>
    <body>
      <div class="drawer" id="moscato-drawer">
        <div data-lift="embed?what=body"/>
        {n \ "_"}
      </div>
    </body>
  }
  
}