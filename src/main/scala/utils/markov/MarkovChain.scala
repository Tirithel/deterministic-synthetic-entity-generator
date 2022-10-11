package org.cmoran
package utils.markov

import scala.annotation.tailrec
import scala.util.Random

case class RandomWalkResult[T](currentStep: T, walk: Seq[T])

case class MarkovChain[T](chain: Map[T, MarkovNode[T]])(
  implicit random:               Random) {
  private val keys = chain.keySet

  private val statesPointingToNonExistingState = chain.values
    .flatMap(_.links)
    .map(_.to)
    .filterNot(keys.contains)

  require(
    statesPointingToNonExistingState.isEmpty,
    s"Outgoing links pointing to a non existing node: $statesPointingToNonExistingState",
  )

  def randomWalk(startingFrom: T, numberOfSteps: Int): RandomWalkResult[T] = {
    require(numberOfSteps > 0)

    @tailrec
    def walk(remainingSteps: Int, walkAccumulator: RandomWalkResult[T]): RandomWalkResult[T] =
      if (remainingSteps == 0) {
        walkAccumulator
      } else {
        val current = walkAccumulator.currentStep
        walk(remainingSteps - 1, RandomWalkResult(chain(current).randomStep(random), current +: walkAccumulator.walk))
      }

    val reverseWalk = walk(numberOfSteps - 1, RandomWalkResult(chain(startingFrom).randomStep(random), Seq(startingFrom)))
    RandomWalkResult(reverseWalk.currentStep, reverseWalk.walk.reverse)
  }

}

case class MarkovNode[T](links: Seq[OutgoingLink[T]]) {

  @tailrec
  final private[markov] def chooseStep(nextPercent: Double, links: Seq[OutgoingLink[T]]): T = links match {
    case x :: _ if nextPercent <= x.prob.toDouble => x.to
    case x :: xs                                  => chooseStep(nextPercent - x.prob.toDouble, xs)
  }

  private[markov] def randomStep(random: Random) = {
    val nextPercent = random.nextDouble() % 100
    chooseStep(nextPercent, links)
  }

}

case class OutgoingLink[T](prob: Probability, to: T)
