package model

import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.json.util._
import play.api.libs.json.Reads._
import play.api.data.validation.ValidationError
import datasources.Couchbase

trait User extends Identity {
  val username: String
  val email: String
}

object User {
  implicit val simpleUserWrites = new Writes[SimpleUser] {
    def writes(user: SimpleUser) = Json.obj(
      "id" -> user.id,
      "username" -> user.username,
      "email" -> user.email
    )
  }

  implicit val complexUserWrites = new Writes[ComplexUser] {
    def writes(user: ComplexUser) = Json.obj(
      "id" -> user.id,
      "username" -> user.username,
      "email" -> user.email,
      "bookmarks" -> user.bookmarks
    )
  }

  def builder = new ComplexUserBuilder
}

case class SimpleUser(id: String, username: String, email: String) extends Identity with User

case class ComplexUser(id: String, username: String, email: String, bookmarks: List[SimpleRestaurant]) extends Identity with User

case class DatabaseUser(id: String,
                        username: String,
                        email: String,
                        bookmarkIds:List[String],
                        typ: String = DatabaseUser.getClass.getSimpleName)
  extends Identity with User

object DatabaseUser {

  implicit val databaseUserWrites = new Writes[DatabaseUser] {
    def writes(user: DatabaseUser) = Json.obj(
      "id" -> user.id,
      "username" -> user.username,
      "email" -> user.email,
      "bookmarkIds" -> user.bookmarkIds,
      "type" -> user.getClass.getSimpleName
    )
  }

  implicit val databaseUserReads: Reads[DatabaseUser] = (
    (JsPath \ "id").read[String] and
    (JsPath \ "username").read[String] and
    (JsPath \ "email").read(email keepAnd minLength[String](5)) and
    (JsPath \ "bookmarkIds").read[List[String]] and
    (JsPath \ "type").read[String]
  )(DatabaseUser.apply _)
  
  def builder = new DatabaseUserBuilder

  def getById(id: String):DatabaseUser = {
    val client = Couchbase.getInstance()
    val databaseUserJson = client.get(id).asInstanceOf[JsObject]
    databaseUserJson.as[DatabaseUser]
    DatabaseUser("", "", "", List())
  }
}

trait UserBuilder[T <: User] extends Builder[T] {

  protected var id: String = ""
  protected var username: String = ""
  protected var email: String = ""
  
  def from(user: User): this.type = {
    this.id = user.id
    this.username = user.username
    this.email = user.email
    this
  }

  def withId(id: String): this.type = {
    this.id = id
    this
  }

  def withUsername(username: String): this.type = {
    this.username = username
    this
  }

  def withEmail(email: String): this.type = {
    this.email = email
    this
  }
}

class ComplexUserBuilder extends UserBuilder[ComplexUser] {

  private var bookmarks: List[SimpleRestaurant] = List()

  def withBookmarks(bookmarks: List[SimpleRestaurant]): this.type = {
    this.bookmarks = bookmarks
    this
  }

  def build = new ComplexUser(id, username, email, bookmarks)
}

class DatabaseUserBuilder extends UserBuilder[DatabaseUser] {

  private var bookmarkIds: List[String] = List()
  
  def addBookmark(bookmarkId: String): this.type = {
    this.bookmarkIds = bookmarkIds ++ Seq(bookmarkId)  
    this
  }
  
  def build = new DatabaseUser(id, username, email, bookmarkIds)
}