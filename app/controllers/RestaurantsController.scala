package controllers

import play.api.mvc.{BodyParsers, Action, Controller}
import model.{Restaurant, DatabaseRestaurant, SimpleRestaurant}
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
      create index "restaurants" mappings (
        "restaurants" as (
          "id" typed StringType,
          "name" typed StringType boost 4
          )
        )
    }

    client.execute {
      index into "restaurants/restaurants" id "sim" doc SimpleRestaurant("sim-rest", "The Birdy")
    }
    
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

  //  curl --include \
  //    --request POST \
  //    --header "Content-type: application/json" \
  //    --data '{"id":"rest-id-00","name": "My Res Bird"}' \
  //  http://localhost:9000/restaurants
  def createAction = Action(BodyParsers.parse.json) { request =>
    val restaurant = request.body.as[SimpleRestaurant]
    val databaseRestaurant = DatabaseRestaurant.builder.from(restaurant).build
    DatabaseRestaurant.save(databaseRestaurant)
    Ok(Json.toJson(restaurant))
  }
}
