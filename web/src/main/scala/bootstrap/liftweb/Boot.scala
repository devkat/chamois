package bootstrap.liftweb

import net.liftweb.util._
import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.http.provider._
import net.liftweb.sitemap._
import net.liftweb.sitemap.Loc._
import Helpers._
import java.sql.{ Connection, DriverManager }
import org.arriba.model._
import shiro.Shiro
import shiro.sitemap.Locs._
import net.liftweb.squerylrecord.RecordTypeMode._
import net.liftweb.squerylrecord.SquerylRecord
import org.squeryl.Session
import org.squeryl.adapters.PostgreSqlAdapter
import java.util.Locale
import org.arriba.auth.ArribaOpenIdVendor
import org.arriba.sitemap._
import net.liftweb.sitemap.Menu.Menuable
import net.liftweb.sitemap.Loc.MenuCssClass
import org.arriba.rest._

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot {
  def boot {

    Class.forName("org.postgresql.Driver")
    SquerylRecord.initWithSquerylSession(Session.create(
      DriverManager.getConnection(Props.get("db.url").get, Props.get("db.user").get, Props.get("db.password").get),
      new PostgreSqlAdapter))
    S.addAround(SquerylRecord.buildLoanWrapper)
    
    LiftRules.resourceNames = "iconhub" :: Nil
    LiftRules.localeCalculator = (_) => Locale.ENGLISH
    LiftRules.dispatch.append(ArribaOpenIdVendor.dispatchPF)
    LiftRules.snippets.append(ArribaOpenIdVendor.snippetPF)
    
    List(ResourcesRest, MercuryRest) foreach {
      LiftRules.statelessDispatch.append(_)
    }
    
    val staticFiles: PartialFunction[Req, Boolean] = {
      case Req("static" :: _, _, _) => false
    }
    LiftRules.liftRequest.append(staticFiles)

    Shiro.init()

    LiftRules.htmlProperties.default.set((r: Req) => new Html5Properties(r.userAgent))
    //LiftRules.loggedInTest = Full(() => User.loggedIn_?)

    //LiftRules.addToPackages("eu.getintheloop") // Shiro
    LiftRules.addToPackages("org.arriba")
    LiftRules.addToPackages("shiro")
    
    val items = List[ConvertableToMenu](
      Menu("home", "Home") / "arriba" / "index" >> DefaultLogin >> Loc.Hidden,
      /*
      Menu("login", "Login") / "arriba" / "login" >> DefaultLogin >> RequireNoAuthentication,
      Menu("my_account", "My account") / "arriba" / "my" / "account" >> PlaceHolder >> RequireAuthentication submenus(
        (Menu("My profile") / "arriba" / "profile" >> RequireAuthentication) :: Shiro.menus
      ),
      //Menu("Role Test") / "restricted" >> RequireAuthentication >> HasRole("admin"),
      Menu("Sign up") / "arriba" / "signup" >> RequireNoAuthentication >> Loc.Hidden,
      //Menu(MercuryLoc),
      Menu("About") / "arriba" / "about" >> Hidden >> LocGroup("footer"),
       */
      Menu.i("Create resource") / "arriba" / "create",
      Menu.i("Edit resource") / "arriba" / "edit",
      Menu.i("Edit with Mercury") / "arriba" / "mercury",
      Menu.i("Import") / "arriba" / "import",
      Menu(ResourceLoc) // >> RequireAuthentication
    )
    
    def addCssClass(c:ConvertableToMenu) = c match {
      case m:Menuable => m >> MenuCssClass("menu-item-" + m.name)
      case _ => c
    }

    LiftRules.setSiteMap(SiteMap(items map addCssClass _ : _*))
    
    /*
     * Show the spinny image when an Ajax call starts
    LiftRules.ajaxStart =
      Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)
     */

    /*
     * Make the spinny image go away when it ends
    LiftRules.ajaxEnd =
      Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)
     */

    LiftRules.early.append(makeUtf8)

    //LiftRules.loggedInTest = Full(() => User.loggedIn_?)

    ResourceServer.allow {
      case "dojo" :: tail => true
      case "dijit" :: tail => true
      case "dojox" :: tail => true
      case "dgrid" :: tail => true
      case "xstyle" :: tail => true
      case "put-selector" :: tail => true
      case "fugue-icons-3.4.1" :: tail => true
    }

  }

  /**
   * Force the request to be UTF-8
   */
  private def makeUtf8(req: HTTPRequest) {
    req.setCharacterEncoding("UTF-8")
  }
}
