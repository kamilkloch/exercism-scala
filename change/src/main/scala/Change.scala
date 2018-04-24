import monix.eval._

import scala.collection.mutable

/** In general, Change is an NP-hard problem.
  * Typical solutions ('n' - amount, 'k' - number of coins):
  *  - bottom-up dynamic programming, and
  *  - bottom-down recursive programming with memoization
  * both have same worst-case performance: memory O(n + k), time O(n * k).
  *
  * Dynamic programming solution is short and comprehensible. However, straightforward array-based approach fails
  * for large 'n' (e.g. n = 10^9, coins = [10^6]. Recursive approach, on the other hand, may fail due to
  * stack overflow (e.g. 'n' = 10000, coins = [1]).
  *
  * Solution below is a recursive solution with memoization, which additionally is stack-safe.
  * It uses the suspended evaluation from [[monix.eval.Coeval]], where recursive calls are trampolined
  * (i.e., stack is traded for heap). Code is an experiment of a recursive and a stack-safe solution.
  */
object Change {
  type Coin = Int
  type Coins = Seq[Coin]
  type Amount = Int

  /** Returns the list containing the fewest amount of coins adding up to the given amount,
    * or None if the change is not possible.
    */
  def findFewestCoins(amount: Amount, coins: Coins): Option[Coins] = {

    /** Cache for computed partial solutions.
      * Used to recursively construct the list of coins and to speed up the computation
      * (from exponential to O(amount * coins.size) in worst case.
      */
    val cache: mutable.Map[Amount, Option[Solution]] = mutable.Map(0 -> Some(Solution(0, 0)))

    val coinsDecreasing = coins.sorted.reverse

    /** Recursively computes the partial Solution for the given amount, memoizes the result in the cache.
      * Whole computation tree is suspended in [[monix.eval.Coeval]] for stack safety.
      */
    def compute(amount: Amount): Coeval[Option[Solution]] = cache.get(amount).map(Coeval.now).getOrElse {

      val xs: Seq[Coeval[Option[Solution]]] = for (c <- coinsDecreasing if amount - c >= 0) yield cache.get(amount - c) match {
        case Some(x) => Coeval.now(x.map(s => Solution(s.numCoins + 1, c)))
        case None => Coeval.defer(compute(amount - c)).map(_.map(s => Solution(s.numCoins + 1, c)))
      }

      for {
        s <- Coeval.sequence(xs) // collect suspended evaluations: Seq[Coeval[...]] ~> Coeval[Seq[...]]
        fs = s.flatten
      } yield {
        if (fs.isEmpty) {
          cache.update(amount, None)
          None
        } else {
          val res = Some(fs.minBy(_.numCoins))
          cache.update(amount, res)
          res
        }
      }
    }

    if (amount < 0)
      None
    else {
      for (v <- compute(amount).value) yield {
        Seq.iterate((amount, v.firstCoin), v.numCoins) { case (a, c) => (a - c, cache(a - c).get.firstCoin) }
          .map(_._2).reverse
      }
    }
  }

  /** Partial solution to the change problem for an some arbitrary amount.
    *
    * @param numCoins  Number of coins in the optimal solution
    * @param firstCoin First coins comprising the optimal solution
    */
  private case class Solution(numCoins: Int, firstCoin: Coin)

}
