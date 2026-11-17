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

import java.time.Instant
import java.util.UUID

import io.circe.Json
import io.circe.literal._
import io.circe.syntax._

class CirceMUnitSyntaxSuite extends FunSuite with CirceMUnitSyntax {

  val obtained: Json = Json.obj(
    "id"         := UUID.randomUUID(),
    "name"       := "Jose",
    "created_at" := Instant.now(),
    "tags"       := List("a", "b"),
    "nested"     := Json.obj("id" := UUID.randomUUID())
  )

  test("a value is read by its path") {
    assertEquals(obtained[String]("name"), "Jose")
    assertEquals(obtained[List[String]]("tags"), List("a", "b"))
    assertEquals(obtained[UUID]("nested", "id"), obtained[UUID]("nested", "id"))
  }

  test("a missing path fails the test") {
    val failure = intercept[FailException](obtained[String]("nested", "name"))

    assert(failure.getMessage.contains("Path nested.name not found"))
  }

  test("a value that does not decode fails the test") {
    val failure = intercept[FailException](obtained[Int]("name"))

    assert(failure.getMessage.contains("Path name does not decode"))
  }

  test("an optional value is read by its path") {
    assertEquals(obtained.get[String]("name"), Some("Jose"))
    assertEquals(obtained.get[String]("nested", "name"), None)
  }

  test("<any> stands for any value") {
    val expected =
      json"""{ "id": "<any>", "name": "<any>", "created_at": "<any>", "tags": "<any>", "nested": "<any>" }"""

    assertEquals(obtained, expected)
  }

  test("placeholders apply at any depth") {
    val expected = json"""{
      "id": "<any>",
      "name": "Jose",
      "created_at": "<any>",
      "tags": ["a", "<any>"],
      "nested": { "id": "<any>" }
    }"""

    assertEquals(obtained, expected)
  }

  test("placeholders can be written with :=") {
    val expected = Json.obj(
      "id"         := Placeholder.any.json,
      "name"       := "Jose",
      "created_at" := Placeholder.any.json,
      "tags"       := List("a", "b"),
      "nested"     := Placeholder.any.json
    )

    assertEquals(obtained, expected)
  }

  test("placeholders can be extended") {
    implicit val placeholders: Placeholders =
      Placeholders.default + ("<positive>", _.as[Int].exists(_ > 0))

    val expected = json"""{ "name": "<any>", "count": "<positive>" }"""

    val obtained = json"""{ "name": "Jose", "count": 3 }"""

    assertEquals(obtained, expected)
  }

  test("an extended placeholder rejects what it does not accept") {
    implicit val placeholders: Placeholders =
      Placeholders.default + ("<positive>", _.as[Int].exists(_ > 0))

    val expected = json"""{ "count": "<positive>" }"""

    val obtained = json"""{ "count": -1 }"""

    intercept[ComparisonFailException](assertEquals(obtained, expected))
  }

  test("a value that does not satisfy its placeholder is reported as received") {
    implicit val placeholders: Placeholders =
      Placeholders.default + ("<positive>", _.as[Int].exists(_ > 0))

    val expected = json"""{ "name": "<positive>", "count": "<positive>" }"""

    val obtained = json"""{ "name": "Jose", "count": 3 }"""

    val failure = intercept[ComparisonFailException](assertEquals(obtained, expected))

    assert(failure.getMessage.contains("""name -> "Jose""""))
    assert(failure.getMessage.contains("""count -> "<positive>""""))
  }

  test("an unexpected field fails") {
    val expected = json"""{ "id": "<any>", "name": "Jose" }"""

    val obtained = json"""{ "id": 1, "name": "Jose", "extra": 1 }"""

    intercept[ComparisonFailException](assertEquals(obtained, expected))
  }

  test("a missing field fails") {
    val expected = json"""{ "id": "<any>", "name": "Jose" }"""

    val obtained = json"""{ "name": "Jose" }"""

    intercept[ComparisonFailException](assertEquals(obtained, expected))
  }

}
