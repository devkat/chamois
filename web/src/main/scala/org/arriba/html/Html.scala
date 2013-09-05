package org.arriba.html

import org.arriba.record.Version
import net.liftweb.util.Html5
import java.io.ByteArrayInputStream

object Html {

  def html(v:Version) =
    Html5.parse(new ByteArrayInputStream(v.content.get))

}