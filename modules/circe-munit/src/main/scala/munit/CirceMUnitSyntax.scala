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

import io.circe.ACursor
import io.circe.Decoder
import io.circe.Json

/** Syntax for working with `Json` values in tests. Mixed into `CirceSuite`; mix it into any other suite to use it
  * there.
  */
trait CirceMUnitSyntax { self: Assertions =>

  implicit final class CirceMUnitJsonOps(json: Json) {

    /** The value at `path` decoded as `A`, failing the test if the path is missing or does not decode.
      *
      * {{{
      * body[String]("workspaces", "root", "id")
      * }}}
      */
    def apply[A: Decoder](path: String*)(implicit loc: Location): A = {
      val cursor = path.foldLeft(json.hcursor: ACursor)(_ downField _)

      if (!cursor.succeeded) fail(s"Path ${path.mkString(".")} not found in $json")
      else cursor.as[A].fold(e => fail(s"Path ${path.mkString(".")} does not decode: ${e.getMessage}"), identity)
    }

    /** The value at `path` decoded as `A`, or `None` if the path is missing. Fails the test if the value does not
      * decode.
      */
    def get[A: Decoder](path: String*)(implicit loc: Location): Option[A] = {
      val cursor = path.foldLeft(json.hcursor: ACursor)(_ downField _)

      if (!cursor.succeeded) None
      else cursor.as[A].fold(e => fail(s"Path ${path.mkString(".")} does not decode: ${e.getMessage}"), Some(_))
    }

  }

}
