@DESCRIPTION@

## Installation

Add the following line to your build.sbt file:

```sbt
libraryDependencies += "@ORGANIZATION@" %% "@NAME@" % "@VERSION@" % Test
```

## Usage

Create a new test class and extend `munit.CirceSuite`:

```scala mdoc:silent
import io.circe._
import io.circe.syntax._

class MySuite extends munit.CirceSuite {

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
```

### Asserting JSON values with placeholders

On a `CirceSuite`, `assertEquals` on two JSON values accepts placeholders in the expected one for values that are not known in advance: an id the server generated, a timestamp of the request. Those are written as placeholders, strings that stand for any value they accept:

```scala mdoc:compile-only
import java.time.Instant
import java.util.UUID

import io.circe.Json
import io.circe.syntax._

class UsersSuite extends munit.CirceSuite {

  test("a user is created") {
    val obtained = Json.obj("id" := UUID.randomUUID(), "name" := "Jose", "created_at" := Instant.now())

    val expected = Json.obj("id" := "<uuid>", "name" := "Jose", "created_at" := "<recent timestamp>")

    assertEquals(obtained, expected)
  }

}
```

On failure the diff shows the obtained value with every value that satisfied its placeholder replaced by the marker, so only the values that did not match are shown as they were received. A field missing from the expected value, or present in it but missing from the obtained one, fails like any other difference.

These placeholders come by default:

| Placeholder | Accepts |
| :--- | :--- |
| `<any>` | any value |
| `<string>`, `<number>`, `<boolean>`, `<object>`, `<array>` | any value of that type |
| `<uuid>` | any UUID |
| `<uri>` | any URI |
| `<timestamp>` | any ISO-8601 instant |
| `<recent timestamp>` | any instant within the last ten seconds |

Add your own by putting a `Placeholders` value in scope:

```scala mdoc:compile-only
import munit.Placeholders

implicit val placeholders: Placeholders =
  Placeholders.default + ("<positive>", _.as[Int].exists(_ > 0))
```

A placeholder can also be used with `:=` when building the expected value, through its `json` value: `"id" := Placeholder.uuid.json`.

Placeholders and the syntax below are available on any `CirceSuite`. To use them on another suite, mix in `munit.CirceMUnitSyntax`.

### Reading JSON values by path

When a single value is what matters, `apply` on a `Json` reads it by path and decodes it, failing the test when the path is missing or the value does not decode. `get` returns `None` instead when the path is missing:

```scala mdoc:compile-only
import io.circe.Json
import io.circe.syntax._

class UsersSuite extends munit.CirceSuite {

  test("a user is read") {
    val user = Json.obj("name" := "Jose", "workspace" := Json.obj("id" := "42"))

    assertEquals(user[String]("name"), "Jose")
    assertEquals(user[String]("workspace", "id"), "42")
    assertEquals(user.get[String]("deleted_at"), None)
  }

}
```

## Contributors to this project 

@CONTRIBUTORS_TABLE@
