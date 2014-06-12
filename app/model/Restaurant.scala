package model

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import play.api.libs.json.util._
import play.api.libs.json.Reads._
import play.api.data.validation.ValidationError
import datasources.Couchbase
import model.SimpleRestaurant
import scala.collection.JavaConversions

trait Restaurant extends Identity {
  val name: String
}

object Restaurant {
  
  implicit val simpleRestaurantWrites = new Writes[SimpleRestaurant] {
    def writes(restaurant: SimpleRestaurant) = Json.obj(
      "id" -> restaurant.id,
      "name" -> restaurant.name
    )
  }

  def from(restaurant: Restaurant): SimpleRestaurant = new SimpleRestaurant(restaurant.id, restaurant.name)
}

case class SimpleRestaurant(id: String, name: String) extends Identity with Restaurant

object SimpleRestaurant {
  
  def builder = new SimpleRestaurantBuilder
}

trait RestaurantBuilder[T <: Restaurant] extends Builder[T] {
  protected var id: String = ""
  protected var name: String = ""
  
  def from(restaurant: Restaurant): this.type = {
    this.id = restaurant.id
    this.name = restaurant.name
    this
  }
}

class SimpleRestaurantBuilder extends RestaurantBuilder[SimpleRestaurant] {
  def build = SimpleRestaurant(id, name)
}



