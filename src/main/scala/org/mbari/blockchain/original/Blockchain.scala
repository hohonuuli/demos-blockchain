package org.mbari.blockchain.original

import java.net.URL
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.concurrent.TimeUnit
import javax.xml.bind.DatatypeConverter

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ HttpRequest, StatusCode, StatusCodes }
import akka.stream.ActorMaterializer

import scala.collection.mutable
import scala.concurrent.Await
import scala.concurrent.duration._

/**
 *
 *
 * @author Brian Schlining
 * @since 2017-10-22T10:37:00
 */
class Blockchain {

  import Blockchain._

  var currentTransactions = new mutable.ArrayBuffer[Transaction]
  var chain = new mutable.ArrayBuffer[Block]
  var nodes = new mutable.ArrayBuffer[URL]
  newBlock(proof = 100, previousHash = Option("1"))

  def registerNode(address: String): Unit = {
    val url = new URL(address)
    nodes.append(url)
  }

  def validChain(chain: Seq[Block]): Boolean = {
    var lastBlock = chain.head
    var currentIdx = 1
    while (currentIdx < chain.size) {
      val block = chain(currentIdx)
      println(blockJsonFormat.write(lastBlock))
      println(blockJsonFormat.write(block))
      println("------------------")

      // Check that the hash of the block is correct
      if (block.previousHash != hash(lastBlock)) return false

      // Check that the proof of work is correct
      if (!validProof(lastBlock.proof, block.proof)) return false

      lastBlock = block
      currentIdx = currentIdx + 1
    }
    true
  }

  def resolveConflicts(): Boolean = {

    import spray.json._

    // Configure Akka-http
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()

    val neighbors = nodes
    var newChain: Seq[Block] = new mutable.ArrayBuffer[Block]

    // We're only looking for chains longer than ours
    var maxLength = chain.size

    // Grab and verify chans longer than ours
    for (node <- neighbors) {
      val responseFuture = Http().singleRequest(HttpRequest(uri = node.toURI.toString))
      val response = Await.result(responseFuture, 15 seconds)

      if (response.status == StatusCodes.OK) {
        val body = Await.result(response.entity
          .toStrict(15 seconds)
          .map(_.data.decodeString(StandardCharsets.UTF_8)), 15 seconds)
        val chainResponse = chainResponseJsonFormat.read(body.parseJson)

        val length = chainResponse.length
        val tempChain = chainResponse.chain
        if (length > maxLength && validChain(tempChain)) {
          maxLength = length
          newChain = tempChain
        }
      }
    }

    if (!newChain.isEmpty) {
      this.chain.clear()
      this.chain ++ newChain
      true
    } else false

  }

  def newBlock(proof: Long, previousHash: Option[String] = None): Block = {
    val block = new Block(
      chain.size + 1,
      Instant.now(),
      Seq(currentTransactions: _*), // convert to immutable collection
      proof,
      previousHash.getOrElse(hash(chain.last))
    )

    currentTransactions.clear()
    chain.append(block)
    block
  }

  def newTransaction(sender: String, recipient: String, amount: Int): Int = {
    val transaction = Transaction(sender, recipient, amount)
    currentTransactions.append(transaction)
    chain.last.index + 1
  }

}

object Blockchain extends JsonSupport {

  import java.security.MessageDigest

  private[this] val digest = MessageDigest.getInstance("SHA-256")

  def proofOfWork(lastProof: Long): Long = {
    var proof = 0L
    while (!validProof(lastProof, proof)) {
      proof = proof + 1
    }
    proof
  }

  def validProof(lastProof: Long, proof: Long): Boolean = {
    val guess = s"$lastProof$proof"
    val guessHash = sha256Hex(guess)
    guessHash.substring(0, 4) == "0000"
  }

  def hash(block: Block): String = {
    val blockString = blockJsonFormat.write(block).compactPrint
    sha256Hex(blockString)
  }

  private def sha256Hex(s: String): String = {
    val bytes = digest.digest(s.getBytes(StandardCharsets.UTF_8))
    toHexString(bytes)
  }

  def toHexString(sha: Array[Byte]): String = DatatypeConverter.printHexBinary(sha)
  def fromHexString(hex: String): Array[Byte] = DatatypeConverter.parseHexBinary(hex)

}

case class Transaction(sender: String, recipient: String, amount: Int)

case class Block(
  index: Int,
  timestamp: Instant,
  transactions: Seq[Transaction],
  proof: Long,
  previousHash: String
)

case class ChainResponse(chain: Seq[Block], length: Int)
