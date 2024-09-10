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

import io.circe.Decoder
import io.circe.Encoder
import io.circe.Json
import io.circe.syntax._
import izumi.reflect.Tag
import izumi.reflect.macrortti.LightTypeTag

trait CirceSuite extends FunSuite {

  /** Asserts that the given value can be encoded to the expected JSON and then decoded back to the original value.
    *
    * This method creates a test with the name of the type of the value and its type arguments, if any, so you should
    * use it like this:
    *
    * {{{
    * checkCodec(List(1, 2, 3))(json"""[1, 2, 3]""")
    * }}}
    */
  final def checkCodec[A: Encoder: Decoder: Tag](a: A)(expected: Json)(implicit loc: Location) =
    test(s"Codec[${prettify(Tag[A].tag)}]") {
      val json = a.asJson
      assertEquals(json, expected, clues(json.spaces2))
      assertEquals(json.as[A], Right(a))
    }

  /** Asserts that the given value can be encoded to the expected JSON and then decoded back to the original value.
    *
    * This method creates a test with the name of the type of the value and its type arguments, if any, so you should
    * use it like this:
    *
    * {{{
    * checkCodec("Some description")(List(1, 2, 3))(json"""[1, 2, 3]""")
    * }}}
    *
    * The provided description will be appended to the test name.
    */
  final def checkCodec[A: Encoder: Decoder: Tag](
      description: String
  )(a: A)(expected: Json)(implicit loc: Location) =
    test(s"Codec[${prettify(Tag[A].tag)}] ($description)") {
      val json = a.asJson
      assertEquals(json, expected, clues(json.spaces2))
      assertEquals(json.as[A], Right(a))
    }

  /** Asserts that the given value can be encoded to the expected JSON.
    *
    * This method creates a test with the name of the type of the value and its type arguments, if any, so you should
    * use it like this:
    *
    * {{{
    * checkEncoder(List(1, 2, 3))(json"""[1, 2, 3]""")
    * }}}
    */
  final def checkEncoder[A: Encoder: Tag](a: A)(expected: Json)(implicit loc: Location) =
    test(s"Encoder[${prettify(Tag[A].tag)}]") {
      val json = a.asJson
      assertEquals(json, expected, clues(json.spaces2))
    }

  /** Asserts that the given value can be encoded to the expected JSON.
    *
    * This method creates a test with the name of the type of the value and its type arguments, if any, so you should
    * use it like this:
    *
    * {{{
    * checkEncoder("Some description")(List(1, 2, 3))(json"""[1, 2, 3]""")
    * }}}
    *
    * The provided description will be appended to the test name.
    */
  final def checkEncoder[A: Encoder: Tag](description: String)(a: A)(expected: Json)(implicit loc: Location) =
    test(s"Encoder[${prettify(Tag[A].tag)}] ($description)") {
      val json = a.asJson
      assertEquals(json, expected, clues(json.spaces2))
    }

  /** Asserts that the given JSON can be decoded to the expected value.
    *
    * This method creates a test with the name of the type of the value and its type arguments, if any, so you should
    * use it like this:
    *
    * {{{
    * checkDecoder(List(1, 2, 3))(json"""[1, 2, 3]""")
    * }}}
    */
  final def checkDecoder[A: Decoder: Tag](expected: A)(json: Json)(implicit loc: Location) =
    test(s"Decoder[${prettify(Tag[A].tag)}]") {
      assertEquals(json.as[A], Right(expected))
    }

  /** Asserts that the given JSON can be decoded to the expected value.
    *
    * This method creates a test with the name of the type of the value and its type arguments, if any, so you should
    * use it like this:
    *
    * {{{
    * checkDecoder("Some description")(List(1, 2, 3))(json"""[1, 2, 3]""")
    * }}}
    *
    * The provided description will be appended to the test name.
    */
  final def checkDecoder[A: Decoder: Tag](description: String)(expected: A)(json: Json)(implicit loc: Location) =
    test(s"Decoder[${prettify(Tag[A].tag)}] ($description)") {
      assertEquals(json.as[A], Right(expected))
    }

  /** Returns a pretty representation of a type.
    *
    * This method will return the name of the type and its type arguments, if any, in a pretty format.
    *
    * For example, given the type `com.alejandrohdezma.Foo.Bar[Int, String]` this method will return `Foo.Bar[Int,
    * String]`.
    */
  private def prettify(tag: LightTypeTag): String = {
    val name = tag.ref.getPrefix
      .map(prefix => s"$prefix.${tag.shortName}")
      .getOrElse(tag.shortName)

    val args =
      if (tag.typeArgs.isEmpty) ""
      else tag.typeArgs.map(prettify).mkString("[", ", ", "]")

    s"$name$args"
  }

}
