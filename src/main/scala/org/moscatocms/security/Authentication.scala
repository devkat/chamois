package org.moscatocms.security

import spray.routing.authentication.UserPass
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import org.moscatocms.model.Tables.{User, UserRow}
import org.apache.shiro.authc.UsernamePasswordToken
import slick.driver.PostgresDriver.api._
import org.moscatocms.db.Db._
import org.apache.shiro.authc.SimpleAuthenticationInfo
import org.apache.shiro.util.ByteSource
import org.apache.shiro.authc.credential.HashedCredentialsMatcher
import org.apache.shiro.crypto.hash.Sha512Hash

object Authentication {
  
  implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global
  
  def passwordMatch(user: UserRow, password: String): Boolean = {
    for {
      hash <- user.password
      salt <- user.salt
    } yield {
      val matcher = new HashedCredentialsMatcher(Sha512Hash.ALGORITHM_NAME)
      matcher.setStoredCredentialsHexEncoded(false)
      matcher.setHashIterations(1024)
      val result = matcher.doCredentialsMatch(
          new UsernamePasswordToken(user.username, password),
          new SimpleAuthenticationInfo(user.username, hash, ByteSource.Util.bytes(salt), "moscato"))
      println("CHecking " + hash + " " + salt + " " + password + " > " + result)
      result
    }
  } getOrElse false
  
  def authenticateUser(username: String, password: String): Future[Option[UserRow]] = {
    val query = User.filter(_.username === username)
    val user = db.run(query.result.headOption)
    user.map { _ match {
      case Some(user) if passwordMatch(user, password) => Some(user)
      case _ => None
    }}
  }
  
  def userPass(userPass: Option[UserPass]): Future[Option[String]] =
    userPass match {
      case Some(user) => authenticateUser(user.user, user.pass) map { _ map { _.username }}
      case _ => Future(None)
    }

}