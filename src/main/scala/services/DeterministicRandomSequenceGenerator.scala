package org.cmoran
package services

import scala.collection.AbstractIterator
import scala.util.Random

import zio.stream.ZStream

object DeterministicRandomSequenceGenerator {
  def stream: ZStream[Any, Throwable, Int] = ZStream.fromIterator(DeterministicRandomSequenceGenerator().iteratorFromOffset(0))
}

case class DeterministicRandomSequenceGenerator(val seed: Long = 42, val blockSize: Int = 1024) {

  def iteratorFromOffset(offset: Int = 0): DeterministicRandomSequenceIterator =
    DeterministicRandomSequenceIteratorImpl(seed, blockSize, offset)

}

trait DeterministicRandomSequenceIterator extends AbstractIterator[Int] with Iterator[Int]

case class DeterministicRandomSequenceIteratorImpl(
  private val seed:      Long,
  private val blockSize: Int,
  private val offset:    Int) extends DeterministicRandomSequenceIterator {

  val globalRandom: Random = new Random(seed)
  val globalOffset: Int    = offset / blockSize
  // page to block offset
  for (_ <- 0 until globalOffset) globalRandom.nextLong

  var blockRandom: Random = new Random(globalRandom.nextLong())
  var blockOffset: Int    = offset % blockSize
  // page to offset within block
  for (_ <- 0 until blockOffset) blockRandom.nextLong

  override def hasNext: Boolean = true

  override def next(): Int = {
    if (blockOffset == blockSize) incrementBlock()
    nextValue
  }

  private def nextValue: Int = {
    blockOffset += 1
    blockRandom.nextInt()
  }

  private def incrementBlock(): Unit = {
    blockRandom = new Random(globalRandom.nextLong)
    blockOffset += 1
  }

}
