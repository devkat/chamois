package org.moscatocms.slick

import scala.slick.driver.PostgresDriver.simple._

object Slick {
  
  var db:Database = null
  
  def init(url: String, user: String, password: String) {
    db = Database.forURL(url, user, password)
  }
  
}