package dal

import javax.inject.{ Inject, Singleton }
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import models.User

import scala.concurrent.{ Future, ExecutionContext }

/**
 * A repository for people.
 *
 * @param dbConfigProvider The Play db config provider. Play will inject this for you.
 */
@Singleton
class UserRepository @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  // We want the JdbcProfile for this provider
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  // These imports are important, the first one brings db into scope, which will let you do the actual db operations.
  // The second one brings the Slick DSL into scope, which lets you define the table and other queries.
  import dbConfig._
  import driver.api._

  /**
   * Here we define the table. It will have a name of people
   */
  private class UserTable(tag: Tag) extends Table[User](tag, "people") {

    /** The ID column, which is the primary key, and auto incremented */
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def email = column[String]("email")

    def password = column[String]("password")

    /**
     * This is the tables default "projection".
     *
     * It defines how the columns are converted to and from the User object.
     *
     * In this case, we are simply passing the id, name and page parameters to the User case classes
     * apply and unapply methods.
     */
    def * = (id, email, password) <> ((User.apply _).tupled, User.unapply)
  }

  /**
   * The starting point for all queries on the people table.
   */
  private val users = TableQuery[UserTable]

  /**
   * Create a User with the given name and age.
   *
   * This is an asynchronous operation, it will return a future of the created User, which can be used to obtain the
   * id for that User.
   */
  def create(email: String, password: String): Future[User] = db.run {
    // We create a projection of just the name and age columns, since we're not inserting a value for the id column
    (users.map(p => (p.email, p.password))
      // Now define it to return the id, because we want to know what id was generated for the User
      returning users.map(_.id)
      // And we define a transformation for the returned value, which combines our original parameters with the
      // returned id
      into ((userData, id) => User(id, userData._1, userData._2))
    // And finally, insert the User into the database
    ) += (email, password)
  }

  /**
   * List all the people in the database.
   */
  def list(): Future[Seq[User]] = db.run {
    users.result
  }
}