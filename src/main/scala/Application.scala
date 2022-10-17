package org.cmoran

import scala.language.postfixOps

import model._
import services.DeterministicRandomSequenceGenerator
import zio._
import zio.stream._

object Application extends ZIOAppDefault {

  val chunkSize = 1024

  var count = 0

  private lazy val deterministicRandomSequenceGenerator: ZStream[Any, Throwable, Int] = DeterministicRandomSequenceGenerator.stream

  override def run = Person
    .process(deterministicRandomSequenceGenerator)
    .run(ZSink.collectAllN[Person](3)
      .map(personChunk =>
        for {
          person <- personChunk

          fname = person.forename.value
          lname = person.surname.value
        } yield println(s"$fname $lname"))) *> Movie.process(deterministicRandomSequenceGenerator).run(ZSink.collectAllN[Movie](3)
    .map(movieChunk =>
      for {
        movie <- movieChunk

        title = movie.title
      } yield println(s"$title")))

}
