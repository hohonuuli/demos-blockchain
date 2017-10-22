package org.mbari.blockchain

import java.nio.charset.StandardCharsets
import java.time.Instant
import javax.xml.bind.DatatypeConverter



case class Transaction(sender: String, recipient: String, amount: Int)

case class Block(
  index: Int,
  timestamp: Instant,
  transactions: Seq[Transaction],
  proof: Long,
  previousHash: String
)


object Blockchain extends JsonSupport {

  import java.security.MessageDigest

  private[this] val digest = MessageDigest.getInstance("SHA-256")

  def newBlock(chain: Seq[Block],
               proof: Long,
               previousHash: Option[String] = None,
               transactions: Seq[Transaction] = Nil): Unit = chain :+ Block(
      chain.size + 1,
      Instant.now(),
      transactions,
      proof,
      previousHash.getOrElse(hash(chain.last))
    )


  def newChain(): Seq[Block] =
    Seq(Block(1, Instant.now(), Nil, 100L, "1"))

  def hash(block: Block): String = {

    val blockString = blockJsonFormat.write(block).compactPrint
    sha256Hex(blockString)
  }

  def proof(lastProof: Long): Long = {
    var proof = 0L
    while (!validProof(lastProof, proof)) {
      proof = proof + 1
    }
    proof
  }

  private def sha256Hex(s: String): String = {
    val bytes = digest.digest(s.getBytes(StandardCharsets.UTF_8))
    toHexString(bytes)
  }

  private def validProof(lastProof: Long, proof: Long): Boolean = {
    val guess = s"${lastProof}${proof}"
    val guessHash = sha256Hex(guess)
    guessHash.substring(0, 4).equals("0000")
  }

  def toHexString(sha: Array[Byte]): String = DatatypeConverter.printHexBinary(sha)
  def fromHexString(hex: String): Array[Byte] = DatatypeConverter.parseHexBinary(hex)
}
