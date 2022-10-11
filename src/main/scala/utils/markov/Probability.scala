package org.cmoran
package utils.markov

class Probability(x: Int, y: Int) {
  require(y != 0, "denominator must be non-zero")

  private def gcd(a: Int, b: Int): Int = Math.abs(if (b == 0) a else gcd(b, a % b))

  val g = gcd(x, y)

  val numer    = x / g
  val denom    = y / g
  val toDouble = x.toDouble / y.toDouble

  require(toDouble <= 1, "probability should be not greater that 1")
  require(toDouble >= 0, "probability should be greater than 0")

  def add(r: Probability): Probability = new Probability(numer * r.denom + r.numer * denom, denom * r.denom)

  def +(r: Probability): Probability = add(r)

  def less(r: Probability): Boolean = numer * r.denom < r.numer * denom

  def canEqual(other: Any): Boolean = other.isInstanceOf[Probability]

  override def equals(other: Any): Boolean = other match {
    case that: Probability => (that canEqual this) &&
      numer == that.numer &&
      denom == that.denom
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(numer, denom)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }

  override def toString: String = s" ($numer/$denom) "
}

object Probability {
  val zero = new Probability(0, 1)
  val one  = new Probability(1, 1)

  implicit def ordering[A <: Probability]: Ordering[A] = Ordering.by(e => e.toDouble)
}
