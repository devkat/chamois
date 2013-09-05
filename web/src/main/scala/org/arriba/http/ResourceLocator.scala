package org.arriba.http

import net.liftweb.http.ParsePath
import org.arriba.record.Resource
import net.liftweb.squerylrecord.RecordTypeMode._
import org.arriba.model.Path

object ResourceLocator {
  
  implicit def unapply(parsePath:ParsePath): Option[Resource] = inTransaction {
    parsePath match {
      case ParsePath(l, ext, true, _) => {
        val path:Path = l
        val r = Resource.findByUrl(path, ext)
        println("Resolved resource for path %s and extension %s: %s".format(path, ext, r))
        r
      }
    }
  }
  
}

