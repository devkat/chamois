package org.moscatocms.api

case class UserDataWithId(id: Long, username: String, email: String)

case class UserData(username: String, email: String)