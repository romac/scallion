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

package example.calculator

import scallion.input._
import scallion.lexing._
import scallion.parsing._

sealed trait Token
case class NumberToken(value: Int) extends Token
case class OperatorToken(operator: Char) extends Token
case class ParenthesisToken(isOpen: Boolean) extends Token
case object SpaceToken extends Token
case class UnknownToken(content: String) extends Token

object CalcLexer extends Lexers[Token, Char, Unit] with CharRegExps {

  val lexer = Lexer(
    // Operators
    oneOf("-+/*!")
      |> { cs => OperatorToken(cs.head) },

    // Parentheses
    elem('(') |> ParenthesisToken(true),
    elem(')') |> ParenthesisToken(false),

    // Spaces
    many1(whiteSpace) |> SpaceToken,

    // Numbers
    {
      elem('0') |
      nonZero ~ many(digit)
    }
      |> { cs => NumberToken(cs.mkString.toInt) }
  ) onError {
    (cs, _) => UnknownToken(cs.mkString)
  }


  def apply(it: Iterator[Char]): Iterator[Token] = {
    val source = Source.fromIterator(it, NoPositioner)

    val tokens = lexer(source)

    tokens.filter((token: Token) => token != SpaceToken)
  }
}

sealed abstract class TokenClass(text: String) {
  override def toString = text
}
case object NumberClass extends TokenClass("<number>")
case class OperatorClass(op: Char) extends TokenClass(op.toString)
case class ParenthesisClass(isOpen: Boolean) extends TokenClass(if (isOpen) "(" else ")")
case object OtherClass extends TokenClass("?")

object CalcParser extends Parsers[Token, TokenClass] with Operators {

  override def getKind(token: Token): TokenClass = token match {
    case NumberToken(_) => NumberClass
    case OperatorToken(c) => OperatorClass(c)
    case ParenthesisToken(o) => ParenthesisClass(o)
    case _ => OtherClass
  }

  val number = accept(NumberClass) {
    case NumberToken(n) => n
  }

  val plus = accept(OperatorClass('+')) {
    case _ => (x: Int, y: Int) => x + y
  }

  val minus = accept(OperatorClass('-')) {
    case _ => (x: Int, y: Int) => x - y
  }

  val times = accept(OperatorClass('*')) {
    case _ => (x: Int, y: Int) => x * y
  }

  val div = accept(OperatorClass('/')) {
    case _ => (x: Int, y: Int) => x / y
  }

  val fac = accept(OperatorClass('!')) {
    case _ => (x: Int) => 1.to(x).product
  }

  val uMinus = accept(OperatorClass('-')) {
    case _ => (x: Int) => -x
  }

  val uPlus = accept(OperatorClass('+')) {
    case _ => (x: Int) => x
  }

  val open = elem(ParenthesisClass(true))
  val close = elem(ParenthesisClass(false))

  lazy val basic: Parser[Int] = number | open ~>~ value ~<~ close

  lazy val value: Parser[Int] = recursive {
    operators(prefixes(uPlus | uMinus, postfixes(basic, fac)))(
      times | div is LeftAssociative,
      plus | minus is LeftAssociative)
  }

  def apply(it: Iterator[Token]): Option[Int] = value(it) match {
    case Parsed(value, _) => Some(value)
    case _ => None
  }
}