package org.mbari.blockchain

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import scala.io.StdIn

object Server {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("blockchain")
    implicit val materializer = ActorMaterializer

    implicit val executionContext = system.dispatcher

    val route = {

    }
  }
}