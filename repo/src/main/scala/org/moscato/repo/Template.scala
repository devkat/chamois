package org.moscato.repo

case class TemplateVersion(val mediaType: MediaType, val content: Array[Byte])

case class Template(val name:String, version: Option[TemplateVersion] = None)
