package org.cmoran
package model

import scala.util.Random

import utils._
import zio.stream.ZStream

// type ZPipeline[Env, Err, In, Out] = ZStream[Env, Err, In] => ZStream[Env, Err, Out]

case class Person(seed: Int, surname: Surname, forename: Forename)
case class Surname(seed: Int, value: String)
case class Forename(seed: Int, value: String)

object Person {
  def process(in: ZStream[Any, Any, Int]): ZStream[Any, Any, Person] = in.map(generate)
  def generate(seed: Int): Person                                    = Person(seed, Surname.generate(seed), Forename.generate(seed))
}

private object Surname {
  val markov: Markov                      = new Markov()
  lazy val data: List[String]             = scala.io.Source.fromResource("american_surnames.txt").getLines().toList
  var generator: Option[markov.Generator] = None

  def generate(seed: Int): Surname = {
    val rand: Random = new Random(seed)

    val name = generator match {
      case Some(g) => g.generate.replace("#", "")
      case None => {
        generator = Some(markov.generatorFromData(data = data, order = 3, prior = 0.001f, backoff = false, rand = rand))
        generator.get.generate.replace("#", "")
      }
    }

    Surname(seed, name)
  }

}

private object Forename {
  val markov: Markov                      = new Markov()
  lazy val data: List[String]             = scala.io.Source.fromResource("american_forenames.txt").getLines().toList
  var generator: Option[markov.Generator] = None

  def generate(seed: Int): Forename = {
    val rand: Random = new Random(seed)

    val name = generator match {
      case Some(g) => g.generate.replace("#", "")
      case None => {
        generator = Some(markov.generatorFromData(data = data, order = 3, prior = 0.001f, backoff = false, rand = rand))
        generator.get.generate.replace("#", "")
      }
    }

    Forename(seed, name)
  }

}
