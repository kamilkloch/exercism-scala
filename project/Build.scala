import java.io.File

import sbt.Keys._
import sbt._

import scala.io.Source
import scala.util.parsing.json.{JSON, JSONArray, JSONObject}

object XScalaBuild extends Build {

  scalaVersion := "2.12.5"
  name := "xscala"

  val commonSettings = Seq(
    updateOptions := updateOptions.value.withCachedResolution(true),
    scalaVersion := "2.12.5",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % "test",
    javacOptions += "-Xmx2G",
    scalacOptions ++= Seq(
      "-deprecation", // Emit warning and location for usages of deprecated APIs.
      "-encoding", "utf-8", // Specify character encoding used by source files.
      "-explaintypes", // Explain type errors in more detail.
      "-feature", // Emit warning and location for usages of features that should be imported explicitly.
      "-language:existentials", // Existential types (besides wildcard types) can be written and inferred
      "-language:experimental.macros", // Allow macro definition (besides implementation and application)
      "-language:higherKinds", // Allow higher-kinded types
      "-language:implicitConversions", // Allow definition of implicit functions called views
      "-unchecked", // Enable additional warnings where generated code depends on assumptions.
      "-Xcheckinit", // Wrap field accessors to throw an exception on uninitialized access.
      //"-Xfatal-warnings", // Fail the compilation if there are any warnings.
      "-Xfuture", // Turn on future language features.
      "-Xlint:adapted-args", // Warn if an argument list is modified to match the receiver.
      "-Xlint:by-name-right-associative", // By-name parameter of right associative operator.
      "-Xlint:constant", // Evaluation of a constant arithmetic expression results in an error.
      "-Xlint:delayedinit-select", // Selecting member of DelayedInit.
      "-Xlint:doc-detached", // A Scaladoc comment appears to be detached from its element.
      "-Xlint:inaccessible", // Warn about inaccessible types in method signatures.
      "-Xlint:infer-any", // Warn when a type argument is inferred to be `Any`.
      "-Xlint:missing-interpolator", // A string literal appears to be missing an interpolator id.
      "-Xlint:nullary-override", // Warn when non-nullary `def f()' overrides nullary `def f'.
      "-Xlint:nullary-unit", // Warn when nullary methods return Unit.
      "-Xlint:option-implicit", // Option.apply used implicit view.
      "-Xlint:package-object-classes", // Class or object defined in package object.
      "-Xlint:poly-implicit-overload", // Parameterized overloaded implicit methods are not visible as view bounds.
      "-Xlint:private-shadow", // A private field (or class parameter) shadows a superclass field.
      "-Xlint:stars-align", // Pattern sequence wildcard must align with sequence component.
      "-Xlint:type-parameter-shadow", // A local type parameter shadows a type already in scope.
      "-Xlint:unsound-match", // Pattern match may not be typesafe.
      "-Yno-adapted-args", // Do not adapt an argument list (either by inserting () or creating a tuple) to match the receiver.
      "-Ypartial-unification", // Enable partial unification in type constructor inference
      "-Ywarn-dead-code", // Warn when dead code is identified.
      "-Ywarn-extra-implicit", // Warn when more than one implicit parameter section is defined.
      "-Ywarn-inaccessible", // Warn about inaccessible types in method signatures.
      "-Ywarn-infer-any", // Warn when a type argument is inferred to be `Any`.
      "-Ywarn-nullary-override", // Warn when non-nullary `def f()' overrides nullary `def f'.
      "-Ywarn-nullary-unit", // Warn when nullary methods return Unit.
      "-Ywarn-numeric-widen", // Warn when numerics are widened.
      "-Ywarn-unused:implicits", // Warn if an implicit parameter is unused.
      "-Ywarn-unused:imports", // Warn if an import selector is not referenced.
      "-Ywarn-unused:locals", // Warn if a local definition is unused.
      "-Ywarn-unused:params", // Warn if a value parameter is unused.
      "-Ywarn-unused:patvars", // Warn if a variable bound in a pattern is unused.
      "-Ywarn-unused:privates", // Warn if a private member is unused.
      "-Ywarn-value-discard" // Warn when non-Unit expression results are unused.
    ),

    (unmanagedSources in Test) <<= (unmanagedSources in Test, sourceManaged in Test) map {
      case (testSources, managedTestDir) =>
        copyTestsAndStripPending(testSources, managedTestDir)
    }
  )

  override def projects: Seq[Project] = {
    fetchProblemNames("config.json").map {
      problemName => Project(problemName, new File(problemName)).settings(commonSettings: _*)
    }
  }

  def fetchProblemNames(configJsonFile: String): Seq[String] = {
    val configStr = Source.fromFile(configJsonFile, "UTF-8").getLines().mkString
    val configObj = JSON
      .parseRaw(configStr)
      .map(_.asInstanceOf[JSONObject].obj)
      .getOrElse {
        throw new IllegalArgumentException(s"Could not parse $configJsonFile as JSON")
      }
    configObj
      .get("exercises")
      .map(_.asInstanceOf[JSONArray].list)
      .getOrElse {
        throw new IllegalArgumentException(s"Could not find array 'exercises' in $configJsonFile")
      }
      .collect { case problem: JSONObject => problem.obj("slug").toString }
  }

  def copyTestsAndStripPending(tests: Seq[File], managedSourceDir: File): Seq[File] = {
    tests.map { originalTestFile =>
      val filename = originalTestFile.name
      val newTestFile = managedSourceDir / filename
      IO.writeLines(newTestFile, IO.readLines(originalTestFile).filter(_.trim != "pending"))
      newTestFile
    }
  }
}
