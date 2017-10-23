package org.mbari.blockchain.original

import java.util.UUID

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route


/**
 *
 *
 * @author Brian Schlining
 * @since 2017-10-22T16:32:00
 */
class BlockchainServer extends JsonSupport {

  def main(args: Array[String]): Unit = {

    implicit val system = ActorSystem("blockchain")
    implicit val materializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.dispatcher

    val blockchain = new Blockchain

    val nodeIdentifier = UUID.randomUUID().toString.replace("-", "")

    val mineRout = path("mine") {
      get {
        val lastBlock = blockchain.chain.last
        val lastProof = lastBlock.proof
        val proof = Blockchain.proofOfWork(lastProof)

        // We must receive a reward for finding the proof
        // The sender is "0" to sginify that this node has mined a new coin
        blockchain.newTransaction(")", nodeIdentifier, 1)
        val block = blockchain.newBlock(proof)
        val response = ResponseMessage(
          "New Block Forged",
          block.index,
          block.transactions,
          block.proof,
          block.previousHash
        )
        complete(HttpEntity(
          ContentTypes.`application/json`,
          responseMessageJsonFormat.write(response).compactPrint
        ))
      }
    }

    val transactionRoute = path("transactions/new") {
      post {
        entity(as[Transaction]) { transaction =>
          val index = blockchain.newTransaction(
            transaction.sender,
            transaction.recipient, transaction.amount
          )
          val msg = Message(s"Transaction will be added to Block $index")
          complete(messageJsonFormat.write(msg).compactPrint)
        }
      }
    }

    val chainRout = path("chain") {
      get {
        val response = ChainResponse(blockchain.chain, blockchain.chain.size)
        complete(chainResponseJsonFormat.write(response).compactPrint)
      }
    }

    val nodesRoute = pathPrefix("nodes") {
      path("register") {
        entity(as[Nodes]) { nodes =>
          nodes.nodes.foreach(n => blockchain.registerNode(n))
          val r = NodesResponse(
            "New Nodes have been added",
            blockchain.nodes.map(_.toExternalForm)
          )
          complete(nodesResponseJsonFormat.write(r).compactPrint)
        }
      } ~
        path("resolve") {
          val replaced = blockchain.resolveConflicts()
          val c = if (replaced) {
            val r = NodeResolved1("Our chain was replaced", blockchain.chain)
            nodeResolved1JsonFormat.write(r).compactPrint
          } else {
            val r = NodeResolved2("Our chain is authoritative", blockchain.chain)
            nodeResolved2JsonFormat.write(r).compactPrint
          }
          complete(c)
        }
    }

    val routes = mineRout ~ transactionRoute ~ chainRout ~ nodesRoute

    val route: Route = pathPrefix("v1") {
      routes
    }

    Http().bindAndHandle(route, "localhost", 8888)

  }

}

case class ResponseMessage(
  message: String,
  index: Int,
  transactions: Seq[Transaction],
  proof: Long,
  previousHash: String
)

case class Message(message: String)

case class Nodes(nodes: Seq[String])

case class NodesResponse(message: String, totalNodes: Seq[String])

case class NodeResolved1(message: String, newChain: Seq[Block])
case class NodeResolved2(message: String, chain: Seq[Block])
