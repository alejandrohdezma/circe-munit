/*
 * Copyright 2024 Alejandro Hernández <https://github.com/alejandrohdezma>
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

package munit

import cats.syntax.eq._

import io.circe.Json

/** A value that stands for any JSON value accepted by `matches` inside an expected JSON document.
  *
  * Placeholders are plain strings, so they can be written in `json"..."` literals as well as with `:=`:
  *
  * {{{
  * json"""{ "id": "<any>", "name": "Jose" }"""
  * }}}
  *
  * @param marker
  *   the string that stands for the placeholder in an expected document
  * @param matches
  *   whether an obtained value is accepted
  */
final case class Placeholder(marker: String, matches: Json => Boolean) {

  /** The placeholder as a JSON value, for use with `:=` when building an expected document. */
  val json: Json = Json.fromString(marker)

}

object Placeholder {

  /** Any value. */
  val any: Placeholder = Placeholder("<any>", _ => true)

}

/** The placeholders `assertEquals` recognises when comparing two `Json` values. The default set is in scope on every
  * suite; add to it with `+`:
  *
  * {{{
  * implicit val placeholders: Placeholders = Placeholders.default + ("<positive>", _.as[Int].exists(_ > 0))
  * }}}
  */
final case class Placeholders(all: List[Placeholder]) {

  def +(placeholder: Placeholder): Placeholders = Placeholders(all :+ placeholder)

  /** Adds a placeholder from its marker and predicate: `Placeholders.default + ("<positive>", _.as[Int].exists(_ >
    * 0))`.
    */
  def +(marker: String, matches: Json => Boolean): Placeholders = this + Placeholder(marker, matches)

  /** The placeholder whose marker is `json`, if any. */
  def find(json: Json): Option[Placeholder] = json.asString.flatMap(marker => all.find(_.marker === marker))

}

object Placeholders {

  implicit val default: Placeholders = Placeholders(
    List(Placeholder.any)
  )

}
