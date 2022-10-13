package org.cmoran

import org.cmoran.pipelines.{ ContainerEntity, ContainerType }
import org.cmoran.pipelines.ContainerEntity.tagPipe
import org.cmoran.services.ZIODeterministicRandomSequenceGeneratorImpl

import zio._
import zio.stream.{ ZSink, ZStream }

object Application extends ZIOAppDefault {

  val chunkSize = 1024

  def bloomFilter[T](dim1: Int, dim2: Int, h1: T => Int, h2: T => Int): (T => Unit, T => Boolean) = {
    val bloomfilter = Array.ofDim[Int](dim1, dim2)
    println(s"initialized bloomfilter with $dim1, $dim2")

    def add(entity: T): Unit = bloomfilter(h1(entity))(h2(entity))
    def contains(entity: T): Boolean = {
      println(bloomfilter.map(_.mkString(", ")).mkString("\n"))
      bloomfilter(h1(entity))(h2(entity)) != 0
    }

    (add, contains)
  }

  def hash1(s: String): Int = s.length            % 75
  def hash2(s: String): Int = s.split(" ").length % 10

  val (filterAdd, filterContains) = bloomFilter[String](75, 10, hash1, hash2)

  @Override
  def run = for {
    it <- ZStream
      .fromIterator(ZIODeterministicRandomSequenceGeneratorImpl(blockSize = chunkSize).iterator().iterator)
      .run(ZSink.collectAllN(chunkSize))
    entities <- ZStream.fromIterable(it)
      .via(tagPipe)
      .run(ZSink.collectAll)
    _ <- ZIO.foreach(entities) { entity =>
      filterAdd(entity.tagValue)
      Console.printLine(s"[${entity.tagValue}] written to bloomfilter.")
    }
    _ <- Console.printLine(filterContains("resounds for"))
  } yield {}

}
