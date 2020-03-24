## typelevel-koans

Koans focused on using key features of the typelevel stack. Sprinkled with some notes on theory.

#### Using this Repository

To use this repository, clone or fork it and check out the classes under `src/test`. They are named in
the order they should be attempted.

The tests written in each file are failing. To gain further mastery along the path to enlightenment, read 
the comments and complete each test so that it passes. Any structure containing two or more underscores 
`__` are meant to be replaced by the learner.

##### Helpful SBT Commands:

Usually you only want to run the tests you're working on. To make this easy, the koan modules are numbered
in increasing order, so you can run 
```
sbt:cats-koans> testOnly *01
```
to run all the tests in the first module. The tests are configured to be completed in order, so all
tests after the first failure are discarded.


Run all tests in this repository (not recommended usually)
```
sbt:cats-koans> test
```

#### Roadmap:

1. Semigroup and Monoid
  a. use the Monoid instance for Int, String directly
  b. use the Monoid instance for Int, String via syntax imports
  c. use a generic function requiring the Monoid trait on a type argument
  d. write a generic function requiring the Monoid trait on its type argument
  e. define and use a reverse Semigroup instance for cats.data.NonEmptyList
  f. define and use a multiplicative Monoid instance for Double
2. Typeclasses in Scala via Implicits
  a. Explain type-constructors
  b. Explain and use type-bounds
  c. Demonstrate where implicit typeclasses should be defined
  d. Demonstrate summoners.
  e. Demonstrate a new typeclass Reversable[A]
  f. Demonstrate PML (Pimp My Library) via implicit conversions
3. Functor
  b. Option
  c. List
  d. Either[A, B] as a right-biased Functor
  e. implement Functor for a Tuple2
  f. implement Functor for a Tree
  g. Functors compose
  g. function types can have a Functor instance defined.
  h. Functor laws -- map and pure must play nicely together
  i. Functor trait bounds on a generic def. Introducing F[_]
5. Monad
  a. composition of A => B, B => C
  b. composition of A => F[B], B => F[C]
  a. Functor plus flatMap
  b.
  a. List
  b. 