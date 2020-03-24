name := "cats-koans"
version := "0.0.1-SNAPSHOT"

javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint")

name := "s5-oidc"
organization := "com.s5stratos"

scalaVersion := "2.13.1"

DefaultOptions.addCredentials

ThisBuild / resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots"),
  "Artifactory Dev SBT" at "https://s5stratos.jfrog.io/s5stratos/sbt-dev/",
  "Artifactory Release SBT" at "https://s5stratos.jfrog.io/s5stratos/sbt-release/",
  "Artifactory Snapshot MVN" at "https://s5stratos.jfrog.io/s5stratos/libs-snapshot/",
  "Artifactory Release MVN" at "https://s5stratos.jfrog.io/s5stratos/libs-release/",
  Classpaths.typesafeReleases
)

scalacOptions ++= Seq(
  "-language:existentials",
  "-language:higherKinds"
)

libraryDependencies += compilerPlugin("org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full)
libraryDependencies += "org.typelevel" %% "cats-core" % "2.1.1"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.1.1" % Test
