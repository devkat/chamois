package org.moscatocms.api

import org.moscatocms.model.Tables._
import spray.json._
import java.sql.Timestamp

object MoscatoJsonProtocol extends DefaultJsonProtocol {

  implicit object TimestampJsonFormat extends RootJsonFormat[Timestamp] {
    def write(t: Timestamp) = JsString(t.toString)

    def read(value: JsValue) = value match {
      case JsString(v) => Timestamp.valueOf(v)
      case _ => deserializationError("String expected")
    }
  }
  
  implicit val UserFormat = jsonFormat2(UserData)
  
  /*
  implicit object UserFormat extends RootJsonFormat[UserData] {
    def write(user: UserData) = JsObject(
      "email" -> JsString(user.email)
    )
    def read(value: JsValue) = {
      value.asJsObject.getFields("email") match {
        case Seq(JsString(email)) => UserData(email)
        case _ => throw new DeserializationException("User expected")
      }
    }
  }
  */

}