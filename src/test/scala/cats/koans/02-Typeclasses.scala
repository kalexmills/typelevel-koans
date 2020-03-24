package cats.koans

import cats._
import cats.implicits._
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.must.Matchers

import Util._
import org.scalatest.CancelAfterFailure

// These Koans are meant to demystify some of the implementation details of cats, which can help with
// debugging. It also introduces typeclasses as a concept.
class TypeclassKoans_02 extends AnyFunSpec with Matchers with CancelAfterFailure {

  /**
    * Cats provides a common interface for several general-purpose typeclasses used in pure functional
    * programmming. But what is a Typeclass?
    */
  describe("Typeclass Masters") {

    /**
      * In general we think of Typeclasses as 'type constructors'. They define 'new types' given other
      * types as their argument, just like constructors define new objects given other objects as their
      * arguments.
      *
      * In Scala, Typeclasses are implemented as implicit traits with one or more generic parameters.
      * We think of the defs on the trait as extending the behavior of the types
      */
    they("understand how typeclasses are implemented and used") {
      // A simplified definition of the Monoid typeclass is below.
      trait SimpleMonoid[A] {
        def combine(x: A, y: A): A // combine extends the behavior of type A by providing an associative operation
        def empty: A               // empty extends the behavior of type A by providing a left and right identity for combine.
      }

      // the typeclasses required for a type are described as type bounds on generic arguments
      def combineAll[A: SimpleMonoid](list: List[A]): A = {
        // within the body of implementations, an instance of the typeclass can be summoned.
        val summonedInstance = implicitly[SimpleMonoid[A]]
        list.fold(summonedInstance.empty)(summonedInstance.combine)
      }

      // at the callsite, an implicit instance of SimpleMonoid[A] must be available

      implicit val monoidForString: SimpleMonoid[String] = ___

      combineAll(List("a", "b", "c")) mustBe ("abc")

      // If no typeclass is available at the callsite, compilation will fail with a warning like
      // could not find implicit evidence of type SimpleMonoid[Int].

      // combineAll(List(1, 2, 3)) mustBe (6)
      fail("Uncomment the above line and provide a valid typeclass to ensure it compiles")
    }

    // Below is a simple typeclass we made up for types which can be reversed
    trait Reversable[A] {
      ___ // TODO: add an appropriate 'reverseIt' method (named to avoid collision with 'reverse')
    }

    // TODO: add your own implementation of the Reversable typeclass here.
    implicit val reversableForString: Reversable[String] = ___

    they("can define and use their own typeclasses") {
      // implicitly[Reversable[String]].reverseIt("asdf") mustBe ("fdsa")
      fail("Uncomment the above method to and ensure it compiles")
    }

    // cats provides summoners for all of their typeclasses, via the scala apply method.
    object Reversable {
      // the typical implementation routes apply to implicitly.
      def apply[A: Reversable]: Reversable[A] = implicitly[Reversable[A]]
    }

    they("know how to invoke a summoner for a typeclass") {
      ___ mustBe a[Reversable[String]]
    }

    // cats also provides its Syntactic sugar via implicit type conversions and a special Ops class. This is
    // more generally known as the "Pimp My Library" pattern.
    implicit class ReversableOps[A: Reversable](a: A) {
      def reverseIt[A] = ___ // TODO: reverse a
    }
    they("know how to avoid summoning entirely by implicitly converting to an Ops instance") {
      // "123456".reverseIt shouldBe ("654321")
      fail("Uncomment the above method and ensure the test passes")
    }
  }
}
