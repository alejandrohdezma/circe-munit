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

}
