ThisBuild / organization           := "com.alejandrohdezma"
ThisBuild / versionPolicyIntention := Compatibility.BinaryCompatible

addCommandAlias("ci-test", "fix --check; versionPolicyCheck; mdoc; +publishLocal; +test")
addCommandAlias("ci-docs", "github; mdoc; headerCreateAll")
addCommandAlias("ci-publish", "versionCheck; github; ci-release")

lazy val documentation = project
  .enablePlugins(MdocPlugin)
  .dependsOn(`circe-munit`)

lazy val `circe-munit` = module
  .settings(Test / fork := true)
