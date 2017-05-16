lazy val core = (project in file("core"))
  .settings(
    name := "core"
  )

lazy val fooService = (project in file("fooService"))
  .enablePlugins(DockerPlugin)
  .settings(
    name := "fooService"
  )

lazy val util = (project in file("util"))
  .settings(
    name := "util"
  )
