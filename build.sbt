ThisBuild / scalaVersion           := "2.13.15"
ThisBuild / crossScalaVersions     := Seq("2.13.15", "3.3.3")
ThisBuild / organization           := "com.alejandrohdezma"
ThisBuild / versionPolicyIntention := Compatibility.BinaryAndSourceCompatible

addCommandAlias("ci-test", "fix --check; versionPolicyCheck; mdoc; +publishLocal; +test")
addCommandAlias("ci-docs", "github; mdoc; headerCreateAll")
addCommandAlias("ci-publish", "versionCheck; github; ci-release")

lazy val documentation = project
  .enablePlugins(MdocPlugin)
  .dependsOn(`circe-munit`)

lazy val `circe-munit` = module
  .settings(Test / fork := true)
  .settings(libraryDependencies += "org.scalameta" %% "munit" % "1.0.1")
  .settings(libraryDependencies += "io.circe" %% "circe-core" % "0.14.10")
  .settings(libraryDependencies += "dev.zio" %% "izumi-reflect" % "2.3.10")
