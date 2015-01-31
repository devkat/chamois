package org.moscatocms.auth

import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.sitemap._
import net.liftweb.sitemap.Loc._
import net.liftweb.util._
import net.liftweb.util.Helpers._
import scala.xml._
import scala.xml.transform._
import org.moscatocms.repo.User

class CurrentUser(user:User) {
  /*
  lazy val entityManager = Model.newEM
  
  type TheUserType = User

  private object curUserId extends SessionVar[Box[String]](Empty) {
    override lazy val __nameSalt = Helpers.nextFuncName
  }

  def currentUserId: Box[String] = curUserId.is

  private object curUser extends RequestVar[Box[TheUserType]](currentUserId.flatMap(userFromStringId))  with CleanRequestVarOnSessionTransition  {
    override lazy val __nameSalt = Helpers.nextFuncName
  }

  /**
   * Given a String representing the User ID, find the user
   */
  protected def userFromStringId(id: String): Box[TheUserType]

  def currentUser: Box[TheUserType] = curUser.is

  /**
   * By default, destroy the session on login.
   * Change this is some of the session information needs to
   * be preserved.
   */
  protected def destroySessionOnLogin = true

  /**
   * The application's home page
   */
  def homePage = "/"
  
  var onLogIn: List[TheUserType => Unit] = Nil

  var onLogOut: List[Box[TheUserType] => Unit] = Nil

  /**
   * Insert this LocParam into your menu if you want the
   * User's menu items to be children of that menu
   */
  final case object AddUserMenusUnder extends Loc.LocParam[Any]

  private lazy val UnderUnapply = SiteMap.buildMenuMatcher(_ == AddUserMenusUnder)

  /**
   * The base path for the user related URLs.  Override this
   * method to change the base path
   */
  def basePath: List[String] = "user_mgt" :: Nil

  /**
   * Calculate the path given a suffix by prepending the basePath to the suffix
   */
  protected def thePath(end: String): List[String] = basePath ::: List(end)

  /**
   * The path suffix for the login screen
   */
  def loginSuffix = "login"

  /**
   * The computed path for the login screen
   */
  lazy val loginPath = thePath(loginSuffix)
  
  /**
   * The path suffix for the logout screen
   */
  def logoutSuffix = "logout"

  /**
   * The computed pat for logout
   */
  lazy val logoutPath = thePath(logoutSuffix)


    /**
   * The SiteMap mutator function
   */
  def sitemapMutator: SiteMap => SiteMap = SiteMap.sitemapMutator {
    case UnderUnapply(menu) => List(menu.rebuild(_ ::: sitemap))
  }(SiteMap.addMenusAtEndMutator(sitemap))

  lazy val sitemap: List[Menu] =
  List(loginMenuLoc,
       createUserMenuLoc,
       editUserMenuLoc,
       validateUserMenuLoc,
       logoutMenuLoc).flatten(a => a)
       
  /**
   * The menu item for login (make this "Empty" to disable)
   */
  def loginMenuLoc: Box[Menu] =
    Full(Menu(Loc("Login", loginPath, S.?("login"), loginMenuLocParams ::: globalUserLocParams)))
    
  /**
   * If you want to include a LocParam (e.g. LocGroup) on all the
   * User menus, add them here
   */
  protected def globalUserLocParams: List[LocParam[Unit]] = Nil

  /**
   * The LocParams for the menu item for login.
   * Overwrite in order to add custom LocParams. Attention: Not calling super will change the default behavior!
   */
  protected def loginMenuLocParams: List[LocParam[Unit]] =
    If (notLoggedIn_? _, S.?("already.logged.in")) ::
    Template(() => wrapIt(login)) ::
    Nil

  protected def notLoggedIn_? = !user.loggedIn_?
    
  /**
   * The menu item for creating the user/sign up (make this "Empty" to disable)
   */
  def createUserMenuLoc: Box[Menu] =
    Full(Menu(Loc("CreateUser", signUpPath, S.?("sign.up"), createUserMenuLocParams ::: globalUserLocParams)))

  /**
   * The LocParams for the menu item for creating the user/sign up.
   * Overwrite in order to add custom LocParams. Attention: Not calling super will change the default behavior!
   */
  protected def createUserMenuLocParams: List[LocParam[Unit]] =
    Template(() => wrapIt(signupFunc.map(_()) openOr signup)) ::
    If(notLoggedIn_? _, S.?("logout.first")) ::
    Nil

  /**
   * The menu item for editing the user (make this "Empty" to disable)
   */
  def editUserMenuLoc: Box[Menu] =
    Full(Menu(Loc("EditUser", editPath, S.?("edit.user"), editUserMenuLocParams ::: globalUserLocParams)))

  /**
   * The LocParams for the menu item for editing the user.
   * Overwrite in order to add custom LocParams. Attention: Not calling super will change the default behavior!
   */
  protected def editUserMenuLocParams: List[LocParam[Unit]] =
    Template(() => wrapIt(editFunc.map(_()) openOr edit)) ::
    testLogginIn ::
    Nil

