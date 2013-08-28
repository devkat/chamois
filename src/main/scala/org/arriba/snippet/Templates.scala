package org.arriba.snippet

import scala.xml.NodeSeq
import org.arriba.content.MediaType
import MediaType._
import scala.xml.Elem
import net.liftweb.util.Html5
import org.arriba.content.DefaultTemplates

object Templates {

  implicit def string2bytes(s:String) = s.getBytes("utf-8")
  implicit def html2bytes(n:Elem) = string2bytes(Html5.toString(n))
  
  // TODO include user templates
  def templates = DefaultTemplates.templates

  def radioButtons(n:NodeSeq): NodeSeq =
    templates map { case (key, t) =>
      <div class="radio">
        <label>
          <input type="radio" name="template" value={key}/>
          {t.name}
        </label>
      </div>
    } toList

  def get(k: String) = templates(k)
    
}