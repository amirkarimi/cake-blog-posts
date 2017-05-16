## Applying settings to multiple projects using SBT triggered plugins

SBT is the main build tool for Scala projects. It's simply one of the most powerful build tools available. You're not limited to a single method for doing a specific job and there is no predefined and restricted rules. You can automate things as much as you want. And the good news is that SBT tries to run the tasks in parallel as much as possible.

I used to have single-project `build.sbt` files in the past, but now, the standard way is to have multi-project build files which may consist of a set of projects. Keeping all related projects inside a single build file is a good idea as they probably depend on one another and they may share some common settings or dependencies.

There are definitely some settings which should have the same value for all projects; like `organization`, `version`, etc. Repeating those values for each project simply doesn't make sense.

The simplest way for sharing common settings between projects is to define the common settings earlier as a `Seq` and use them later.

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

But our build files are not always that simple. We probably have some plugins enabled for some of the projects. Consequently, there are some plugin-specific settings which can be shared between the projects for which the plugin is enabled.

For example `dockerRepository` of [`DockerPlugin`](http://www.scala-sbt.org/sbt-native-packager/formats/docker.html) can be shared between almost all of the projects for which `DockerPlugin` is enabled. Again, the simplest solution is to create another `Seq` consist of the common settings for the corresponding plugin and use it ONLY in projects that the plugin is enabled for.

```sbt
lazy val commonSettings = Seq(
  organization := "com.example",
  version := "0.1.0-SNAPSHOT"
)

lazy val dockerCommonSettings = Seq(
  dockerRepository in Docker := Some("my-repo")
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

How ever, we should be careful to add those plugin-specific settings to the project for which the plugin is enabled which we might forget to do for new projects.

As I said, there is no limitation about what you want to automate when it comes to SBT. So why bother taking care of those settings manually? Isn't it possible to ask SBT to do that for us?

Of course, it's possible. You can ask SBT to automatically enable a plugin only for projects for which another specific plugin is enabled. We can do that using SBT [auto plugins](http://www.scala-sbt.org/0.13/docs/Plugins.html#Using+an+auto+plugin). First of all, lets see how we can create a home-made auto plugin.

Creating a SBT auto plugin is super easy. Just create a Scala file in `project` directory and extend `AutoPlugin`.

```scala
import sbt._
import sbt.Keys._

object DockerProjectSpecificPlugin extends AutoPlugin {
  import com.typesafe.sbt.packager.Keys._
  import com.typesafe.sbt.packager.docker._
  import com.typesafe.sbt.SbtNativePackager._

  override def requires = DockerPlugin
  override def projectSettings = Seq(
    dockerRepository in Docker := Some("my-repo")
  )
}
```

The above plugin specify a value for `dockerRepository` setting of `DockerPlugin`. By overriding `requires` method we specify that this plugin requires `DockerPlugin` to be enabled for the target project. But it still needs to be explicitly enabled to take effect.

```sbt
//...
lazy val fooService = (project in file("fooService"))
  .enablePlugins(DockerPlugin, DockerProjectSpecificPlugin)
  .settings(
    commonSettings,
    // other settings
  )
//...
```

Again we have to take care of enabling the project-specific plugin manually! Here is where [triggered plugins](http://www.scala-sbt.org/0.13/docs/Plugins.html#Root+plugins+and+triggered+plugins) come handy. Auto plugins provide a way to automatically attach themselves to projects if their dependencies are met. We can simply convert our plugin to a *triggered* one by overriding `trigger` method to return `allRequirements`.

```scala
import sbt._
import sbt.Keys._

object DockerProjectSpecificPlugin extends AutoPlugin {
  import com.typesafe.sbt.packager.Keys._
  import com.typesafe.sbt.packager.docker._
  import com.typesafe.sbt.SbtNativePackager._

  override def trigger  = allRequirements
  override def requires = DockerPlugin
  override def projectSettings = Seq(
    dockerRepository in Docker := Some("my-repo")
  )
}
```

Now we no longer need to enable this plugin explicitly. As we specified its dependencies using `requires` method, it will automatically be attached to the projects for which `DockerPlugin` is enabled.

```sbt
//...
lazy val fooService = (project in file("fooService"))
  .enablePlugins(DockerPlugin)
  .settings(
    commonSettings,
    // other settings
  )
//...
```

We can even go further and move all common settings into another triggered plugin.

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

And finally, `build.sbt` would be much cleaner and concise.

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

Thanks to triggered plugins you'll end up having a cleaner `build.sbt` and you no longer have to worry about assigning the common plugin-specific settings to the projects manually.

You can also find the sample project of this blog post [here](https://github.com/amirkarimi/cake-blog-posts/tree/master/01-apply-settings-using-sbt-auto-plugin/sample).
