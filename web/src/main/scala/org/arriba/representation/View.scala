package org.arriba.representation

import net.liftweb.common.Box
import scala.xml.Node
import net.liftweb.util._
import net.liftweb.util.Helpers._
import scala.xml.NodeSeq
import scala.xml.Elem

object View extends Representation {

  def name = None
  
  def template(html:Elem): Node = insertDrawer(html).head

  lazy val insertDrawer = "body" #> { n:NodeSeq =>
    <body>
      <div class="drawer" id="arriba-drawer">
        <div data-lift="embed?what=body"/>
        {n \ "_"}
      </div>
    </body>
  }
  
}