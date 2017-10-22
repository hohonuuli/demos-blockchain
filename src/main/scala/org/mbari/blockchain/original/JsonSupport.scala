package org.mbari.blockchain.original

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

trait JsonSupport extends SprayJsonSupport {
  // import the default encoders for primitive types (Int, String, Lists etc)
  import DefaultJsonProtocol._

  implicit val blockJsonFormat = jsonFormat5(Block)
  implicit val transactionJsonFormat = jsonFormat3(Transaction)
  implicit val chainResponseJsonFormat = jsonFormat2(ChainResponse)


}