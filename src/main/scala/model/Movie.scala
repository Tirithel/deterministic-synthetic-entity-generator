package org.cmoran
package model

import scala.util.Random

import utils._
import zio.stream.ZStream

case class Movie(seed: Int, title: MovieTitle)
case class MovieTitle(seed: Int, value: String)

object Movie {
  def process(in: ZStream[Any, Any, Int]): ZStream[Any, Any, Movie] = in.map(generate)
  def generate(seed: Int): Movie                                    = Movie(seed, MovieTitle.generate(seed))
}

private object MovieTitle {
  val markov: Markov                      = new Markov()
  lazy val data: List[String]             = scala.io.Source.fromResource("movie_titles.txt").getLines().toList
  var generator: Option[markov.Generator] = None

  def generate(seed: Int): MovieTitle = {
    val rand: Random = new Random(seed)

    val title = generator match {
      case Some(g) => g.generate.replace("#", "")
      case None => {
        generator = Some(markov.generatorFromData(data = data, order = 2, prior = 0.001f, backoff = false, rand = rand))
        generator.get.generate.replace("#", "")
      }
    }

    MovieTitle(seed, title)
  }

}
