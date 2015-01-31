package org.moscatocms.representation
import scala.xml.Node
import net.liftweb.util._
import net.liftweb.util.Helpers._
import scala.xml.NodeSeq
import scala.xml.Elem
import net.liftweb.http.TemplateFinder
import net.liftweb.common.Box.box2Option

object View extends Representation {

  def name = ""
  
  def template(html:Elem): Node = TemplateFinder(List("templates-hidden", "moscato", "view")).get.head
  
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