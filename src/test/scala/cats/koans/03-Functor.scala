package cats.koans

import cats._
import cats.implicits._
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.must.Matchers

import Util._
import org.scalatest.CancelAfterFailure

class FunctorKoans_03 extends AnyFunSpec with Matchers with CancelAfterFailure {

  /**
    * The Functor typeclass is one of the most ubiquitous and useful typeclasses in functional programming.
    */
  describe("Functor Masters") {

    /**
      * In a nutshell, a Functor describes how the types within a "container-type" can be transformed via
      * an arbitrary function.
      */
    they("understand what it means to map over a container type") {
      val l = List(1, 2, 3, 4, 5)

      // the argument of map takes a function which is applied to each element of the collection.
      l.map(___) mustBe (List(10, 20, 30, 40, 50))
    }

    // From another point of view, a Functor describes how to chain together small functions on a container via
    // composition. Said another way, it allows you to thread function composition through a type.
    they("can use map to chain together multiple operations in a sequence") {
      List(1, 2, 3, 4, 5)
        .map(___)
        .map(_.toString)
        .map(___) mustBe (List("111!", "222!", "333!", "444!", "555!"))
    }

    // The power of Functors comes from how general they are. Functors exist on many types.
    they("understand that Options are a functor") {
      def divide(x: Int, y: String): Option[Int] =
        y.toIntOption
          .map(___)

      List("0", "1", "2", "3").map(___) mustBe (List(None, Right(1), Right(0.5), Right(1 / 3)))
    }

    they("understand that Eithers are a functor, by convention map applies to the type on the right") {
      def multiply(x: String, y: Int): Either[String, String] =
        Either
          .fromOption(x.toIntOption, "notnum")
          .map(___)
          .map(___)

      List("0", "1", "pansies", "3").map(___) mustBe (List(Right("0"), Right("12"), Left("notnum"), Right("36")))
    }

    /**
      * For most container types, an implementation of map is already defined by the standard library. However
      * the cats implementation provides a unified interface and integrates nicely with other typeclasses.
      */
    they("know that additional combinators are provided 'for free' by the cats library") {
      // fproduct preserves the argument of the map
      List(1, 2, 3)
        .fproduct(___) mustBe (List((1, "1"), (2, "2"), (3, "3")))

      // unzip breaks apart a Functor of pairs into a pair of Functors.
      Functor[List]
        .unzip(List((1, 2), (3, 4), (5, 6))) mustBe (List(1, 3, 5), ___)

      sealed trait Animal
      case class Dog(color: String) extends Animal

    }

    /**
      * The Functor typeclass is unlike the Monoid and Semigroup typeclasses we've seen before. Monoid and
      * Semigroup take a type with zero unbound generic arguments as parameters, whereas Functor takes a type with
      * one unbound type argument. These types are called 'types with a hole' or 'higher-kinded' types.
      */
    they("can recognize the presence of higher-kinded types") {
      type F[A] = List[A]              // F is a higher-kinded type with one hole.
      type G[A] = Either[String, F[A]] // Either is a higher-kinded type with two holes. Filling one yields a type with one hole.

      def converter[A](x: G[A]): Either[String, List[A]] = ___

      converter(Right(List(1))) mustBe (Right(List(1)))
      converter(Left("nope")) mustBe (Left("nope"))
    }

    // F[_]: Functor is any higher kinded type with one hole for which there is an implicit Functor[F] in scope.
    def functorWrapSome[A, F[_]: Functor](x: F[A]): F[Option[A]] = ___

    /**
      * When writing generic functions with higher-kinded types, any type bounds must be satisfied at the call-site.
      */
    they("can work with generic functions which accept any functor") {
      functorWrapSome(List(1, 2, 3, 4)) mustBe (List(Some(1), Some(2), Some(3), Some(4)))
    }

    /**
      * Functors compose. That is, if types F[_] and G[_] are both Functors, type F[G[_]] will also be a Functor.
      * Practically speaking, this means that map can "unwrap" multiple layers in a functor stack
      */
    they("know that functors compose") {
      val listOption: List[Option[Int]] = List(Some(1), None, Some(4), None)

      listOption.map(___) mustBe Some(List(Some("1!"), None, Some("4!"), None))
    }

    /**
      * Functor implementations must follow the Functor laws.
      */
    they("can implement their own lawful Functor typeclasses for new types") {
      // Implement a functor for (A, B, C), like Either, it should be right-biased.
      implicit def functorForTuple3[C, D] = new Functor[(C, D, *)] {
        def map[A, B](fa: (C, D, A))(f: A => B): (C, D, B) = ___
      }

      (1, 2, 3).map(_.toString) mustBe ((1, 2, "3"))

      // Functors must preserve the identity map.
      (1, 2, "fish").map(identity) mustBe ((1, 2, "fish"))

      // Functors must preserve the composition of functions.
      val f = (x: Int) => x + 1
      val g = (x: Int) => 3 * x

      (1, 2, 4).map(f compose g) mustBe ((1, 2, 4).map(f).map(g))
    }

    they("can implement their own Functor typeclasses for new data structures") {
      sealed trait Tree[A]
      case class Leaf[A](a: A)                                extends Tree[A]
      case class Node[A](left: Tree[A], right: Tree[A], a: A) extends Tree[A]

      implicit def functorForTree: Functor[Tree] = ___

      val tree: Tree[Int] = Node(Node(Leaf(1), Leaf(3), 2), Leaf(5), 4)

      tree.map(_ + 13) mustBe (Node(Node(Leaf(14), Leaf(16), 15), Leaf(18), 17))
    }

  }
}
