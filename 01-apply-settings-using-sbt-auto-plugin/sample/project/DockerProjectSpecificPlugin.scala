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
