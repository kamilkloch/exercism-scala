import Permutation.StringOps
import eu.timepit.refined.api.Refined
import eu.timepit.refined.auto._
import eu.timepit.refined.numeric._

/** Simple permutation abstraction, used in the cipher. */
class Permutation(private val xs: IndexedSeq[Int]) {
  require(xs.nonEmpty, "Empty array.")
  require(xs.sorted == xs.indices, s"Input array is not a permutation of [0..${xs.size - 1}].")

  val size = xs.size

  def inverse: Permutation = {
    val inv = Array.fill(size)(0)
    for (i <- 0 until size)
      inv(xs(i)) = i
    new Permutation(inv.toIndexedSeq)
  }
}

object Permutation {
  def from(xs: IndexedSeq[Int]): Permutation = new Permutation(xs)

  def from(x: Int, xs: Int*): Permutation = new Permutation(x +: xs.toIndexedSeq)

  /** Permutation operations on a String */
  implicit class StringOps(s: String) {

    /** Enables the following syntax:
      * {{{
      *   val s = "rail"
      *   val p = Permutation.from(3, 2, 1, 0)
      *   s permuteBy p // "liar"
      * }}}
      */
    def permuteBy(p: Permutation): String = {
      require(s.size == p.size, "Incompatible sizes.")
      val res = new StringBuilder
      for (idx <- p.xs)
        res += s(idx)
      res.result()
    }
  }

}

object RailFenceCipher {

  def encode(clearText: String, rails: Int Refined Positive): String = if (rails.value == 1) clearText else {
    clearText permuteBy encodingPermutation(clearText.length, rails)
  }

  def decode(encodedText: String, rails: Int Refined Positive): String = if (rails.value == 1) encodedText else {
    encodedText permuteBy encodingPermutation(encodedText.length, rails).inverse
  }

  /** Computes Rails-Fence-Cipher permutation of characters for a text of given length. */
  private def encodingPermutation(length: Int, rails: Int Refined Positive): Permutation = {
    val b = IndexedSeq.fill(rails)(Seq.newBuilder[Int])
    for (idx <- 0 until length) idx % (2 * rails - 2) match {
      case i if i < rails => b(i) += idx
      case i => b(rails - 2 - (i % rails)) += idx
    }
    Permutation.from(b.flatMap(_.result()))
  }
}
