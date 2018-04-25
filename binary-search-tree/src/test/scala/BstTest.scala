import org.scalatest.{Matchers, FlatSpec}

/** @version created manually **/
class BSTTest extends FlatSpec with Matchers {
  val bst4: BST[Int] = BST(4)

  it should "empty tree" in {
    BST.empty should equal(Nil)
  }

  it should "retain data" in {
    bst4.value should equal(4)
  }

  it should "retain data - char" in {
    BST('d').value should equal('d')
  }

  it should "insert less" in {
    bst4.insert(2).left.value should equal(2)
  }

  it should "insert less - char" in {
    BST('d').insert('a').left.value should equal('a')
  }

  it should "insert same" in {
    bst4.insert(4).left.value should equal(4)
  }

  it should "insert greater than" in {
    bst4.insert(5).right.value should equal(5)
  }

  it should "handle complex tree - sort out of order list" in {
    val bst = BST.fromList(List(4, 2, 6, 1, 3, 7, 5))
    bst.toList should equal((1 to 7).toList)

    bst.value should equal(4)
    bst.left.value should equal(2)
    bst.left.left.value should equal(1)
    bst.left.right.value should equal(3)
    bst.right.value should equal(6)
    bst.right.left.value should equal(5)
    bst.right.right.value should equal(7)
  }

  it should "iterating one element" in {
    bst4.toList should equal(List(4))
  }

  it should "iterating over smaller element" in {
    BST.fromList(List(4, 2)).toList should equal(List(2, 4))
  }

  it should "iterating over larger element" in {
    BST.fromList(List(4, 5)).toList should equal(List(4, 5))
  }

  it should "iterating over complex tree" in {
    BST.fromList(List(4, 2, 1, 3, 6, 7, 5)).toList should equal((1 to 7).toList)
  }

  it should "iterating over complex tree - chars" in {
    BST.fromList(List('d', 'b', 'a', 'c', 'f', 'g', 'e')).toList should
      equal(('a' to 'g').toList)
  }
}
