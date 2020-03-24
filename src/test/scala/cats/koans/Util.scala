package cats.koans

object Util {

  def ___[A]: A = ???.asInstanceOf[A]

  // There are too many ways not to write code in Scala
  type __
  type ____[_]
  type _____[_[_]]

  implicit def implicitFor__      = ???.asInstanceOf[__]
  implicit def implicitFor____[A] = ???.asInstanceOf[____[A]]
}
