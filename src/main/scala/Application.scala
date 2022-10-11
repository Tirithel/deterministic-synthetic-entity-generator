package org.cmoran

import org.cmoran.pipelines.ContainerEntity
import org.cmoran.pipelines.ContainerEntity.tagPipe
import org.cmoran.services.ZIODeterministicRandomSequenceGeneratorImpl

import zio._
import zio.stream.{ ZSink, ZStream }

object Application extends ZIOAppDefault {

  val chunkSize = 1024

  @Override
  def run = for {
    it <- ZStream
      .fromIterator(ZIODeterministicRandomSequenceGeneratorImpl(blockSize = chunkSize).iterator().iterator)
      .run(ZSink.collectAllN(chunkSize))
    entities <- ZStream.fromIterable(it)
      .via(tagPipe)
      .run(ZSink.collectAll)
    _ <- ZIO.foreach(entities)(e => Console.printLine(e.toString))
  } yield ExitCode.success // processes one chunk then exits

}
