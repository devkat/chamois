package org.moscatocms

import org.moscatocms.rest.ResourcesRest
import net.liftweb.http.LiftRules
import net.liftweb.http.Req
import net.liftweb.http.Html5Properties
import net.liftweb.common.Full
import net.liftweb.sitemap.SiteMap
import net.liftweb.sitemap.Menu
import org.moscatocms.sitemap.ResourceLoc
import net.liftweb.http.LiftSession
import net.liftweb.util.Vendor

object Moscato {
  
  def init() {
    
    /*
    LiftRules.dispatch.append(MoscatoOpenIdVendor.dispatchPF)
    LiftRules.snippets.append(MoscatoOpenIdVendor.snippetPF)
     */
    
    List(ResourcesRest) foreach {
      LiftRules.statelessDispatch.append(_)
    }
    
    LiftRules.addToPackages("org.moscatocms")

    LiftRules.htmlProperties.default.set((r: Req) => new Html5Properties(r.userAgent))

    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

    //LiftRules.autoIncludeAjaxCalc.default.set(Vendor(() => (session: LiftSession) => false))
    
    /*
     * Show the spinny image when an Ajax call starts
     */
    LiftRules.ajaxStart =
      Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)

  }

  def sitemap = Seq(
      Menu("home", "Home") / "moscato" / "index",
      /*
      Menu("login", "Login") / "moscato" / "login" >> DefaultLogin >> RequireNoAuthentication,
      Menu("my_account", "My account") / "moscato" / "my" / "account" >> PlaceHolder >> RequireAuthentication submenus(
        (Menu("My profile") / "moscato" / "profile" >> RequireAuthentication) :: Shiro.menus
      ),
      //Menu("Role Test") / "restricted" >> RequireAuthentication >> HasRole("admin"),
      Menu("Sign up") / "moscato" / "signup" >> RequireNoAuthentication >> Loc.Hidden,
      //Menu(MercuryLoc),
      Menu("About") / "moscato" / "about" >> Hidden >> LocGroup("footer"),
       */
      Menu.i("Create resource") / "moscato" / "create",
      Menu.i("Edit resource") / "moscato" / "edit",
      Menu.i("Edit with Mercury") / "moscato" / "mercury",
      Menu.i("Import") / "moscato" / "import",
      Menu.i("Drawer") / "moscato" / "drawer",
      Menu(ResourceLoc) // >> RequireAuthentication
    )
  
}