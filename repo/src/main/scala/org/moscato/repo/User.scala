package org.moscato.repo
import java.sql.Timestamp
import MoscatoDb._
import net.liftweb.squerylrecord.KeyedRecord
import net.liftweb.record.Record
import net.liftweb.record.field._
import net.liftweb.record.MetaRecord
import net.liftweb.squerylrecord.RecordTypeMode._
import net.liftweb.common._
import net.liftweb.http._
import java.util.GregorianCalendar
import org.apache.shiro.crypto.SecureRandomNumberGenerator
import org.apache.shiro.crypto.hash.Sha256Hash
import org.apache.shiro.util.SimpleByteSource
import net.liftweb.util.FieldError
import org.squeryl.annotations.Column
import shiro.Utils._

/**
 * The singleton that has methods for accessing the database
 */
class User private() extends Record[User] with KeyedRecord[Long] {
  
  override def meta = User
  
  @Column(name="id")
  override val idField = new LongField(this)
  
  val created = new DateTimeField(this, new GregorianCalendar)
  
  protected class MyEmail(obj: User, size: Int) extends EmailField(obj, size) {
    
    def valUnique(msg:String)(email:String): List[FieldError] = {
      (from(users)(u => where(u.email.is === email) compute(count)):Long) match {
        case 0 => Nil
        case _ => FieldError(MyEmail.this, msg) :: Nil
      }
    }
      
    override def validations = valUnique(S.?("email-address-not-unique")) _ :: super.validations
    override def displayName = S ? "email.address"
  }

  val email = new MyEmail(this, 256)
  
  @Column("password_hash")
  val passwordHash = new OptionalStringField(this, Empty)
  
  @Column("password_salt")
  val passwordSalt = new OptionalBinaryField(this)
  
  val rng = new SecureRandomNumberGenerator
  def newSalt = rng.nextBytes()

  def password(password: String) {
    val salt = newSalt.toString + email.get
    passwordSalt(salt.getBytes())
    //val hash = new Sha256Hash(password, (new SimpleByteSource(newSalt)).getBytes).toHex
    val hash = new Sha256Hash(password, salt).toBase64
    passwordHash(hash)
  }
  


  lazy val roles = userToRole.left(this)

  /*
  @OneToMany
  var iconSets: java.util.List[IconSet] = new java.util.Vector[IconSet]
  
  @Temporal(TemporalType.TIMESTAMP)
  @Column
  var created: java.util.Date

  
  protected def findUserByUniqueId(id: String): Box[TheUserType] = User.findById(id)
  
  protected def findUserByUserName(name: String): Box[TheUserType] = User.findByUserName(name)
  
  protected def createNewUserInstance(): TheUserType = new User
  
  protected def userFromStringId(id:String): Box[TheUserType] = Empty
  */
  
}

object User extends User with MetaRecord[User] {
  
  def now = new Timestamp(System.currentTimeMillis)

  def findById(id:Long) = users.lookup(id)
  
  def findByEmail(email:String) =
    from(users)(u => where(u.email.get === email) select(u)).headOption
  
  // FIXME
  def findByUsername(username:String) = findByEmail(username)
  
  def loggedIn_? = isAuthenticated
  
  def currentUser: Option[User] =
    principal match {
      case Full(id) => findById(id)
      case _ => Empty
    }
  
  def currentUserId:Box[Long] = principal
  
  def currentUserIdOrException: Long =
    principal match {
      case Full(userId) => userId
      case _ => throw new Exception("No user logged in.")
    }


}