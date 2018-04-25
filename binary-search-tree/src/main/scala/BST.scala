/** Purely functional BST ADT
  */
sealed trait BST[+T] {
  def value: T

  def left: BST[T]

  def right: BST[T]

  def isEmpty: Boolean

  def insert[U >: T](x: U)(implicit ev: Ordering[U]): BST[U] = this match {
    case Nil => Node(x, Nil, Nil)
    case Node(v, left, right) if ev.lteq(x, v) => Node(v, left.insert(x), right)
    case Node(v, left, right) if ev.gt(x, v) => Node(v, left, right.insert(x))
  }

  def toList: List[T] = this match {
    case Nil => List.empty
    case Node(v, left, right) => left.toList ::: v :: right.toList
  }
}

case class Node[+T](value: T, left: BST[T], right: BST[T]) extends BST[T] {
  override def isEmpty: Boolean = false
}

case object Nil extends BST[Nothing] {
  override def value: Nothing = throw new NotImplementedError("Empty tree")

  override def left: Nothing = throw new NotImplementedError("Empty tree")

  override def right: Nothing = throw new NotImplementedError("Empty tree")

  override def isEmpty: Boolean = true
}

object BST {

  def fromList[T: Ordering](l: List[T]): BST[T] = {
    l.foldLeft(Nil: BST[T])((acc, v) => acc.insert(v))
  }

  def apply[T](x: T): BST[T] = Node(x, Nil, Nil)

  def empty[T]: BST[T] = Nil
}