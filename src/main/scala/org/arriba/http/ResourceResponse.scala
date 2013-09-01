package org.arriba.http

import net.liftweb.http.InMemoryResponse
import org.arriba.model.Version

object ResourceResponse {
  
  def response(v:Version) =
    InMemoryResponse(v.content.get, ("Content-Type", v.mediaType.toString) :: Nil, Nil, 200)
  
}