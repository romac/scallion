/* Copyright 2019 EPFL, Lausanne
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package scallion
package parsing
package visualization

import scala.collection.mutable.{Queue, StringBuilder}

/** Contains utilities to vizualize parsers as BNF grammars.
  * Expected to be mixed-in [[scallion.parsing.Parsers]].
  *
  * @groupprio grammar 1
  * @groupname grammar Grammar
  *
  * @groupprio symbol 2
  * @groupname symbol Symbols
  */
trait Grammars[Kind] { self: Parsers[_, Kind] =>

  /** Grammar symbol.
    *
    * @group symbol
    */
  sealed trait Symbol {

    /** Returns a pretty description of the symbol.
      * The `toString` method is used for token kinds.
      *
      * @param names Names of non-terminals.
      */
    def pretty(names: Int => String): String = this match {
      case NonTerminal(id) => names(id)
      case Terminal(kind) => kind.toString
      case Epsilon => "𝛆"
    }
  }

  /** Non-terminal symbol.
    *
    * @param id Index of the rule in the grammar.
    *
    * @group symbol
    */
  case class NonTerminal(id: Int) extends Symbol

  /** Terminal symbol.
    *
    * @param kind Kind of tokens represented by the terminal.
    *
    * @group symbol
    */
  case class Terminal(kind: Kind) extends Symbol

  /** Empty symbol.
    *
    * @group symbol
    */
  case object Epsilon extends Symbol

  /** Disjunction between various sequences of symbols.
    *
    * @group grammar
    */
  case class Rule(sequences: Seq[Seq[Symbol]]) {

    /** Returns a pretty description of the rule.
      *
      * @param id    Index of this rule.
      * @param names Names of non-terminals.
      */
    def pretty(id: Int, names: Int => String): String = names(id) + " ::= " +
      sequences.map(xs => xs.map(_.pretty(names)).mkString(" ")).mkString(" | ")
  }

  /** Sequence of rules.
    *
    * @group grammar
    */
  case class Grammar(rules: Seq[Rule]) {

    /** Returns a pretty description of the grammar.
      *
      * @param names Names of non-terminals. By default, the index of the rule is displayed.
      */
    def pretty(names: Int => String = _.toString): String =
      rules.zipWithIndex.map {
        case (rule, id) => rule.pretty(id, names)
      }.mkString("\n")
  }

  import Parser._

  /** Computes the grammar associated with a `parser`.
    *
    * @group grammar
    */
  def getGrammar(parser: Parser[Any]): Grammar = {
    var nextId = 0
    var rules = Vector[Rule]()
    val queue = new Queue[Parser[Any]]
    var ids = Map[Parser[Any], Int]()

    def inspect(next: Parser[Any]): Int = {
      if (!ids.contains(next)) {
        val res = nextId
        nextId += 1
        ids += next -> res
        queue.enqueue(next)
        res
      }
      else {
        ids(next)
      }
    }

    inspect(parser)

    def getSymbols(next: Parser[Any]): Seq[Seq[Symbol]] = next match {
      case Disjunction(left, right) => getSymbols(left) ++ getSymbols(right)
      case _ => Seq(getSequents(next))
    }

    def getSequents(next: Parser[Any]): Seq[Symbol] = next match {
      case Failure => Seq()
      case Success(_) => Seq(Epsilon)
      case Elem(kind) => Seq(Terminal(kind))
      case Transform(_, inner) => getSequents(inner)
      case Sequence(left, right) => getSequents(left) ++ getSequents(right)
      case Concat(left, right) => getSequents(left) ++ getSequents(right)
      case d@Disjunction(_, _) => {
        val id = inspect(d)
        Seq(NonTerminal(id))
      }
      case r@Recursive(_) => {
        val id = inspect(r.inner)
        Seq(NonTerminal(id))
      }
    }

    while(queue.nonEmpty) {
      val current = queue.dequeue()
      rules :+= Rule(getSymbols(current))
    }

    Grammar(rules)
  }
}