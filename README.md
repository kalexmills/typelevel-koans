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

#### Ready for Testing

The below modules are ready for testing. Please open a ticket for any issues.

* **Alpha Testing** [`Monoid`](https://github.com/kalexmills/typelevel-koans/blob/master/src/test/scala/cats/koans/01-Monoid.scala), [`Typeclasses`](https://github.com/kalexmills/typelevel-koans/blob/master/src/test/scala/cats/koans/02-Typeclasses.scala), [`Functor`](https://github.com/kalexmills/typelevel-koans/blob/master/src/test/scala/cats/koans/03-Functor.scala), [`Monad`](https://github.com/kalexmills/typelevel-koans/blob/master/src/test/scala/cats/koans/04-Monad.scala)

#### Roadmap:

5. Monad Transformers
    * can reason about the effect of combined flatMap operations
    * can compose multiple transformers to create arbitrarily nested stacks

6. Effects and IO
    * Understand that referential transparency aids local reasoning
    * Know that allocating memory is an effect
    * Know that modifying memory is an effect
    * Know that randomness and system calls rely on mutable memory (and so are effects)
    * Can control effects by writing effectful functions
    * Appreciate the need for a common effect powerful enough to handle arbitrary side-effects.
    * Know to use a general-purpose 'computation monad' like IO to wrap effects.
    * Understand how to use IO to suspend execution of code

7. Bracket
    * Can use bracket as a pure replacement for tryFinally
    * Understand ExitCase
    
8. Semigroupal -- computations when order doesn't matter