  /**
   * The menu item for validating a user (make this "Empty" to disable)
   */
  def validateUserMenuLoc: Box[Menu] =
    Full(Menu(Loc("ValidateUser", (validateUserPath, true), S.?("validate.user"), validateUserMenuLocParams ::: globalUserLocParams)))

  /**
   * The LocParams for the menu item for validating a user.
   * Overwrite in order to add custom LocParams. Attention: Not calling super will change the default behavior!
   */
  protected def validateUserMenuLocParams: List[LocParam[Unit]] =
    Hidden ::
    Template(() => wrapIt(validateUser(snarfLastItem))) ::
    If(notLoggedIn_? _, S.?("logout.first")) ::
    Nil

  /**
   * The menu item for logout (make this "Empty" to disable)
   */
  def logoutMenuLoc: Box[Menu] =
    Full(Menu(Loc("Logout", logoutPath, S.?("logout"), logoutMenuLocParams ::: globalUserLocParams)))

  /**
   * The LocParams for the menu item for logout.
   * Overwrite in order to add custom LocParams. Attention: Not calling super will change the default behavior!
   */
  protected def logoutMenuLocParams: List[LocParam[Unit]] =
    Template(() => wrapIt(logout)) ::
    testLogginIn ::
    Nil

  def login = {
  }

  def logout = {
    logoutCurrentUser
    S.redirectTo(homePage)
  }

  /**
   * This function is given a chance to log in a user
   * programmatically when needed
   */
  var autologinFunc: Box[()=>Unit] = Empty

  def loggedIn_? = {
    if(!currentUserId.isDefined)
      for(f <- autologinFunc) f()
    currentUserId.isDefined
  }

  def logUserIdIn(id: String) {
    curUser.remove()
    curUserId(Full(id))
  }

  def logUserIn(who: TheUserType, postLogin: () => Nothing): Nothing = {
    if (destroySessionOnLogin) {
      S.session.open_!.destroySessionAndContinueInNewSession(() => {
        logUserIn(who)
        postLogin()
      })
    } else {
      logUserIn(who)
      postLogin()
    }
  }

  def logUserIn(who: TheUserType) {
    curUserId.remove()
    curUser.remove()
    curUserId(Full(who.userIdAsString))
    curUser(Full(who))
    onLogIn.foreach(_(who))
  }

  def logoutCurrentUser = logUserOut()

  def logUserOut() {
    curUserId.remove()
    curUser.remove()
    S.session.foreach(_.destroySession())
  }

  /**
   * What template are you going to wrap the various nodes in
   */
  def screenWrap: Box[Node] = Empty

  protected def wrapIt(in: NodeSeq): NodeSeq =
      screenWrap.map(new RuleTransformer(new RewriteRule {
        override def transform(n: Node) = n match {
          case e: Elem if "bind" == e.label && "lift" == e.prefix => in
          case _ => n
        }
      })) openOr in

  /**
   * If there's any state that you want to capture pre-login
   * to be set post-login (the session is destroyed),
   * then set the state here.  Just make a function
   * that captures the state... that function will be applied
   * post login.
   */
  protected def capturePreLoginState(): () => Unit = () => {}

  /**
   * The path suffix for the edit screen
   */
  def editSuffix = "edit"

  object editFunc extends RequestVar[Box[() => NodeSeq]](Empty) {
    override lazy val __nameSalt = Helpers.nextFuncName
  }

  /**
   * The computed path for the edit screen
   */
  lazy val editPath = thePath(editSuffix)

  /**
   * If there's any mutation to do to the user on retrieval for
   * editting, override this method and mutate the user.  This can
   * be used to pull query parameters from the request and assign
   * certain fields. Issue #722
   *
   * @param user the user to mutate
   * @return the mutated user
   */
  protected def mutateUserOnEdit(user: TheUserType): TheUserType = user
  
  def edit = {
    val theUser: TheUserType = 
      mutateUserOnEdit(currentUser.open_!) // we know we're logged in

    val theName = editPath.mkString("")

    def testEdit() {
      theUser.validate match {
        case Nil =>
          entityManager.persist(theUser)
          S.notice(S.?("profile.updated"))
          S.redirectTo(homePage)

        case xs => S.error(xs) ; editFunc(Full(innerEdit _))
      }
    }

    def editXhtml(user: TheUserType) = {
        (<form method="post" action={S.uri}>
        <table><tr><td colspan="2">{S.?("edit")}</td></tr>
          {localForm(user, true)}
          <tr><td>&nbsp;</td><td><user:submit/></td></tr>
        </table>
     </form>)
    }
    
    def innerEdit = bind("user", editXhtml(theUser),
                         "submit" -> editSubmitButton(S.?("save"), testEdit _))
                         
    innerEdit
  }

  def editSubmitButton(name: String, func: () => Any = () => {}): NodeSeq = {
    standardSubmitButton(name, func)
  }

  protected def localForm(user: TheUserType, ignorePassword: Boolean): NodeSeq = {
    for {
      field <- List("email", "name")
    } yield <tr><td>{field}</td><td>{field}</td></tr>
  }

  def standardSubmitButton(name: String,  func: () => Any = () => {}) = {
    SHtml.submit(name, func)
  }
*/
}