package controllers

import play.api.mvc.{Action, Controller}
import model.{DatabaseRestaurant, SimpleRestaurant}
import play.api.libs.json._
import com.sksamuel.elastic4s.ElasticClient
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.mappings.FieldType._
import com.sksamuel.elastic4s.StopAnalyzer

/**
 * @author dpersa
 */
object RestaurantsController extends Controller {

  def search = Action {
    val client = ElasticClient.remote("localhost", 9300)
    client.execute {
      create index "places" mappings (
        "cities" as (
          "id" typed IntegerType,
          "name" typed StringType boost 4,
          "content" typed StringType analyzer StopAnalyzer
          )
        )
    }

    client.execute {
      index into "places/cities" id "uk" fields (
        "name" -> "London",
        "country" -> "United Kingdom",
        "continent" -> "Europe",
        "status" -> "Awesome"
        )
    }
    println("XXXX: Search")
    
    Ok("Ok")
  }
  
  def populate = Action {
    (1 until 100).foreach{x => {
        val databaseRestaurant = DatabaseRestaurant("rest-id-" + x, "The Bird " + x)
        DatabaseRestaurant.save(databaseRestaurant)
      }
    }
    Ok("OK")
  }

  def show(id: String)  = Action {
    
    val databaseRestaurant = DatabaseRestaurant.getById(id)
    
    val restaurant = SimpleRestaurant.builder.from(databaseRestaurant).build
    
    Ok(Json.toJson(restaurant))
  }
}
