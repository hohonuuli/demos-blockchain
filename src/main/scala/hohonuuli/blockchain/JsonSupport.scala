package hohonuuli.blockchain

import java.time.Instant

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{ DefaultJsonProtocol, JsString, JsValue, JsonFormat }

trait JsonSupport extends SprayJsonSupport {
  // import the default encoders for primitive types (Int, String, Lists etc)
  //import DefaultJsonProtocol._

  object SnakifiedSprayJsonSupport extends SnakifiedSprayJsonSupport

  import SnakifiedSprayJsonSupport._

  implicit def instantFormat = new JsonFormat[Instant] {

    override def write(obj: Instant): JsValue = JsString(obj.toString)

    override def read(json: JsValue): Instant = json match {
      case JsString(s) => Instant.parse(s)
      case x => spray.json.deserializationError("Expected time as JsString, but got " + x)
    }
  }

  implicit val transactionJsonFormat = jsonFormat3(Transaction)
  implicit val blockJsonFormat = jsonFormat5(Block)

  implicit val chainResponseJsonFormat = jsonFormat2(ChainResponse)
  implicit val responseMessageJsonFormat = jsonFormat5(ResponseMessage)
  implicit val messageJsonFormat = jsonFormat1(Message)
  implicit val nodesJsonFormat = jsonFormat1(Nodes)
  implicit val nodesResponseJsonFormat = jsonFormat2(NodesResponse)
  implicit val nodeResolved1JsonFormat = jsonFormat2(NodeResolved1)
  implicit val nodeResolved2JsonFormat = jsonFormat2(NodeResolved2)

}

/**
 * A custom version of the Spray DefaultJsonProtocol with a modified field naming strategy
 */
trait SnakifiedSprayJsonSupport extends DefaultJsonProtocol {
  import reflect._

  /**
   * This is the most important piece of code in this object!
   * It overrides the default naming scheme used by spray-json and replaces it with a scheme that turns camelcased
   * names into snakified names (i.e. using underscores as word separators).
   */
  override protected def extractFieldNames(classTag: ClassTag[_]) = {
    import java.util.Locale

    def snakify(name: String) = PASS2.replaceAllIn(PASS1.replaceAllIn(name, REPLACEMENT), REPLACEMENT).toLowerCase(Locale.US)

    super.extractFieldNames(classTag).map { snakify(_) }
  }

  private val PASS1 = """([A-Z]+)([A-Z][a-z])""".r
  private val PASS2 = """([a-z\d])([A-Z])""".r
  private val REPLACEMENT = "$1_$2"
}