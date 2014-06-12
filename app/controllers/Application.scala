package controllers

import play.api._
import play.api.mvc._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.json._
import com.couchbase.client.vbucket.config.Bucket
import com.couchbase.client.{CouchbaseClient}
import java.net.URI
import java.util
import datasources.Couchbase
import model._
import scala.collection.mutable.ArrayBuffer
import net.spy.memcached.transcoders.SerializingTranscoder
import scala.collection.JavaConversions
import model.DatabaseRestaurant
import model.SimpleUser

object Application extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }
  
  def hello(name: String) = Action {
    Ok("Hello " + name)
  }
  
  def helloDelayed(name: String) = Action.async {
      val nameFuture = Future {
          Thread.sleep(1000)
          name
      }
      nameFuture.map(name => Ok("Hello: " + name))
  }
  
  def users = Action.async {
      val future = Future {
        val user = new SimpleUser("id", "dan", "dan@yahoo.com")
        Json.toJson(user)
      }
      future.map(json => Ok(json))
  }
  
  def couchbase = Action.async {
    val future = Future {
      val client = Couchbase.getInstance()

      val databaseRestaurant1 = new DatabaseRestaurant("rest-1", "The Bird")
      val databaseRestaurant2 = new DatabaseRestaurant("rest-2", "White Trash")
      val databaseUser = DatabaseUser.builder
                                     .withId("userId")
                                     .withEmail("dan@yahoo.com")
                                     .withUsername("danix")
                                     .addBookmark(databaseRestaurant1.id)
                                     .addBookmark(databaseRestaurant2.id)
                                     .build

      // Store a Document
      client.set(databaseRestaurant1.id, Json.toJson(databaseRestaurant1)).get()
      client.set(databaseRestaurant2.id, Json.toJson(databaseRestaurant2)).get()
      client.set(databaseUser.id, Json.toJson(databaseUser)).get()

      val userFromDatabase = DatabaseUser.getById(databaseUser.id)

      val bookmarksFromDatabase = DatabaseRestaurant.getBulk(userFromDatabase.bookmarkIds)

      val bookmarks = bookmarksFromDatabase.map(dbRestaurant => Restaurant.from(dbRestaurant))

      val complexUser = User.builder
                            .from(userFromDatabase)
                            .withBookmarks(bookmarks)
                            .build

      Json.toJson(complexUser)
    }
    
    
      
    future.map(json => Ok(json))
  }

  def couchbase1 = Action.async {
    val future = Future {
      val databaseUser = DatabaseUser.builder
        .withId("userId")
        .withEmail("dan@yahoo.com")
        .withUsername("danix")
        .addBookmark("12345")
        .build

      Json.toJson(databaseUser)      
    }



    future.map(json => Ok(json).as("application/json"))
  }
}