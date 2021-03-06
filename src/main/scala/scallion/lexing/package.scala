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

/** This package is used to write lexers.
  *
  * To use the package, mix-in the [[scallion.lexing.Lexers]] trait.
  *
  * {{{
  * object MyLexers extends Lexers[Character, Position] {
  *
  *   // Then define your parsers using combinators.
  *   val myLexer = Lexer(...)
  * }
  * }}}
  *
  * Additional traits can be mixed-in.
  * See for instance [[scallion.lexing.CharRegExps]]
  * for regular expressions on `Char`.
  */
package object lexing