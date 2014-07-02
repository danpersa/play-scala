package datasources

import com.couchbase.client.CouchbaseClient
import scala.concurrent.{Promise, Future}
import net.spy.memcached.internal.{OperationFuture, OperationCompletionListener}
import scala.util.Try
import play.api.libs.json.JsObject

/**
 * @author dpersa
 */
case class ScalaCouchbaseClient(javaClient: CouchbaseClient) {
  
  
  def set(key: String, value: AnyRef): Future[Boolean] = {
    val promise = Promise[Boolean]
    val result = javaClient.set(key, value);
    result.addListener(new OperationCompletionListener {
      def onComplete(future: OperationFuture[_]): Unit = {
        val r = future.get().asInstanceOf[Boolean]
        promise.success(r)
      }
    })
    promise.future
  }
  
  def get(key: String): String = {
    javaClient.get(key).asInstanceOf[String]
  }
}
