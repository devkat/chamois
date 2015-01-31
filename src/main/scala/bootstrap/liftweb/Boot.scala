package bootstrap.liftweb

import net.liftweb.util._
import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.http.provider._
import net.liftweb.sitemap._
import net.liftweb.sitemap.Loc._
import Helpers._
import java.sql.{ Connection, DriverManager }
import org.moscatocms.repo._
import shiro.Shiro
import shiro.sitemap.Locs._
import net.liftweb.squerylrecord.RecordTypeMode._
import net.liftweb.squerylrecord.SquerylRecord
import org.squeryl.Session
import org.squeryl.adapters.PostgreSqlAdapter
import java.util.Locale
import org.moscatocms.auth.MoscatoOpenIdVendor
import org.moscatocms.sitemap._
import net.liftweb.sitemap.Menu.Menuable
import net.liftweb.sitemap.Loc.MenuCssClass
import org.moscatocms.rest._
import org.moscatocms.Moscato

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot {
  def boot {

    Class.forName("org.postgresql.Driver")
    for {
      url <- Props.get("db.url")
      user <- Props.get("db.user")
      password <- Props.get("db.password")
    } {
      SquerylRecord.initWithSquerylSession(Session.create(
        DriverManager.getConnection(url, user, password),
        new PostgreSqlAdapter))
      S.addAround(SquerylRecord.buildLoanWrapper)
    }
    
    LiftRules.localeCalculator = (_) => Locale.ENGLISH
    /*
    LiftRules.dispatch.append(MoscatoOpenIdVendor.dispatchPF)
    LiftRules.snippets.append(MoscatoOpenIdVendor.snippetPF)
    
    val staticFiles: PartialFunction[Req, Boolean] = {
      case Req("static" :: _, _, _) => false
    }
    LiftRules.liftRequest.append(staticFiles)
    */

    Shiro.init()

    //LiftRules.loggedInTest = Full(() => User.loggedIn_?)

    //LiftRules.addToPackages("eu.getintheloop") // Shiro
    LiftRules.addToPackages("shiro")
    println("init...")
    Moscato.init()
    
    LiftRules.setSiteMap(SiteMap(Moscato.sitemap:_*))
    

    //LiftRules.loggedInTest = Full(() => User.loggedIn_?)

    /*
    ResourceServer.allow {
      case "dojo" :: tail => true
      case "dijit" :: tail => true
      case "dojox" :: tail => true
      case "dgrid" :: tail => true
      case "xstyle" :: tail => true
      case "put-selector" :: tail => true
      case "fugue-icons-3.4.1" :: tail => true
    }
    */
    
  }

}
