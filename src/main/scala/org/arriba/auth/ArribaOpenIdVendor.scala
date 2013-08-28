package org.arriba.auth

import net.liftmodules.openid.SimpleOpenIDVendor
import org.openid4java.discovery.DiscoveryInformation
import org.openid4java.message.AuthRequest
import net.liftmodules.openid._
import net.liftweb.common.Full

/**
 * https://www.assembla.com/spaces/liftweb/wiki/OpenID
 */
object ArribaOpenIdVendor extends SimpleOpenIDVendor {
  
  def ext(di: DiscoveryInformation, authReq: AuthRequest): Unit = { 
    import WellKnownAttributes._ 
    WellKnownEndpoints.findEndpoint(di) map {ep => 
      ep.makeAttributeExtension(List(Email, FullName, FirstName, LastName)) foreach {ex =>
        authReq.addExtension(ex)
      }
    } 
  }
  
  override def createAConsumer = new OpenIDConsumer[UserType] { 
    beforeAuth = Full(ext _) 
  }
  
}