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
