package org.cmoran
package utils

import scala.collection.{ mutable, SortedSet }
import scala.language.postfixOps
import scala.util.Random

class Markov {
  var generator: Option[Generator] = None

  /**
    * @param model
    *   Precompiled MarkovModel to generate against.
    * @param order
    *   The highest order model used by this generator. Generators own models of
    *   order 1 through order "n". Generators of order "n" look back up to "n"
    *   characters when choosing the next character.
    * @param prior
    *   Dirichlet prior, acts as an additive smoothing factor. The prior adds a
    *   constant probability that a random letter is picked from the alphabet
    *   when generating a new letter.
    * @param backoff
    *   Whether to fall back to lower orders of models when a higher-order model
    *   fails to generate a letter.
    * @param rand
    *   optionally seeded random value for deterministic outcomes.
    *
    * @return
    *   Generator
    */
  def generatorFromModel(
    model: MarkovModel,
    order: Int = 3,
    prior: Float = 0.001f,
    backoff: Boolean = true,
    rand: Random = new Random()): Generator = generator match {
    case Some(gen) => gen
    case None =>
      generator = Some(Generator(order, prior, backoff, model)(rand))
      generator.get
  }

  /**
    * @param data
    *   Data used to create MarkovModels
    * @param order
    *   The highest order model used by this generator. Generators own models of
    *   order 1 through order "n". Generators of order "n" look back up to "n"
    *   characters when choosing the next character.
    * @param prior
    *   Dirichlet prior, acts as an additive smoothing factor. The prior adds a
    *   constant probability that a random letter is picked from the alphabet
    *   when generating a new letter.
    * @param backoff
    *   Whether to fall back to lower orders of models when a higher-order model
    *   fails to generate a letter.
    * @param rand
    *   optionally seeded random value for deterministic outcomes.
    *
    * @return
    *   Generator
    */
  def generatorFromData(
    data: List[String],
    order: Int = 3,
    prior: Float = 0.001f,
    backoff: Boolean = true,
    rand: Random = new Random()): Generator = generator match {
    case Some(gen) => gen
    case None =>
      generator = Some(Generator(order, prior, backoff, MarkovModel.make(data, order, prior, backoff))(rand))
      generator.get
  }

  /**
    * @param order
    *   The highest order model used by this generator. Generators own models of
    *   order 1 through order "n". Generators of order "n" look back up to "n"
    *   characters when choosing the next character.
    * @param prior
    *   Dirichlet prior, acts as an additive smoothing factor. The prior adds a
    *   constant probability that a random letter is picked from the alphabet
    *   when generating a new letter.
    * @param backoff
    *   Whether to fall back to lower orders of models when a higher-order model
    *   fails to generate a letter.
    * @param model
    *   The array of Markov models used by this generator, starting from highest
    *   order to lowest order.
    * @param rand
    *   optionally seeded random value for deterministic outcomes.
    */
  case class Generator(
    order:             Int,
    prior:             Float,
    backoff:           Boolean = true,
    model:             MarkovModel)(
    implicit val rand: Random) {

    /**
      * Generates a word.
      *
      * @return
      *   The generated word.
      */
    def generate: String = {
      var word = "#".repeat(order)

      var letter = getLetter(word)
      while (letter != null && letter != "#") {
        if (letter != null) word = word + letter
        letter = getLetter(word)
      }

      word
    }

    /**
      * Generates the next letter in a word.
      *
      * @param context
      *   The context the models will use for generating the next letter.
      * @return
      *   The generated letter, or null if no model could generate one.
      */
    private def getLetter(word: String): String = {
      var letter: String = null
      var context        = word.substring(word.length - order, word.length)

      for (model <- model.models) {
        if (letter == null || letter == "#") {
          letter = model.generate(context, rand)
          context = context.substring(1)
        }
      }

      letter
    }

  }

  object MarkovModel {

    def make(data: List[String], order: Int, prior: Float, backoff: Boolean): MarkovModel = {
      var letters: SortedSet[Char] = SortedSet()
      var models: List[Model]      = List()
      var domain: List[String]     = List()

      for (pointer <- data.indices) {
        data(pointer).foreach(letter => letters = letters + letter)
      }

      domain = letters.map(c => s"$c").toList.prepended("#")

      if (backoff) {
        for (i <- 0 until order) models = models.appended(Model(data.map(i => i), order - i, prior, domain))
      } else {
        models = models.appended(Model(data.map(i => i), order, prior, domain))
      }

      models.foreach { m =>
        m.train(m.data)
        m.build()
      }

      new MarkovModel(models)
    }

  }

  case class MarkovModel(var models: List[Model])

  /**
    * @param data
    *   The training data for the model, an array of words.
    * @param order
    *   The order of the model i.e. how many characters this model looks back.
    * @param prior
    *   Dirichlet prior, like additive smoothing, increases the probability of
    *   any item being picked.
    * @param domain
    *   The unique character set used in this model
    */
  case class Model(data: List[String], order: Int, prior: Float, domain: List[String]) {
    private val observations: mutable.Map[String, List[String]] = mutable.Map[String, List[String]]()
    private val chains: mutable.Map[String, List[Float]]        = mutable.Map[String, List[Float]]()

    def train(data: List[String]): Unit = {
      data.foreach { item =>
        val str = ("#".repeat(order)) + item + "#"

        for (index <- 0 until str.length - order) {

          val key   = str.substring(index, index + order)
          var value = observations.getOrElse(key, List[String]())

          value = value.appended(s"${str.charAt(index + order)}")

          observations.put(key, value)
        }
      }

      println(s"training done observations=${observations.toList.length}.")
    }

    def build(): Unit = {
      for (ctx <- observations.keys) {
        for (prediction <- domain) {
          chains.get(ctx) match {
            case Some(value) => chains.put(ctx, value.appended(prior + countMatches(observations.get(ctx), prediction)))
            case None        => chains.put(ctx, List(prior + countMatches(observations.get(ctx), prediction)))
          }
        }
      }
      println(s"done building chains=${chains.keys.toList.length}.")
    }

    private def countMatches(arr: Option[List[String]], str: String): Int = arr match {
      case Some(value) => value.count(_ == str)
      case None        => 0
    }

    def generate(ctx: String, rand: Random = new Random()): String = {
      val value = chains.get(ctx).orNull
      if (value == null) null
      else domain(select(value, rand))
    }

    private def select(chain: List[Float], rand: Random): Int = {
      var totals: List[Float] = List[Float]()
      var accumulator: Float  = 0f

      for (weight <- chain) {
        accumulator = accumulator + weight
        totals = totals ++ List(accumulator)
      }

      val random = rand.nextDouble() * accumulator

      for (index <- totals.indices) {
        if (random < totals(index)) return index
      }

      0
    }

  }

}
