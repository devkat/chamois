package org.moscatocms.domain

import slick.driver.PostgresDriver.api._
import org.moscatocms.db.Db._
import org.moscatocms.model.Tables._
import org.moscatocms.api.UserData

object Users {
  def list() = {
    db.run(User.map(u => u).result)
  }

  def add(userData: UserData) = {
    val query = User.map(u => (u.username, u.email)) returning User +=
      (userData.username, userData.email)
    db.run(query)
  }
}