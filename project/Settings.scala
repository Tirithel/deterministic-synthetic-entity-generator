import sbt._
import sbt.Keys._

import org.scalafmt.sbt.ScalafmtPlugin.autoImport.scalafmtOnCompile

import Dependencies._

object Settings {

  val commonSettings = Seq(
    scalaVersion        := Version.scala,
    logLevel            := Level.Info,
    scalafmtOnCompile   := true,
    testFrameworks      := Seq(new TestFramework("zio.test.sbt.ZTestFramework")),
    Global / cancelable := true, // https://github.com/sbt/sbt/issues/2274
    Global / fork       := true, // ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

    resolvers ++= Common.resolvers,
  )

}
