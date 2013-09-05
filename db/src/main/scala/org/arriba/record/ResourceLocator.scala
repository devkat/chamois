package org.arriba.http

import net.liftweb.http.ParsePath
import org.arriba.record.Resource
import net.liftweb.squerylrecord.RecordTypeMode._
import org.arriba.model.Path

object ResourceLocator {

  implicit def unapply(parsePath: ParsePath): Option[Resource] = inTransaction {
    parsePath match {
      case ParsePath(path, ext, true, _) if path.length > 0 => {
        val folderPath = path.take(path.length - 1)
        val name = path.last
        val (slug, extension) = ext match {
          case "" =>
            name.lastIndexOf('.') match {
              case -1 => (name, "")
              case i => (name.substring(0, i), name.substring(i))
            }
          case e => (name, e)
        }
        val r = Resource.findByUrl(folderPath, slug, extension)
        println("Resolved resource for folder %s, slug %s, extension %s: %s".format(folderPath, slug, extension, r))
        r
      }
    }
  }

}

