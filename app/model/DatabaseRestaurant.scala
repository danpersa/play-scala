package model

import datasources.Couchbase
import play.api.libs.json._
import scala.collection.JavaConversions
import play.api.libs.json.JsObject
import play.api.libs.functional.syntax._

/**
 * @author dpersa
 */
case class DatabaseRestaurant(id: String, name: String, t: String = DatabaseRestaurant.getClass.getSimpleName) extends Identity with Restaurant

class DatabaseRestaurantBuilder extends RestaurantBuilder[DatabaseRestaurant] {
  def build = DatabaseRestaurant(id, name)
}

object DatabaseRestaurant {

  implicit val databaseRestaurantWrites = new Writes[DatabaseRestaurant] {
    def writes(restaurant: DatabaseRestaurant) = Json.obj(
      "id" -> restaurant.id,
      "name" -> restaurant.name,
      "type" -> restaurant.getClass.getSimpleName
    )
  }

  implicit val databaseRestaurantReads: Reads[DatabaseRestaurant] = (
    (JsPath \ "id").read[String] and
    (JsPath \ "name").read[String] and
    (JsPath \ "type").read[String]
  )(DatabaseRestaurant.apply _)

  val client = Couchbase.getInstance()

  def getById(id: String):DatabaseRestaurant = {
    val jsonString = client.get(id).asInstanceOf[String]
    val jsonObject = Json.parse(jsonString)
    jsonObject.as[DatabaseRestaurant]
  }

  def save(databaseRestaurant: DatabaseRestaurant) = {
    val jsonObject: JsValue = Json.toJson(databaseRestaurant)
    val jsonString: String = Json.stringify(jsonObject)
    client.set(databaseRestaurant.id, jsonString).get()
  }

  def getBulk(ids: String*) = {
    client.getBulk(JavaConversions.asJavaIterator(ids.iterator))
  }

  def getBulk(ids: List[String]) = {
    val restaurantsJsonString = client.getBulk(JavaConversions.asJavaIterator(ids.iterator)).values().toString
    val restaurantsJson = Json.parse(restaurantsJsonString)
    restaurantsJson.as[List[DatabaseRestaurant]]
  }

  def builder = new DatabaseRestaurantBuilder
}



