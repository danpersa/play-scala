package controllers

import play.api.mvc.{BodyParsers, Action, Controller}
import model.{Restaurant, DatabaseRestaurant, SimpleRestaurant}
import play.api.libs.json._
import com.sksamuel.elastic4s.ElasticClient
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.mappings.FieldType._
import com.sksamuel.elastic4s.StopAnalyzer
import play.api.libs.concurrent.Promise
import scala.concurrent.duration._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Success, Failure}

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
  def createAction = Action.async(BodyParsers.parse.json)  { request =>
    val restaurant = request.body.as[SimpleRestaurant]
    val databaseRestaurant = DatabaseRestaurant.builder.from(restaurant).build
    val saveFuture = DatabaseRestaurant.save(databaseRestaurant)
    val timoutFuture = Promise.timeout("Timeout", 1.second)
    Future.firstCompletedOf(Seq(saveFuture, timoutFuture)) map {
      case true => Ok(Json.toJson(restaurant))
      case false => InternalServerError("fail")
      case t: String => InternalServerError(t)
    } 
  }
}
