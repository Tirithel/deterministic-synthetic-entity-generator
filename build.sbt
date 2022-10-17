ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := Dependencies.Version.scala

Global / onChangedBuildSource := ReloadOnSourceChanges

Compile / resourceDirectory := baseDirectory.value / "./src/main/scala/resources"
Runtime / resourceDirectory := baseDirectory.value / "./src/main/scala/resources"

lazy val root = (project in file("."))
  .settings(Settings.commonSettings)
  .settings(
    name             := "DeterministicSyntheticEntityGenerator",
    idePackagePrefix := Some("org.cmoran"),
    libraryDependencies ++= Seq(
      Dependencies.zio,
      Dependencies.zioStreams,
      Dependencies.zioTest,
      Dependencies.zioTestSbt,
      Dependencies.scalatest,
      Dependencies.scalamock,
      Dependencies.scalatic
    )
  )
