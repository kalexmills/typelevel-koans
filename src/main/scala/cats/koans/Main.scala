package cats.koans

import cats.instances.string._
import cats.syntax.semigroup._
import cats._
object Main extends App {

  println("Hello " |+| "Cats!")
}
