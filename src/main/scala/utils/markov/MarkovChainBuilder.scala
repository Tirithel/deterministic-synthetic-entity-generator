package org.cmoran
package utils.markov

import scala.util.Random

case class ChainBuilder[T](chain: Map[T, NodeBuilder[T]] = Map.empty[T, NodeBuilder[T]])(
  implicit random:                Random) {

  def linkTo(from: T, to: T, prob: Probability): ChainBuilder[T] = {
    val chainWithFrom =
      if (!chain.contains(from)) {
        chain + (from -> NodeBuilder(from))
      } else {
        chain
      }

    val chainWithTo =
      if (!chainWithFrom.contains(to)) {
        chainWithFrom + (to -> NodeBuilder(to))
      } else {
        chainWithFrom
      }

    val fromBuilder    = chainWithTo(from)
    val newFromBuilder = fromBuilder.linkedTo(to, prob)
    copy((chain - from) + (from -> newFromBuilder))
  }

  def build(): MarkovChain[T] = MarkovChain(chain.view.mapValues(_.toNode).toMap)
}

case class NodeBuilder[T](value: T, links: Seq[OutgoingLink[T]] = List.empty) {
  private[markov] def linkedTo(to: T, prob: Probability) = copy(links = OutgoingLink(prob, to) +: this.links)

  private[markov] def toNode: MarkovNode[T] = {
    require(
      links.foldRight(Probability.zero)((link, probSum) => probSum + link.prob) == Probability.one,
      "The sum of all the outgoing probabilities should be 1.0")
    MarkovNode(links.sortBy(_.prob).reverse)
  }

}
