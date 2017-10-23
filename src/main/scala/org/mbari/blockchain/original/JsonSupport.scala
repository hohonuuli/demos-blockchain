package org.mbari.blockchain.original

import java.time.Instant

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{ DefaultJsonProtocol, JsString, JsValue, JsonFormat }

trait JsonSupport extends SprayJsonSupport {
  // import the default encoders for primitive types (Int, String, Lists etc)
  import DefaultJsonProtocol._

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