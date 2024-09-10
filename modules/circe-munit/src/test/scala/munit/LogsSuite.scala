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

import scala.collection.mutable.ListBuffer
import scala.reflect.ClassTag

import sbt.testing.EventHandler
import sbt.testing.TaskDef

import io.circe.Codec
import io.circe.Decoder
import io.circe.Encoder
import io.circe.Json
import io.circe.syntax._

/** This suite ensures that the logs outputed by this library are correct.
  *
  * For this we create some dummy suites in the suite's companion object (so they are not launched), execute them in a
  * controlled runner, get its output, and compare it against an expected one.
  */
class LogsSuite extends FunSuite {

  private val thisFile = s"${sys.props("user.dir")}/src/test/scala/munit/LogsSuite.scala"

  test("Suite works as expected in Scala 2.13") {
    assume(!isScala3)

    val obtained = execute[LogsSuite.SimpleSuite]

    val expected =
      s"""==> Success Codec[List[Int]]
         |==> Failure Encoder[Map[String, Int]] (This test should fail) at $thisFile
         |      Map("foo" -> 42, "bar" -> 43)
         |    } {
         |      Json.obj("bar" := 43)
         |Clues {
         |  json.spaces String = '''{
         |  "foo" : 42,
         |  "bar" : 43
         |}'''
         |}
         |=> Obtained
         |JObject(
         |  value = object[foo -> 42,bar -> 43]
         |)
         |=> Diff (- obtained, + expected)
         | JObject(
         |-  value = object[foo -> 42,bar -> 43]
         |+  value = object[bar -> 43]
         | )
         |==> Success Decoder[Map[String, Int]] (Something)
         |==> Success Codec[LogsSuite::SimpleSuite.Foo]
         |==> Success Codec[LogsSuite::SimpleSuite.Bar[LogsSuite::SimpleSuite.Foo, Map[String, Int]]]""".stripMargin

    assertNoDiff(obtained, expected)
  }

  test("Suite works as expected in Scala 3") {
    assume(isScala3)

    val obtained = execute[LogsSuite.SimpleSuite]

    val expected =
      s"""==> Success Codec[List[Int]]
         |==> Failure Encoder[Map[String, Int]] (This test should fail) at $thisFile
         |      Json.obj("bar" := 43)
         |    }
         |Clues {
         |  json.spaces String = '''{
         |  "foo" : 42,
         |  "bar" : 43
         |}'''
         |}
         |=> Obtained
         |JObject(
         |  value = object[foo -> 42,bar -> 43]
         |)
         |=> Diff (- obtained, + expected)
         | JObject(
         |-  value = object[foo -> 42,bar -> 43]
         |+  value = object[bar -> 43]
         | )
         |==> Success Decoder[Map[String, Int]] (Something)
         |==> Success Codec[LogsSuite::SimpleSuite.Foo]
         |==> Success Codec[LogsSuite::SimpleSuite.Bar[LogsSuite::SimpleSuite.Foo, Map[String, Int]]]""".stripMargin

    assertNoDiff(obtained, expected)
  }

  private def isScala3: Boolean = compileErrors("scala.quoted.Expr").isEmpty()

  private def execute[T](implicit classTag: ClassTag[T]): String = {
    val framework = new Framework

    val runner = framework.runner(Array("+l"), Array(), this.getClass().getClassLoader())

    val taskDef = new TaskDef(classTag.runtimeClass.getName(), framework.munitFingerprint, false, Array())

    val tasks = runner.tasks(Array(taskDef))

    val buffer = ListBuffer.empty[String]

    val eventHandler: EventHandler = event => {
      buffer += "==> "
      buffer += event.status().name()
      buffer += " "
      buffer += event.fullyQualifiedName()
      buffer += (if (event.throwable().isDefined()) s" at ${event.throwable().get().getMessage()}" else "")
      buffer += "\n"
    }

    tasks.foreach(_.execute(eventHandler, Array()))

    buffer.mkString
      .replace("\"\"\"", "'''")
      .replaceAll("\\.scala:\\d+", ".scala")
      .replaceAll("""\d+:\n""", "")
      .replaceAll("""\d+:(.*)\n""", "$1\n")
      .replace(classTag.runtimeClass.getName() + ".", "")
  }

}

object LogsSuite {

  class SimpleSuite extends CirceSuite {

    checkCodec(List(1, 2, 3)) {
      Json.arr(1.asJson, 2.asJson, 3.asJson)
    }

    checkEncoder("This test should fail") {
      Map("foo" -> 42, "bar" -> 43)
    } {
      Json.obj("bar" := 43)
    }

    checkDecoder("Something")(Map("foo" -> 42, "bar" -> 43)) {
      Json.obj("foo" := 42, "bar" := 43)
    }

    case class Foo(i: Int, s: String)

    implicit val fooCodec: Codec[Foo] = Codec.forProduct2("i", "s")(Foo.apply)(foo => (foo.i, foo.s))

    checkCodec(Foo(42, "foo")) {
      Json.obj("i" := 42, "s" := "foo")
    }

    case class Bar[A, B](a: A, b: B)

    implicit def barCodec[A: Encoder: Decoder, B: Encoder: Decoder]: Codec[Bar[A, B]] =
      Codec.forProduct2[Bar[A, B], A, B]("a", "b")(Bar(_, _))(bar => (bar.a, bar.b))

    checkCodec(Bar(Foo(42, "foo"), Map("bar" -> 43))) {
      Json.obj(
        "a" := Json.obj("i" := 42, "s" := "foo"),
        "b" := Json.obj("bar" := 43)
      )
    }

  }

}
