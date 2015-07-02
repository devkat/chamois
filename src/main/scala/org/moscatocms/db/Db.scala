package org.moscatocms.db

import slick.driver.PostgresDriver.api._

object Db {
  
  val db = Database.forURL("jdbc:postgresql:moscato");

}