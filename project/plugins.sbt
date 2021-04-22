addSbtPlugin("ch.epfl.scala" % "sbt-web-scalajs-bundler" % "0.20.0")
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.8.8")
addSbtPlugin("com.typesafe.sbt" % "sbt-digest" % "1.1.4")
addSbtPlugin("com.typesafe.sbt" % "sbt-gzip" % "1.0.2")
addSbtPlugin("com.vmunier" % "sbt-web-scalajs" % "1.1.0")
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.0.0")
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.5.0")
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.5.1")

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-json" % "2.9.2",
  "org.scala-js" %% "scalajs-linker-interface" % "1.5.0"
)
