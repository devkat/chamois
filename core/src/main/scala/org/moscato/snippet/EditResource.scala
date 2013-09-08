package org.moscato.snippet

import net.liftweb.squerylrecord.RecordTypeMode._
import net.liftweb.http.S
import org.moscato.repo.MoscatoDb._
import org.moscato.repo.{Resource, Version, Path}
import java.util.UUID
import scala.xml.Text
import net.liftweb.common._
import org.moscato.repo.Path._
import net.liftweb.http.SHtml
import scala.xml.NodeSeq
import net.liftweb.http.FieldBinding
import net.liftweb.util.FieldContainer
import net.liftweb.http.FieldBinding._
import org.moscato.repo.Template
import net.liftweb.util.Html5
import net.devkat.lift.http.CssBoundBootstrapScreen

class EditResource extends CssBoundBootstrapScreen {
  
  def formName = "edit"

  object version extends ScreenVar[Box[Version]](Empty)
  
  /*
  def editableContent() =
    version.flatMap(_.html).map(_ \ "body" \ "_").getOrElse(<p>No content</p>)
      
  val content = textarea("", editableContent.toString, FieldBinding("content"))
   */
/*
  override def localSetup() {
    super.localSetup()
    val resource:Option[Resource] = S.param("url").map(Path.unapply(_)) match {
      case Full(url) => { Resource.findByUrl(url) match {
        case None => {
          S.notice("Resource " + url + " not found.")
          None
        }
        case r => r
      }}
      case Empty => None
      case Failure(msg, _, _) => {
        S.error(msg)
        None
      }
    }
    
    val v = for (r <- resource; v <- r.currentVersion) yield v
    version.set(v)
    
  }
  */
  def finish() {
    /*
    version foreach { v =>
      Html5.parse(content) foreach { html =>
        v.content.set(html.toString.getBytes("utf-8"))
      }
      versions.update(v)
    }
    */
  }
  
  override protected def redirectBack() = S.redirectTo(version.get.get.resource.href)

}