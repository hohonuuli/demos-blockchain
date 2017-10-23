package org.mbari.blockchain

import akka.actor.Actor.Receive
import akka.actor.{ Actor, Props }

object BlockchainActor {
  case class NewBlock()
  case class Hash()
  case class NewTransaction()
  case class LastBlock()
  case class Mine()

  def props: Props = Props[BlockchainActor]
}

class BlockchainActor extends Actor {
  import BlockchainActor._

  def receive: Receive = {
    case NewBlock =>
    //sender() ! Users(users.toSeq)
    case Hash =>
    //users += user
    //sender() ! ActionPerformed(s"User ${user.name} created.")
    case NewTransaction =>
    //sender() ! users.find(_.name == name)
    case LastBlock =>
    //users.find(_.name == name) foreach { user => users -= user }
    //sender() ! ActionPerformed(s"User ${name} deleted.")
    case Mine() =>
    case _ =>
  }
}
