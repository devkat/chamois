package org.arriba.comet

import net.liftweb._
import http._
import net.liftweb.common.{Box, Full}
import net.liftweb.util._
import net.liftweb.actor._
import net.liftweb.util.Helpers._
import scala.xml.Text
import net.liftweb.http.js.JsCmds.{SetHtml}

class CometLog extends CometActor {

  override def defaultPrefix = Full("comet")

  def render = bind("message" -> <span id="message">Whatever you feel like returning</span>)

  Schedule.schedule(this, LogMessage, 10000L)

  override def lowPriority : PartialFunction[Any,Unit] = {
    case LogMessage => {
      partialUpdate(SetHtml("message", Text("updated: " + now.toString)))
      Schedule.schedule(this, LogMessage, 10000L)
    }
  }
}
case object LogMessage

