package controllers

import play.api._
import play.api.mvc._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.json._
import model.{SimpleRestaurant, ComplexUser, SimpleUser}

object Application1 extends Controller {

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
        val restaurant = new SimpleRestaurant("id", "The Bird")
        val complexUser = new ComplexUser("id", "danix", "danix@yahoo.com", List(restaurant))
        
        Json.toJson(Seq(complexUser))
      }
      
      future.map(json => Ok(json))
  }
}