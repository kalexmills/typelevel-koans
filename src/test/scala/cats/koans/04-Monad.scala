package cats.koans

import cats._
import cats.implicits._
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.CancelAfterFailure
import Util._
import java.{util => ju}

/**
  * Monads have a reputation for being scary, but they're just Functors with some extra (very powerful) stuff.
  */
class MonadKoans_04 extends AnyFunSpec with Matchers with CancelAfterFailure {

  describe("Monad Masters") {

    /**
      * Composing functions is how functional programmers get things done. Unfortunately, vanilla function
      * composition isn't as helpful as we would like.
      */
    they("appreciate the limits of function composition") {
      // divide should return Left("notnum") if y is not a number, and Left("div0") if y is zero,
      // otherwise it should return Right(x / y.toInt)
      def divide(x: Int, y: String): Either[String, Int] =
        Either
          .fromOption(y.toIntOption, "notnum")
          .map(___)                       // convert y to a Left("div0") if y is zero
          .map((safeY: Int) => x / safeY) // divide x by y

      // It is not possible to provide a function to map handles the conversion in the comment.

      // The type signature of map requires Int => Int, but a function of this type is not powerful enough to get the job done.
      // We need some way to map over functions like Int => Either[String, Int] to handle the additional complication of the
      // input possibly being zero.
      //
      // Once we've learned how to do this, we want to capture the knowledge in a typeclass so we can use the same behavior over
      // and over. That typeclass is called a Monad.

      // assertThrows[NotImplementedError] {  // TODO: add this assert to pass this test.
      divide(1, "0") mustBe (Left("div0"))
      //}
    }

    /**
      * Using pure functions shifts some of the burden of catching errors onto the compiler. As we've seen above, that's not always
      * possible using only the powers provided by Functor.
      *
      * We need a typeclass which knows how to compose functions like map, but also have the ability to handle some extra
      * complications. Since we want the typeclass to be general, we will leave those 'extra complications' as general as possible.
      * We'll refer to these complications as 'effects'.
      *
      *   If A is a type, we'll let F[A] be the type A, wrapped up in an effect by F[_].
      *
      * Think of F[A] as a way to encode some extra complication involved in retrieving a value of type A.
      *
      *   when F[_] = Option[_]     the complication is that the value in the type might or might not be there
      *   when F[_] = Either[B, _]  the complication is that the value of the type might actually be a value of type B
      *   when F[_] = List[_]       the complication is that there might be zero or zillions of values for the type.
      *
      * Our goal for the Monad typeclass is to compose functions of the form  A => F[B] which return values wrapped up in an effect.
      * Functions that look like this are called Kleisli arrows.
      *
      * The Functor typeclass lets us compose functions which look like A => B. Specifically, we can take A => B and compose it with
      * B => C to get A => C. Functor encapsulates how to do for all the values inside an effect F[_] by using map.
      *
      * What we want is a new combinator that lets us compose functions A => F[B] and B => F[C] to get a function A => F[C].
      *
      * Specifically, we want to compose A => F[B] with B => F[C] and end up with a function A => F[C]. Monads let us
      * do that for an effect by using flatMap.
      */
    they("understand the power of Kleisli arrow composition") {
      def divide(x: Int, y: String): Either[String, Int] =
        Either
          .fromOption(y.toIntOption, "notnum")
          .flatMap(___)
          .map((safeY: Int) => x / safeY) // fearlessly divide x by y

      divide(1, "0") mustBe (Left("div0"))
      divide(12, "2") mustBe (Right(6))
      divide(12, "fish") mustBe (Left("notNum"))
    }

    /**
      * Below are several common effects and the complications their Monad instances handle.
      *
      *   when F[_] = Option[_]  the complication is that the value in the type might or might not be there
      */
    they("understand how to use the Option monad to handle the effect of missing values") {
      def toBoringNumber(maybeExcitingNumber: String): Option[Int] = maybeExcitingNumber.replaceAll("!", "").toIntOption

      def divideExcitingNumbers(excitingNumber1: String, excitingNumber2: String): Option[Double] =
        toBoringNumber(excitingNumber2)
          .flatMap(___)
          .flatMap(___)

      divideExcitingNumbers("16!!!", "4!") mustBe (Some(4.0))
      divideExcitingNumbers("3", "fish") mustBe (None) // only exciting numbers allowed!
      divideExcitingNumbers("3!", "0!") mustBe (None)  // no division by zero, even if it's exciting!
    }

    // fake user "database" for a the following koans
    val userIds        = Map("Alex" -> 1, "Ryan"                 -> 2, "Denver"       -> 3, "Broken_User" -> 4)
    val passwordHashes = Map(1      -> "YmVzdC1wYXNzd29yZCEK", 2 -> "YWJjMTIzCg==", 3 -> "bDMzdGhheG9yCg==")
    def hash(plaintext: String) =
      new String(ju.Base64.getEncoder().encode(plaintext.getBytes())) // somewhat less than secure

    /**
      *   when F[_]: Either[B, _]      the complication is that the value of the type might actually be a value of type B
      */
    they("understand how to use the Either monad to handle the effect of mutually exclusive values of different types") {
      def passwordIsCorrect(username: String, plaintext: String): Either[String, Boolean] =
        Either
          .right(username)
          .flatMap(username => ___)
          .flatMap(uid => ___)
          .map(ciphertext => ___)

      passwordIsCorrect("Alex", "best-password!") mustBe (Right(true))
      passwordIsCorrect("Ryan", "super-secret-password") mustBe (Right(false))
      passwordIsCorrect("Broken_User", "roflmao") mustBe (Left("no_pwd"))
      passwordIsCorrect("Anonymous", "we_are_legion") mustBe (Left("no_uid"))
    }

    /**
      *   when F[_] = List[_]      the complication is that there might be zero or zillions of values for the type.
      */
    they("understand how to use the List monad to handle the effect of arbitrarily many values") {
      "abcd".toList
        .map(___)
        .flatMap(___)
        .flatMap(___) mustBe
        (List("a!", "a!", "b!", "b!", "c!", "c!"))
    }

    /**
      * Other more complicated (and therefore more useful) Monadic effects are covered in other modules. They are all built around
      * Monads and other typeclasses.
      *
      *   when F[_] = Eval[_]      the complication is that the value might be computed lazily.
      *   when F[_] = Future[_]    the complication is that you might have to wait to get your value (and lots can go wrong).
      *   when F[_] = Reader[_]    the complication is that you need to provide some other value in order for your value to be computed.
      *   when F[_] = Writer[_]    the complication is that the computation is carrying along an additional value being written to (e.g. logs).
      *   when F[_] = IO[_]        the complication is that to get your value, you have to run an arbitrarily complex program which may
      *                            involve concurrency, system-level calls, and (potentially) interaction with other systems.
      */
    /**
      * flatMap can be defined as the composition of two other combinators, map and flatten.
      */
    they("know that flatMap can be implemented in terms of map and flatten") {
      def flattenOption[A](oa: Option[Option[A]]): Option[A] = ___

      flattenOption(Some(None)) mustBe (None)
      flattenOption(Some(Some(1))) mustBe (Some(1))

      val arrow: Int => Option[Int] =
        (x: Int) => if (x > 0) Some(x) else None

      flattenOption(Some(3).map(arrow)) mustBe (Some(3)).flatMap(arrow)
      flattenOption(Some(-3).map(arrow)) mustBe (Some(-3)).flatMap(arrow)
    }

    /**
      * To make reading large chains of flatMaps more pleasant, Scala provides the for-yield construct, which is syntactic sugar for a sequence of flatMaps.
      */
    they("understand that for-yield constructs are just syntactic sugar for a sequence of flatMap operations") {
      def passwordIsCorrect(username: String, plaintext: String): Either[String, Boolean] =
        ___ // copy answer from koan above

      // forComp implements the same program as passwordIsCorrect. The variable names line up with the stubbed out version above.
      def forComp(username: String, plaintext: String) =
        for {
          username   <- Either.right(username)
          uid        <- ___[Either[String, Int]]
          ciphertext <- ___[Either[String, String]]
        } yield hash(plaintext) == ciphertext

      passwordIsCorrect("Alex", "best-password!") mustBe (forComp("Alex", "best-password!"))
      passwordIsCorrect("Ryan", "super-secret-password") mustBe (forComp("Ryan", "super-secret-password"))
      passwordIsCorrect("Broken_User", "roflmao") mustBe (forComp("Broken_User", "roflmao"))
      passwordIsCorrect("Anonymous", "we_are_legion") mustBe (forComp("Anonymous", "we_are_legion"))
    }

    /**
      * The most useful piece of the Monad is flatMap, but to have a Monad, we also need another function called pure.
      */
    they("know how pure works to lift values into Monadic contexts") {
      // cats provides a few ways to invoke pure.
      val y = Monad[Option].pure(2) // the noisy way
      val x = 1.pure[Option]        // the easy way (requires import cats.implicits._)

      x mustBe (Some(1))
      y mustBe (Some(2))

      val a = Monad[List].pure(1) // We often say that pure 'lifts' a value into the context of some effect.

      a mustBe (___)
    }

    /**
      * Like Functors, Monads have to follow the Monad laws.
      */
    they("can implement lawful Monads") {

      /**
        * Here is one way to define a typeclass for Monads
        */
      trait SimpleMonad[F[_]] {
        // pure lifts the value a into the monadic context
        def pure[A](a: A): F[A]

        // flatMap allows you to modify the value inside an effect, while possibly introducing additional complication.
        def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]
      }

      val monadForOption: SimpleMonad[Option] = new SimpleMonad[Option] {
        def pure[A](a: A): Option[A]                                   = ___
        def flatMap[A, B](fa: Option[A])(f: A => Option[B]): Option[B] = ___
      }

      val mfo: SimpleMonad[Option] = monadForOption // just an alias for terseness

      val a = randomInt()

      // f is a Kleisli arrow from Int => Option[String]
      val f: Int => Option[String] = (x) => Some(x + "!")

      // pure acts as the left identity for flatMap.
      mfo.flatMap(mfo.pure(a))(x => Some(x + 2)) mustBe f(a)

      // pure also acts as the right identity for flatMap.
      mfo.flatMap(f(a))(mfo.pure) mustBe f(a)

      // g is a Kleisli arrow from String => Option[Int]
      val g: String => Option[Int] = (s) => s.substring(0, s.length() - 1).toIntOption

      // flatMap must compose in an associative way... that is..
      mfo.flatMap(                                 // it doesn't matter how flatMaps nest,
        mfo.flatMap(Some(1))(f)                    // flatMap with f first
      )(g) mustBe (                                // and then flatMap with g ...or
        mfo.flatMap(Some(1))(x => f(x).flatMap(g)) // apply f(x) and then flatMap on the result
      )                                            // the result should be the same
    }

