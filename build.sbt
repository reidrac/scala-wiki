val CommonmarkVersion = "0.18.0"
val DoobieVersion = "1.0.0-RC1"
val H2DatabaseVersion = "1.4.200"
val Http4sVersion = "0.23.10"
val Log4CatsVersion = "2.2.0"
val LogbackVersion = "1.2.10"

lazy val root = (project in file("."))
  .settings(
    organization := "net.usebox.net",
    name := "wiki",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.13.8",
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % LogbackVersion % Runtime,
      "com.h2database" % "h2" % H2DatabaseVersion,
      "org.commonmark" % "commonmark" % CommonmarkVersion,
      "org.http4s" %% "http4s-dsl" % Http4sVersion,
      "org.http4s" %% "http4s-ember-client" % Http4sVersion,
      "org.http4s" %% "http4s-ember-server" % Http4sVersion,
      "org.http4s" %% "http4s-scalatags" % Http4sVersion,
      "org.tpolecat" %% "doobie-core" % DoobieVersion,
      "org.tpolecat" %% "doobie-h2" % DoobieVersion,
      "org.typelevel" %% "log4cats-slf4j" % Log4CatsVersion
    ),
    addCompilerPlugin(
      "org.typelevel" %% "kind-projector" % "0.13.2" cross CrossVersion.full
    ),
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
  )
