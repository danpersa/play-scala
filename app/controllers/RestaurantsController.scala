package controllers

import play.api.mvc.{Action, Controller}
import model.{DatabaseRestaurant, SimpleRestaurant}
import play.api.libs.json._

/**
 * @author dpersa
 */
object RestaurantsController extends Controller {

  def populate = Action {
    (1 until 100).foreach{x => {
        val databaseRestaurant = DatabaseRestaurant("rest-id-" + x, "The Bird " + x)
        DatabaseRestaurant.save(databaseRestaurant)
      }
    }
    Ok("OK")
  }

  def index = Action {
    Ok("OaK")
  }

  def show(id: String)  = Action {
    
    val databaseRestaurant = DatabaseRestaurant.getById(id)
    
    val restaurant = SimpleRestaurant.builder.from(databaseRestaurant).build
    
    Ok(Json.toJson(restaurant))
  }
}
