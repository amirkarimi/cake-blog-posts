import sbt._
import sbt.Keys._

object CommonProjectSettingsPlugin extends AutoPlugin {

  override def trigger  = allRequirements
  override def projectSettings = Seq(
    organization := "com.example",
    version := "0.1.0-SNAPSHOT"
  )
}
