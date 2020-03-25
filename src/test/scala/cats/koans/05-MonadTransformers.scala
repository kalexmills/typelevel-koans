package cats.koans

import cats._
import cats.implicits._
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.CancelAfterFailure
import Util._

/**
  * Monads don't compose in general, but MonadTransformers do.
  */
class MonadTransformerKoans_05 extends AnyFunSpec with Matchers with CancelAfterFailure {
  describe("Monad Transformer Masters") {

    /**
      * If F[_] and G[_] are Monads, then F[G[_]] is usually also a Monad, but there's no way to automatically create a Monad instance for
      * F[G[_]] unless one of the types is fixed.
      */
    they("understand that to compose Monads F[_] and G[_], one Monad must be fixed") {
      // this is the same flatmap definition from an earlier koan, with G[_] fixed to be Option[_]
      def flatMap[F[_]: Monad, A, B](fa: F[Option[A]])(f: A => F[Option[B]]): F[Option[B]] = {
        ___ // with G fixed to a concrete type, an implementation should be possible
      }

      flatMap(List(Some(1), Some(2)).widen[Option[Int]])(x => List(Some(x), None)) mustBe
        (List(Some(1), None, Some(2), None))
    }

    /**
      * We can create a specific typeclass which handles composing any monad F[_] with a fixed G[_] to yield a monad F[G[_]]
      */
    they("understand how to implement their own Monad transformers") {
      // by convention, we name a transformer after the fixed Monad, suffixed with 'T'
      trait SimpleOptionT[F[_]] { // Normally this would be called 'OptionT'
        def pure[F[_], A](a: A): F[Option[A]]
        def flatMap[F[_]: Monad, A, B](fa: F[Option[A]])(f: A => F[Option[B]]): F[Option[B]]
      }

      def optionTForMonad[F[_]: Monad] = new SimpleOptionT[F] {
        def pure[F[_], A](a: A): F[Option[A]]                                                = ___
        def flatMap[F[_]: Monad, A, B](fa: F[Option[A]])(f: A => F[Option[B]]): F[Option[B]] = ___
      }

      optionTForMonad[List].flatMap(List(Some(1), Some(2), None, Some(4)))(x => List(Some(x + 1), None)) mustBe
        (List(Some(2), None, Some(3), None, None, None, Some(5), None))

      optionTForMonad[Either[String, *]].flatMap(4.some.asRight[String])(x => Right(None)) mustBe (Right(None))
    }

    /**
      * cats provides Monad Transformers for most of its Monads, so usually they don't have to be written from scratch.
      */
    they("know how to use the Monad transformers provided by cats") {
      import cats.data.OptionT
    }
  }
}
