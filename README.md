MUnit assertions for Circe codecs

## Installation

Add the following line to your build.sbt file:

```sbt
libraryDependencies += "com.alejandrohdezma" %% "circe-munit" % "0.0.0" % Test
```

## Usage

Create a new test class and extend `munit.CirceSuite`:

```scala
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

## Contributors to this project 

| <a href="https://github.com/alejandrohdezma"><img alt="alejandrohdezma" src="https://avatars.githubusercontent.com/u/9027541?v=4&s=120" width="120px" /></a> |
| :--: |
| <a href="https://github.com/alejandrohdezma"><sub><b>alejandrohdezma</b></sub></a> |
