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

import io.circe._
import io.circe.syntax._

class ExampleSuite extends CirceSuite {

  // We can asserts codecs, encoders and decoders

  checkCodec(List(1, 2, 3)) {
    Json.arr(1.asJson, 2.asJson, 3.asJson)
  }

  checkEncoder(List(1, 2, 3)) {
    Json.arr(1.asJson, 2.asJson, 3.asJson)
  }

  checkDecoder(List(1, 2, 3)) {
    Json.arr(1.asJson, 2.asJson, 3.asJson)
  }

  // We can provide an extra description for the test
  // Test name will be "Codec[Map[String, Int]] (some description)"

  checkCodec("some description")(Map("foo" -> 42, "bar" -> 43)) {
    Json.obj("foo" := 42, "bar" := 43)
  }

  // We can use any type with a codec, encoder or decoder

  case class Foo(i: Int, s: String)

  implicit val FooCodec: Codec[Foo] = Codec.forProduct2("i", "s")(Foo.apply)(foo => (foo.i, foo.s))

  checkCodec(Foo(42, "foo")) {
    Json.obj("i" := 42, "s" := "foo")
  }

  // If the type has type parameters, they will also appear on the test name

  case class Bar[A, B](a: A, b: B)

  implicit def BarCodec[A: Encoder: Decoder, B: Encoder: Decoder]: Codec[Bar[A, B]] =
    Codec.forProduct2[Bar[A, B], A, B]("a", "b")(Bar(_, _))(bar => (bar.a, bar.b))

  // Test name for 👇🏼 will be: `Codec[Bar[Foo, Map[String, Int]]]`
  checkCodec(Bar(Foo(42, "foo"), Map("bar" -> 43))) {
    Json.obj(
      "a" := Json.obj("i" := 42, "s" := "foo"),
      "b" := Json.obj("bar" := 43)
    )
  }

}
