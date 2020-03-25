package cats.koans

import cats._
import cats.implicits._
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.must.Matchers

import Util._
import org.scalatest.CancelAfterFailure
import org.scalatest.Sequential
class MonoidKoans_01 extends AnyFunSpec with Matchers with CancelAfterFailure {

  /**
    * Cats is a library which defines a common interface for useful, general-purpose abstractions that
    * follow well-known laws.
    *
    * Monoids are an abstraction over associative binary operations aInt with an identity. An example
    * is addition over numbers, where the identity is zero.
    */
  describe("Monoid Masters") {

    /**
      * Monoids in cats are defined by two operations:
      *  def  combine[A](x: A, y: A): A
      *  def  empty: A
      */
    they("can summon Monoids and use them directly") {
      // The default monoid for numbers peforms addition.
      val monoidForInt = Monoid[Int]

      monoidForInt.combine(3, ___) mustEqual (7)

      monoidForInt.empty mustEqual (___[Int])
    }

    they("know the Monoid laws") {
      val a = randomInt()

      // The identity for any Monoid must act as a left and right identity
      Monoid[Int].combine(a, ___) mustEqual (a)
      Monoid[Int].combine(___, a) mustEqual (a)

      val b = randomInt()
      val c = randomInt()

      // The combine operation must be associative.
      Monoid[Int].combine(a, Monoid[Int].combine(b, c)) mustEqual (___[Int])
    }

    they("understand Monoids are general concept") {
      // There's a monoid for strings where the binary operation is concatenation
      Monoid[String].combine("a", ___) mustEqual (___[String])

      // Based on the Monoid laws, what should the identity be?
      Monoid[String].empty mustEqual (___[String])
    }

    they("recognize syntax provided by the cats library") {
      // Invoking Monoid[Int] directly is cumbersome, so cats provides extra syntax to help.
      1.combine(___) mustEqual (3)
      ___[String].combine("bc") mustEqual ("abc")

      1 |+| 3 mustEqual (___[Int])

      "a" |+| "c" |+| "b" mustEqual (___[String])
    }

    they("understand trait constraints on generic type arguments") {
      // The power of working with general constructs comes from the ability to re-use code based on
      // generic trait bounds. The [A: Monoid] below indicates that the arguments can be any type which is
      // a Monoid.
      def combineTuple[A: Monoid](tuple: (A, A, A)): A = {
        ___[A]
      }

      combineTuple((1, 2, 3)) mustEqual (6)
      combineTuple(("a", "b", "c")) mustEqual ("abc")
    }

    they("can define their own functions using generic bounds as required") {
      // define a function combineList so that it satisfies the 3 commented lines below
      def combineList = ___

      /* uncomment these lines
      combineList(List(1, 2, 3, 4, 5)) mustEqual (15)
      combineList(List.empty[Int]) mustEqual (0)
      combineList[String](List.empty) mustEqual ("")
       */

      fail("Comment or remove this line and remove comments from above")
    }

    they("understand how to read and use the Monoids implemented by others") {

      /**
        * Integers also form a Monoid under multiplication. To create a cats instance of Monoid for this
        * behavior, the typical pattern is to create an implicit value of an anonymous class, like below.
        */
      implicit val multiplicativeMonoidForInt = new Monoid[Int] {
        def combine(x: Int, y: Int): Int = x * y
        def empty: Int                   = 1
      }

      multiplicativeMonoidForInt.combine(3, ___) mustEqual (15)
    }

    // implement a Monoid for strings which reverses the string as part of the combine operation.
    lazy val reversingMonoidForString: Monoid[String] = ___

    they("are able to write their own Monoids when needed") {

      reversingMonoidForString.combine("a", "b") mustEqual ("ba")
      reversingMonoidForString.combine("a", reversingMonoidForString.empty) mustEqual ("a")
      reversingMonoidForString.combine(reversingMonoidForString.empty, "b") mustEqual ("b")
    }

    they("appreciate the power of using higher-level functions which rely on their implementation of primitives") {
      // combineAll uses the definitions of combine and empty internally.
      List("a", "b", "c").combineAll(reversingMonoidForString) mustEqual ("cba")

      def reverse(str: String): String =
        str.toList.map(_.toString).combineAll(___)

      reverse("abcdefg") mustEqual ("gfedcba")
      reverse("backwards") mustEqual ("sdrawkcab")
      reverse("") mustEqual ("")
    }

    they("understand that a Semigroup is just a monoid without the empty method") {
      // Some structures have an associative combine but no possible empty implementation. One example is
      // cats.data.NonEmptyList. Implement a Semigroup for it below.
      import cats.data.NonEmptyList

      implicit def reversingSemigroupForNonEmptyList[A] = ___

      List(NonEmptyList.one(1), NonEmptyList.one(2), NonEmptyList.one(4)).combineAllOption mustEqual
        (NonEmptyList.of(1, 2, 4))

      // NonEmptyList can be a useful construct to ensure guarantees and enhance local reasoning.
    }
  }
}
