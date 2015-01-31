package org.moscatocms.importer

import java.io.File
import java.io.FilenameFilter
import org.moscatocms.repo._
import org.moscatocms.repo.MoscatoDb._
import org.apache.tika.Tika

object HtmlFileImporter extends FileImporter {

  lazy val tika = new Tika

  def nameAndExt(name: String) = name.lastIndexOf(".") match {
    case -1 => (name, "")
    case index @ _ => (name.substring(0, index), name.substring(index + 1))
  }

  def importContent(file: File) {
    importResourcesInDir()(file)

  }

  def importResourcesInDir(parent:Option[Long] = None)(f: File) {
    f.listFiles(new FilenameFilter {
      override def accept(f: File, n: String) = !n.startsWith(".")
    }) foreach importResources(parent) _
  }

  def importResources(folderId: Option[Long] = None)(f: File) {
    print("Importing " + f)
    
    /*
    def setSlug(slug:String) {
      Resource.findBySlug(slug, parent) match {
        case None => r.slug.set(slug)
        case Some(resource) => setSlug(slug + "_")
      }
    }
    
    setSlug(slug)
    
    r.slug.set(f.getName)
     */
    val (slug, ext) = nameAndExt(f.getName)
    if (f.isDirectory()) {
      println
      val folder = Folder.createRecord
      folder.slug.set(slug)
      folder.parentId.set(folderId)
      folders.insert(folder)
      importResourcesInDir(Some(folder.id))(f)
    } else {
      MediaType.parse(tika.detect(f)) match {
        case Some(mediaType) => {
          
          val overriddenMediaType = ext match {
            case "js" => MediaType("application", "javascript")
            case _ => mediaType
          }
          println(" -> detected %s, using %s".format(mediaType, overriddenMediaType))
          
          val r = Resource.createRecord
          r.folderId.set(folderId)
          r.slug.set(slug)
          r.name.set(f.getName)
          r.setMediaType(overriddenMediaType)
          r.newVersion(read(f))
          resources.insert(r)
        }
        case None => {
          println("Could not detect media type of file " + f)
        }
      }
    }
  }

}