package cats.koans

import cats._
import cats.implicits._
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.CancelAfterFailure
import Util._
import java.{util => ju}
import java.nio.charset.CharsetDecoder
import java.nio.charset.Charset

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
          .flatMap(___)                   // use flatMap here to return a Left("div0") in case y is zero
          .map((safeY: Int) => x / safeY) // divide x by y

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
          .flatMap(___) // guard against division by zero in one flatMap and perform the division in the other
          .flatMap(___)

      divideExcitingNumbers("16!!!", "4!") mustBe (Some(4.0))
      divideExcitingNumbers("3", "1") mustBe (None)   // only exciting numbers allowed!
      divideExcitingNumbers("3!", "0!") mustBe (None) // no division by zero, even if it's exciting!
    }

    /**
      *   when F[_]: Either[B, _]      the complication is that the value of the type might actually be a value of type B
      */
    they("understand how to use the Either monad to handle the effect of mutually exclusive values of different types") {
      /* UNDER CONSTRUCTION */
    }

    /**
      *   when F[_] = List[_]      the complication is that there might be zero or zillions of values for the type.
      */
    they("understand how to use the List monad to handle the effect of arbitrarily many values") {
      "abcd".toList                                // convert to a list of characters
        .map(___)                                  // convert each character to a string
        .flatMap(___)                              // double each letter in the string
        .flatMap(___) mustBe                       // do something else ;)
        (List("a!", "a!", "b!", "b!", "c!", "c!")) // no d's in the response
    }

    /**
      * Other more complicated (and therefore more useful) Monadic effects are covered in other modules. They are all built around
      * Monads and other typeclasses.
      *
      *   when F[_] = Eval[_]      the complication is that the value might be computed lazily.
      *   when F[_] = Future[_]    the complication is that you might have to wait to get your value (and lots can go wrong)
      *   when F[_] = Reader[_]    the complication is that you need to provide some other value in order for your value to be computed.
      *   when F[_] = Writer[_]    the complication is that the computation is carrying along an additional value being written to (e.g. logs).
      *   when F[_] = IO[_]        the complication is that to get your value, you have to run an arbitrarily complex program which may
      *                            involve concurrency, system-level calls, and potential interaction with other systems.
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
      * To make reading large chains of easier, Scala provides the for-yield construct, which is syntactic sugar for a sequence of flatMaps.
      */
    they("understand that for-yield constructs are just syntactic sugar for a sequence of flatMap operations") {
      val userIds        = Map("Alex" -> 1, "Ryan"                 -> 2, "Denver"       -> 3, "Broken_User" -> 4)
      val passwordHashes = Map(1      -> "YmVzdC1wYXNzd29yZCEK", 2 -> "YWJjMTIzCg==", 3 -> "bDMzdGhheG9yCg==")
      def hash(plaintext: String) =
        new String(ju.Base64.getEncoder().encode(plaintext.getBytes())) // somewhat less than secure

      def passwordIsCorrect(username: String, plaintext: String): Either[String, Boolean] =
        Either
          .right(username)
          .flatMap(username => ___) // retrieve uid or Left("no_uid")
          .flatMap(uid => ___)      // retrieve ciphertext or Left("no_pwd")
          .map(ciphertext => ___)   // compare ciphertext to plaintext

      // forComp implements the same program as passwordIsCorrect. Notice how the variable names line up with the stubbed out version above.
      def forComp(username: String, plaintext: String) =
        for {
          username   <- Either.right(username)
          uid        <- Either.fromOption(userIds.get(username), "no_uid")
          ciphertext <- Either.fromOption(passwordHashes.get(uid), "no_pwd")
        } yield hash(plaintext) == ciphertext

      passwordIsCorrect("Alex", "best-password!") mustBe (forComp("Alex", "best-password!"))
      passwordIsCorrect("Ryan", "super-secret-password") mustBe (forComp("Ryan", "super-secret-password"))
      passwordIsCorrect("Broken_User", "roflmao") mustBe (forComp("Broken_User", "roflmao"))
      passwordIsCorrect("Anonymous", "we_are_legion") mustBe (forComp("Anonymous", "we_are_legion"))
    }

    /**
      * The most useful part of the Monad is the flatMap, but to have a Monad, we also need another function called pure.
      */
    they("know how pure works to lift values into Monadic contexts") {
      // cats provides a few ways to invoke pure.
      val y = Monad[Option].pure(2) // the hard way
      val x = 1.pure[Option]        // the easy way

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

      // Every Monad has to satisfy the laws but for simple monads there's really only one way to get it right. Focus on making
      // the types line up.
      val monadForOption: SimpleMonad[Option] = new SimpleMonad[Option] {
        def pure[A](a: A): Option[A]                                   = ___
        def flatMap[A, B](fa: Option[A])(f: A => Option[B]): Option[B] = ___
      }

      val mfo: SimpleMonad[Option] = monadForOption // just an alias for terseness

      val a = randomInt()

      // f is a Kleisli arrow from Int => Option[String]
      val f: Int => Option[String] = (x) => Some(x + "!")

      // pure acts as the left identity for flatMap. That is to say, wrapping up a value using pure and then applying a Kleisli
      // arrow must be the same as applying that Kleisli arrow
      mfo.flatMap(mfo.pure(a))(x => Some(x + 2)) mustBe f(a)

      // pure also acts as the right identity for flatMap. That is to say, applying a Kleisli arrow and then using pure as the second argument
      // to flat map yields the same value as applying the Kleisli
      mfo.flatMap(f(a))(mfo.pure) mustBe f(a)

      // g is a Kleisli arrow from String => Option[Int]
      val g: String => Option[Int] = (s) => s.substring(0, s.length() - 1).toIntOption

      // flatMap must compose in an associative way.
      mfo.flatMap(                                 // it doesn't matter how flatMaps nest,
        mfo.flatMap(Some(1))(f)                    // flatMap with f first
      )(g) mustBe (                                // and then flatMap with g ...or
        mfo.flatMap(Some(1))(x => f(x).flatMap(g)) // apply f(x) and then flatMap on the result
      )                                            // the result should be the same
    }

    /**
      * One example about getting functionality for cheap is the fact that a lawful map operation can be implemented using only flatMap and pure.
      * By implementing the Monad, we get a Functor 'for-free'.
      */
    they("know that map can be implemented in terms of flatMap and pure") {
      trait SimpleMonad[F[_]] { // same typeclass from before.
        def pure[A](a: A): F[A]
        def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]

        // implement a lawful map operation using only flatMap and pure. (follow the types).
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
      trait SimpleMonad[F[_]] { // same typeclass from before.
        def pure[A](a: A): F[A]
        def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]
      }

      trait SimpleFunctor[F[_]] { // same typeclass from module 2
        def map[A, B](fa: F[A])(f: A => B): F[B]
      }

      // the below function defines an instance of SimpleFunctor for any F[_] which has a implicit SimpleMonad[F]
      def functorForAnyMonad[F[_]: Monad]: SimpleFunctor[F] = new SimpleFunctor[F] {
        def map[A, B](fa: F[A])(f: A => B): F[B] = ___
      }

      val monadForOption: SimpleMonad[Option] = ___ // TODO: copy your lawful monad from above.

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

    they(
      "know that the cats library requires Monads to implement an additional function, tailRecM for stack-safe recursion"
    ) {}
  }
}
