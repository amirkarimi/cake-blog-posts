## Apply settings to multiple projects using SBT auto plugins

You have a multi-project build.sbt and want to apply some settings for all of them in a DRY way.

You can define the common settings earlier and use them later:

```sbt
lazy val commonSettings = Seq(
  organization := "com.example",
  version := "0.1.0-SNAPSHOT"
)

lazy val core = (project in file("core"))
  .settings(
    commonSettings,
    // other settings
  )

lazy val fooService = (project in file("fooService"))
  .settings(
    commonSettings,
    // other settings
  )

lazy val util = (project in file("util"))
  .settings(
    commonSettings,
    // other settings
  )
```

What if you want some settings to be applied if some plugins where enabled on the project? Of course you can create another `Seq` of settings and use wherever appropriate.

```sbt
lazy val commonSettings = Seq(
  organization := "com.example",
  version := "0.1.0-SNAPSHOT"
)

lazy val dockerCommonSettings = Seq(
  daemonUser in Docker := "test"
)

lazy val core = (project in file("core"))
  .settings(
    commonSettings,
    // other settings
  )

lazy val fooService = (project in file("fooService"))
  .enablePlugins(DockerPlugin)
  .settings(
    commonSettings,
    dockerCommonSettings,
    // other settings
  )

lazy val util = (project in file("util"))
  .settings(
    commonSettings,
    // other settings
  )
```

But you have to take care of that and add the plugin-specific common settings on the project for which the plugin is enabled.

You can have SBT to do that for you. You can ask SBT to enable an auto plugin for projects based on . This is possible using SBT **auto plugins**.

Create an Scala file (with `.scala` extension) in your `project` directory which extends `AutoPlugin`. That's how you create SBT auto plugins:

```scala
import sbt._
import sbt.Keys._

object DockerProjectSpecificPlugin extends AutoPlugin {
  import com.typesafe.sbt.packager.Keys._
  import com.typesafe.sbt.packager.docker._
  import com.typesafe.sbt.SbtNativePackager._

  override def requires = DockerPlugin
  override def trigger  = allRequirements
  override def projectSettings = Seq(
    daemonUser in Docker := "test"
  )
}
```

You can even use this method to specify the common settings applied to all projects. Simply just don't specify any required plugin:


```scala
import sbt._
import sbt.Keys._

object CommonProjectSettingsPlugin extends AutoPlugin {

  override def trigger  = allRequirements
  override def projectSettings = Seq(
    organization := "com.example",
    version := "0.1.0-SNAPSHOT"
  )
}  
```

Finally, `build.sbt` would look like this:

```sbt
lazy val core = (project in file("core"))
  .settings(
    // other settings
  )

lazy val fooService = (project in file("fooService"))
  .enablePlugins(DockerPlugin)
  .settings(
    // other settings
  )

lazy val util = (project in file("util"))
  .settings(
    // other settings
  )
```

You can find the sample project of this blog post [here](#).