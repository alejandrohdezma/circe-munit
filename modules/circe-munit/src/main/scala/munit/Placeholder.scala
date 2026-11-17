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

import java.net.URI
import java.time.Instant
import java.util.UUID

import scala.concurrent.duration._

import cats.syntax.all._

import io.circe.Encoder
import io.circe.Json

/** A value that stands for any JSON value accepted by `matches` inside an expected JSON document.
  *
  * Placeholders are plain strings, so they can be written in `json"..."` literals as well as with `:=`:
  *
  * {{{
  * json"""{ "id": "<uuid>", "created_at": "<recent timestamp>" }"""
  * }}}
  *
  * @param marker
  *   the string that stands for the placeholder in an expected document
  * @param matches
  *   whether an obtained value is accepted
  */
final case class Placeholder(marker: String, matches: Json => Boolean)

object Placeholder {

  /** Encodes a placeholder as its marker, so it can be used with `:=` when building an expected document. */
  implicit val PlaceholderEncoder: Encoder[Placeholder] = Encoder.encodeString.contramap(_.marker)

  /** Any value. */
  val any: Placeholder = Placeholder("<any>", _ => true)

  /** Any string. */
  val string: Placeholder = Placeholder("<string>", _.isString)

  /** Any number. */
  val number: Placeholder = Placeholder("<number>", _.isNumber)

  /** Any boolean. */
  val boolean: Placeholder = Placeholder("<boolean>", _.isBoolean)

  /** Any object. */
  val `object`: Placeholder = Placeholder("<object>", _.isObject)

  /** Any array. */
  val array: Placeholder = Placeholder("<array>", _.isArray)

  /** Any UUID. */
  val uuid: Placeholder = Placeholder("<uuid>", _.as[UUID].isRight)

  /** Any URI. */
  val uri: Placeholder = Placeholder("<uri>", _.as[String].exists(s => Either.catchNonFatal(new URI(s)).isRight))

  /** Any instant. */
  val timestamp: Placeholder = Placeholder("<timestamp>", _.as[Instant].isRight)

  /** Any instant within the last `within`. */
  def recentTimestamp(within: FiniteDuration = 10.seconds): Placeholder =
    Placeholder("<recent timestamp>", _.as[Instant].exists(_.isAfter(Instant.now().minusMillis(within.toMillis))))

}

/** The placeholders `assertEquals` recognises when comparing two `Json` values. The default set is in scope on every
  * suite; add to it with `+`, or build a set from scratch:
  *
  * {{{
  * implicit val placeholders: Placeholders = Placeholders.default + ("<positive>", _.as[Int].exists(_ > 0))
  *
  * implicit val placeholders: Placeholders = Placeholders(Placeholder.any, Placeholder.uuid)
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

  def apply(placeholders: Placeholder*): Placeholders = Placeholders(placeholders.toList)

  implicit val default: Placeholders = Placeholders(
    Placeholder.any, Placeholder.string, Placeholder.number, Placeholder.boolean, Placeholder.`object`,
    Placeholder.array, Placeholder.uuid, Placeholder.uri, Placeholder.timestamp, Placeholder.recentTimestamp()
  )

}
