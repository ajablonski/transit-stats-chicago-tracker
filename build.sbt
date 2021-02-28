name := """ct-delay"""
version := "1.0-SNAPSHOT"

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
      specs2 % Test,
      "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test
    )
  )
  .enablePlugins(PlayScala, WebScalaJSBundlerPlugin)
  .dependsOn(sharedJvm)

scalaVersion := "2.13.3"

lazy val client = (project in file("client"))
  .settings(commonSettings)
  .settings(
    scalaJSUseMainModuleInitializer := true,
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "1.1.0"
    ),
    npmDependencies in Compile ++= Seq(
      "leaflet" -> "1.7.1"
    )
  )
  .enablePlugins(ScalaJSBundlerPlugin)
  .dependsOn(sharedJs)


lazy val shared = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("shared"))
  .settings(commonSettings)
  .jsConfigure(_.enablePlugins(ScalaJSWeb))
lazy val sharedJvm = shared.jvm
lazy val sharedJs = shared.js

lazy val commonSettings = Seq(
  scalaVersion := "2.13.3",
  organization := "com.github.ajablonski"
)

