import sbt.TaskKey
import scalajsbundler.sbtplugin.WebScalaJSBundlerPlugin.autoImport.NpmAssets

name := """ct-delay"""
version := "1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.5"

lazy val server = (project in file("server"))
  .settings(commonSettings)
  .settings(
    scalaJSProjects := Seq(client),
    pipelineStages in Assets := Seq(scalaJSPipeline),
    pipelineStages := Seq(digest, gzip),
    // triggers scalaJSPipeline when using compile or continuous compilation
    compile in Compile := ((compile in Compile) dependsOn scalaJSPipeline).value,
    libraryDependencies ++= Seq(
      "com.vmunier" %% "scalajs-scripts" % "1.1.4",
      guice,
      ws,
      caffeine,
      "net.lingala.zip4j" % "zip4j" % "2.7.0",
      "com.github.tototoshi" %% "scala-csv" % "1.3.7",
      "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test,
      "org.scalatestplus" %% "mockito-3-4" % "3.2.8.0" % Test
    ),
    npmAssets ++= NpmAssets.ofProject(client) { nodeModules =>
      (nodeModules / "leaflet") ** ("*.css" || "*.png")
    }.value
  )
  .enablePlugins(PlayScala, WebScalaJSBundlerPlugin)
  .dependsOn(sharedJvm)

val sourceMapCleanup = TaskKey[Unit]("sourceMapCleanUp", "Fix up source map to exclude relative library paths")

lazy val client = (project in file("client"))
  .settings(commonSettings)
  .settings(
    scalaJSUseMainModuleInitializer := true,
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "1.1.0",
      "io.github.cquiroz" %%% "scala-java-time" % "2.2.2",
      "com.raquo" %%% "laminar" % "0.12.2",
      "com.raquo" %%% "airstream" % "0.12.2"
    ),
    webpackConfigFile in fastOptJS := Some(baseDirectory.value / "webpack" / "dev.webpack.config.js"),
    npmDependencies in Compile ++= Seq(
      "leaflet" -> "1.7.1",
      "leaflet-realtime" -> "2.2.0"
    ),
    npmDevDependencies in Compile ++= Seq(
      "scalajs-friendly-source-map-loader" -> "0.1.5"
    ),
    (Compile / fastOptJS / webpack) := (Compile / fastOptJS / webpack).dependsOn(sourceMapCleanup).value,
    version in webpack := "4.46.0",
    sourceMapCleanup := {
      SourceMapCleanup.cleanup(
        (Compile / fastLinkJS / scalaJSLinkerOutputDirectory).value,
        (Compile / fastOptJS / fastLinkJS).value
      )
    }
  )
  .enablePlugins(ScalaJSBundlerPlugin)
  .dependsOn(sharedJs)


lazy val shared = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("shared"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.play" %%% "play-json" % "2.9.2",
      "org.scalatest" %%% "scalatest" % "3.2.8" % "test"
    )
  )
  .jsSettings(
    libraryDependencies ++= Seq(
      "io.github.cquiroz" %%% "scala-java-time" % "2.2.2"
    )
  )
  .jsConfigure(_.enablePlugins(ScalaJSBundlerPlugin))
lazy val sharedJvm = shared.jvm
lazy val sharedJs = shared.js

lazy val commonSettings = Seq(
  organization := "com.github.ajablonski",
  scalacOptions += "-feature"
)

