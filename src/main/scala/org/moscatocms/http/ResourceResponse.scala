package org.moscatocms.http

import net.liftweb.http.InMemoryResponse
import org.moscatocms.repo.Version

object ResourceResponse {
  
  def response(v:Version) =
    InMemoryResponse(v.content.get, ("Content-Type", v.resource.mediaType.toString) :: Nil, Nil, 200)
  
}