    /**
      * A lawful map operation can be implemented using only flatMap and pure. By implementing the Monad, we get a Functor 'for-free'.
      */
    they("know that map can be implemented in terms of flatMap and pure") {
      trait SimpleMonad[F[_]] { // same typeclass from before.
        def pure[A](a: A): F[A]
        def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]

        def map[A, B](fa: F[A])(f: A => B): F[B] = ___
      }

      val monadForOption: SimpleMonad[Option] = new SimpleMonad[Option] {
        def pure[A](a: A): Option[A]                                   = ___ // TODO: copy your lawful Monad from above
        def flatMap[A, B](fa: Option[A])(f: A => Option[B]): Option[B] = ___
      }

      monadForOption.map(Some(1))(x => (x + 12) + "!") mustBe (Some("13!"))
      monadForOption.map(None)(_ => throw new RuntimeException("should never be executed")) mustBe (None)
    }

    /**
      * To prove every Monad is a Functor, we just need to provide a lawful instance of Functor[F[_]] for every instance of a Monad[F[_]].
      */
    they("know that every Monad is a Functor") {
      trait SimpleMonad[F[_]] {
        def pure[A](a: A): F[A]
        def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]
      }

      trait SimpleFunctor[F[_]] {
        def map[A, B](fa: F[A])(f: A => B): F[B]
      }

      // the below function defines an instance of SimpleFunctor for any F[_] which has a implicit SimpleMonad[F]
      def functorForAnyMonad[F[_]: SimpleMonad]: SimpleFunctor[F] = new SimpleFunctor[F] {
        def map[A, B](fa: F[A])(f: A => B): F[B] = ___
      }

      implicit val monadForOption: SimpleMonad[Option] = ___ // TODO: copy your lawful monad from above.

      functorForAnyMonad[Option].map(Some(1))(x => (x + 12) + "!") mustBe (Some("13!"))
      functorForAnyMonad[Option].map(None)(_ => throw new RuntimeException("should never be executed")) mustBe (None)

      // Functors must preserve the identity map.
      functorForAnyMonad[Option].map(Some(1))(identity) mustBe (Some(1))
      functorForAnyMonad[Option].map(None)(identity) mustBe (None)

      // Functors must preserve the composition of functions.
      val a = randomInt()
      val f = (x: Int) => x + 1
      val g = (x: Int) => 3 * x

      functorForAnyMonad[Option].map(Some(a))(f compose g) mustBe (functorForAnyMonad[Option]
        .map(functorForAnyMonad[Option].map(Some(a))(f))(g))
    }

    they("appreciate the additional combinators implemented on Monads for 'free'") {
      // flatten can be used to flatten nested monads.
      Monad[Option].flatten(Some(Some(1))) mustBe (___)
      Monad[Option].flatten(Some(None)) mustBe (___)
      Monad[List].flatten(List(List(1, 2, 3), List(0), List(3, 4))) mustBe (___)

      // product maintains the monad structure while lifting two values into a tuple
      Monad[Option].product(Some(1), Some(2)) mustBe (___)
      Monad[Option].product(Some(3), None) mustBe (___)

      // mproduct can be used to pair the value with the output of a Kleisli arrow.
      Monad[Option].mproduct(Some(1))(x => if (x > 0) Some(x) else None) mustBe (___)
      Monad[List].mproduct(List(1, 2, 3))(x => List(x, x + 5)) mustBe (___)
    }

    /**
      * If F[_] is a monad, and G[_] is a functor, then F[G[_]] is also a functor (but not necessarily a Monad).
      *
      * Monads do not compose with Monads in general. Monads do compose with Monad Transformers which we'll see later.
      */
    they("understand that Monad composition does not work in general") {
      // try to complete the definition for flatMap below.
      def flatMap[F[_]: Monad, G[_]: Monad, A, B](fa: F[G[A]])(f: A => F[G[B]]): F[G[B]] = {
        ___
      }

      flatMap(List(Some(1), Some(2)).widen[Option[Int]])(x => List(Some(x), None)) mustBe
        (List(Some(1), None, Some(2), None))
      // comment out the assertion to pass the koan
    }

    /**
      * For some types, it makes sense to have a flatMap, but there's no good way to implement pure.
      */
    they("understand that FlatMap is a weaker version of the Monad typeclass") {
      // the FlatMap typeclass from the cats library is similar
      trait SimpleFlatMap[F[_]] {
        def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]
        def map[A, B](fa: F[A])(f: A => B): F[B] // you have to provide map, since we don't have a pure, we can't define it for you.
      }

      // we can implement FlatMap for Map[K, *]
      def flatMapForMap[K]: SimpleFlatMap[Map[K, *]] = ___

      flatMapForMap[Int].map(Map(1     -> 2, 3 -> 4))(_ + 1) mustBe (Map(1 -> 3, 3 -> 5))
      flatMapForMap[Int].flatMap(Map(1 -> 2, 3 -> 4))(x => Map(x - 1 -> (x + 1))) mustBe (Map(0 -> 3, 2 -> 5))
    }

    they(
      "know that the cats library requires Monads to implement an additional function, tailRecM for stack-safe recursion"
    ) { /* UNDER CONSTRUCTION */ }
  }
}
