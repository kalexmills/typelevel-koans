package cats.koans

import cats._
import cats.implicits._
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.CancelAfterFailure
import Util._

import cats.data.OptionT

/**
  * Monads don't compose in general, but MonadTransformers do.
  */
class MonadTransformerKoans_05 extends AnyFunSpec with Matchers with CancelAfterFailure {
  describe("Monad Transformer Masters") {

    /**
      * If F[_] and G[_] are Monads, then to obtain a monad for F[G[_]], we use a monad transformer for G.
      *
      * Monad transformers can be recognized by their type signature. Their names are typically suffixed with T
      */
    they("recognize the signature of a monad transformer") {
      // Most of the transformers provided by cats can be found in the cats.data package
      import cats.data.OptionT

      // OptionT is the monad transformer for Option, here, the outer type is List[_]
      val x: OptionT[List, Int] = OptionT(List(Some(1), Some(2), None))

      // Monad transformers often just wrap their value and provide additional operations on it.
      x.value mustBe (___)
    }

    /**
      * Monad transformers allow multiple monadic effects to be applied in a stack, while composing the effects handled by
      * each monad in the stack.
      */
    they("understand that Monad transformers allow stacking effects") {
      // Here F[_] = List, G[_] = Option, and OptionT is the monad transformer.
      val letterList: List[Option[String]] = List(Some("a"), Some("b"), None, Some("c"))
      val numberList: List[Option[Int]]    = List(Some(1), None, Some(2))

      // Monad transformers are typicall named after the inner monad, suffixed with T.
      def stackWrap[A](l: List[Option[A]]): OptionT[List, A] = ___

      val pairList = for {
        x: String <- stackWrap(letterList)
        y: Int    <- stackWrap(numberList)
      } yield (x, y)

      // format: off
      pairList mustBe (List(Some(("a", 1)), None, Some(("a", 2)), 
                            Some(("b", 1)), None, Some(("b", 2)), 
                            None, 
                            Some(("c", 1)), None, Some(("c", 2))
                      ))
      // format: on
    }

    /**
      * When flatMap is combined, the results can sometimes be unintuitive.
      */
    they("can reason about the effect of combined flatMap operations") {}

    /**
      * Since Monad transfomers are themselves monads, they can also be composed.
      */
    they("can compose multiple transformers to create arbitrarily nested stacks") {}

    /**
      * Earlier we saw flatMap does not compose in general. If the inner monad is fixed, we can implement the composition.
      */
    they("know how to implement composed flatmap operations.") {
      // this is the same flatmap definition of F[G[_]] from an earlier koan, with G[_] = Option[_]
      def flatMap[F[_]: Monad, A, B](fa: F[Option[A]])(f: A => F[Option[B]]): F[Option[B]] = {
        ___ // with G fixed to a concrete type, an implementation should be possible
      }

      flatMap(List(Some(1), Some(2)).widen[Option[Int]])(x => List(Some(x), None)) mustBe
        (List(Some(1), None, Some(2), None))
    }

    /**
      * Implementing your own monad transformer is as simple as introducing a
      */
    they("can provide their own implementations of Monad transformers") {
      // by convention, we name monad transformers after the inner monad, suffixed with a T
      case class SimpleEitherT[F[_], E, A](value: F[Either[E, A]])

      implicit def eitherTForMonad[F[_]: Monad, E] = new SimpleMonad[SimpleEitherT[F, E, *]] {
        def pure[A](a: A): SimpleEitherT[F, E, A]                                                             = ___
        def flatMap[A, B](fa: SimpleEitherT[F, E, A])(f: A => SimpleEitherT[F, E, B]): SimpleEitherT[F, E, B] = ___
      }

      eitherTForMonad[List, String]
        .flatMap(SimpleEitherT(List(1.asRight, 2.asRight, "oops".asLeft, 4.asRight)))(x =>
          SimpleEitherT(List((x + 1).asRight, "padding".asLeft))
        )
        .value mustBe
        // format: off
        (List(
          2.asRight,     "padding".asLeft,
          3.asRight,     "padding".asLeft,
          "oops".asLeft, "padding".asLeft,
          5.asRight,     "padding".asLeft
        ))
        // format: on

      eitherTForMonad[Option[*], String]
        .flatMap(SimpleEitherT(4.asRight.some))(x => SimpleEitherT[Option, String, Int](None))
        .value mustBe (Right(None))
    }

    trait SimpleMonad[F[_]] {
      // pure lifts the value a into the monadic context
      def pure[A](a: A): F[A]

      // flatMap allows you to modify the value inside an effect, while possibly introducing additional complication.
      def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]
    }
  }
}
