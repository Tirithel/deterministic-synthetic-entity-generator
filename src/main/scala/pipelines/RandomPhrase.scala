package org.cmoran
package pipelines

import scala.util.{ Random, Using }

import org.cmoran.utils.markov.{ Markov, MarkovChain }

import zio.stream.ZPipeline

case class RandomPhrase(val fromSeed: Int, val value: String) {
  override def toString = value
  def debugString       = s"$fromSeed generated: RandomPhrase($value)"
}

object RandomPhrase {
  // TODO: Config
  val maxLength: Int = Math.min(10, PhraseChain.wordSeq.length - 1)

  val phrasePipeline: ZPipeline[Any, Nothing, Int, RandomPhrase] = ZPipeline.map(fromInt)

  def fromInt(seed: Int): RandomPhrase = {
    PhraseChain.localMarkovRand = new Random(seed)

    // first random is for offset second is for distance
    val offset   = Math.abs(PhraseChain.localMarkovRand.nextInt % PhraseChain.wordSeq.length)
    val distance = 1 + Math.abs(PhraseChain.localMarkovRand.nextInt % maxLength)

    val phrase = PhraseChain.chain.randomWalk(PhraseChain.wordSeq(offset), distance)
      .walk
      .mkString(" ")

    RandomPhrase(seed, phrase)
  }

  object PhraseChain {

    // generated from seed
    implicit var localMarkovRand: Random = new Random

    // should be precompiled
    final lazy val wordSeq: Seq[String] = {
      val resourcesPath = getClass.getResource("../resources/phrase-chain.txt")

      Using(io.Source.fromFile(resourcesPath.getPath)) { file =>
        file.mkString
          .split("\\s+")
          .toList
          .map(_.toLowerCase)
      }.getOrElse(Seq[String]("hello", "world", "I'm", "a", "markov", "chain"))
    }

    // NEEDS to be lazy to get the right implicit randomness seed.
    lazy val chain: MarkovChain[String] = Markov.fromSequenceOfSteps(wordSeq)
  }

}
