package org.moscatocms.auth

import org.apache.shiro.authc.AuthenticationToken
import org.apache.shiro.authc.SimpleAuthenticationInfo
import org.apache.shiro.authc.UsernamePasswordToken
import org.apache.shiro.authz.AuthorizationInfo
import org.apache.shiro.authz.SimpleAuthorizationInfo
import org.apache.shiro.realm.AuthorizingRealm
import org.apache.shiro.subject.PrincipalCollection
import org.apache.shiro.util.ByteSource
import org.moscatocms.slick.Slick._
import org.moscatocms.model.Tables._
import slick.driver.PostgresDriver.simple._

class MoscatoRealm extends AuthorizingRealm {
  
  def doGetAuthenticationInfo(token:AuthenticationToken) = {
    val userToken = token.asInstanceOf[UsernamePasswordToken]
    val query = Users.filter(_.email === userToken.getUsername)
    
    db.run(query.firstOption) match {
      case Some(user) => {
        val info = new SimpleAuthenticationInfo(
            user.id,
            user.passwordHash.get.get,
            ByteSource.Util.bytes(user.passwordSalt.get.get),
            getName())
        info
      }
      case _ => null
    }
  }
  
  def doGetAuthorizationInfo(principals:PrincipalCollection) : AuthorizationInfo = {
    val userId = principals.fromRealm(getName()).iterator.next.asInstanceOf[Long]
    User.findById(userId) match {
      case Some(user) => {
        val info = new SimpleAuthorizationInfo
        /*
        for (Role role : user.getRoles()) {
            info.addRole(role.getName());
            info.addStringPermissions(role.getPermissions());
        }
        */
        return info;
      }
      case _ => null
    }
  }
  
}
