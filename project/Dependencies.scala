import sbt._

object Dependencies {
  // scalatest
  val scalatic  = "org.scalactic" %% "scalactic" % Version.scalatest
  val scalatest = "org.scalatest" %% "scalatest" % Version.scalatest % Test

  // scalamock
  val scalamock = "org.scalamock" %% "scalamock" % Version.scalamock % Test

  // ZIO
  val zio        = "dev.zio" %% "zio"          % Version.zio
  val zioStreams = "dev.zio" %% "zio-streams"  % Version.zio
  val zioKafka   = "dev.zio" %% "zio-kafka"    % Version.zio
  val zioTest    = "dev.zio" %% "zio-test"     % Version.zio % Test
  val zioTestSbt = "dev.zio" %% "zio-test-sbt" % Version.zio % Test

  object Version {
    val scala     = "2.13.8"
    val scalatest = "3.2.12"
    val scalamock = "5.2.0"
    val zio       = "2.0.2"
  }

}
