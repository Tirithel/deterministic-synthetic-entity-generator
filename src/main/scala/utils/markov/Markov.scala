package org.cmoran
package utils.markov

import scala.annotation.tailrec
import scala.util.Random

private case class Link[T](from: T, to: T)

private case class To[T](to: T, times: Int) {
  private[markov] def withProb(prob: Probability) = ToWithProb(to, prob)
}

private case class ToWithProb[T](to: T, prob: Probability)

class Markov[T] {

  def train(trainingSet: Seq[T])(
    implicit random: Random): MarkovChain[T] = {
    val occurrences = trainingSet match {
      case Nil | _ :: Nil | _ :: _ :: Nil => throw new IllegalArgumentException("input too small")
      case first :: others                => parseLink(Seq.empty, first, others)
    }

    val linksByFrom            = groupLinkByFrom(trainingSet, occurrences)
    val fromToTotalOutputSteps = linksByFrom.map(item => (item._1, countTotalNextStateOccurrences(item._2)))
    val fromToDstWithCount     = linksByFrom.map(item => (item._1, countTo(item._2)))
    val fromToDstWithProb      = fromToDstWithCount.map(item => (item._1, countToProb(fromToTotalOutputSteps, item._1, item._2)))

    val markovBuilder = fromToDstWithProb.foldRight(ChainBuilder[T]()) { (item, chainBuilder) =>
      val from         = item._1
      val destinations = item._2

      destinations.foldRight(chainBuilder)((to, builder) => builder.linkTo(from, to.to, to.prob))
    }

    markovBuilder.build()
  }

  @tailrec
  private def parseLink(accumulator: Seq[Link[T]], prevState: T, trainingSet: Seq[T]): Seq[Link[T]] = trainingSet match {
    case Nil             => accumulator
    case nextState :: xs => parseLink(Link(prevState, nextState) +: accumulator, nextState, xs)
  }

  private def countTotalNextStateOccurrences(occurs: Seq[Link[T]]) = occurs.size

  private def countTo(occurs: Seq[Link[T]]): Seq[To[T]] = {
    val grouped      = occurs.groupBy(_.to)
    val groupedCount = grouped.map(item => (item._1, item._2.size))
    groupedCount.map(item => To(item._1, item._2)).toSeq
  }

  private def countToProb(total: Map[T, Int], from: T, occurs: Seq[To[T]]): Seq[ToWithProb[T]] = occurs.map { x =>
    val totalForX = total(from)
    val prob      = new Probability(x.times, totalForX)
    x.withProb(prob)
  }

  private def groupLinkByFrom(trainingSet: Seq[T], occurrences: Seq[Link[T]]) = {
    val lastStep :: nonLasts = trainingSet.reverse
    val isLastStepUnique     = !nonLasts.contains(lastStep)

    val stateWithNoLast = occurrences.groupBy(link => link.from)
    val states =
      if (isLastStepUnique) {
        // forcing the chain to remain last step if the last state is unique
        stateWithNoLast + (lastStep -> Seq(Link(lastStep, lastStep)))
      } else {
        stateWithNoLast
      }
    states
  }

}

object Markov {

  def fromSequenceOfSteps[T](trainingSet: Seq[T])(
    implicit random: Random): MarkovChain[T] = new Markov[T]().train(trainingSet)

}
