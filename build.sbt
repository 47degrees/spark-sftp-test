val PureconfigVersion     = "0.10.2"
val SparkVersion          = "2.4.4"
val CatsEffectVersion     = "2.0.0"
val LogbackVersion        = "1.2.3"
//val Log4catsVersion       = "0.4.0-M2"
val Log4catsVersion       = "1.0.0"

import org.scalafmt.sbt.ScalafmtPlugin.autoImport._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "org.47deg",
      scalaVersion := "2.12.8"
    )),
    name := "sparksftpTest",
    version := "0.0.1",

    //sparkVersion := "2.4.4",
    //sparkComponents := Seq(),

    javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
    javaOptions ++= Seq("-Xms512M", "-Xmx2048M", "-XX:+CMSClassUnloadingEnabled"),
    scalacOptions ++= Seq("-deprecation", "-unchecked"),
    parallelExecution in Test := false,
    fork := true,

    coverageHighlighting := true,

    libraryDependencies ++= Seq(
      "org.apache.spark" %% "spark-streaming" % SparkVersion % "provided",
      "org.apache.spark" %% "spark-sql" % SparkVersion % "provided",
      "com.github.pureconfig" %% "pureconfig"  %  PureconfigVersion,
      "org.typelevel" %% "cats-effect" % CatsEffectVersion,
      "org.fortysevendeg" %% "spark-sftp" % "1.1.7-SNAPSHOT",
      "org.apache.spark" %% "spark-hive" % "2.4.4",
      "org.scalatest" %% "scalatest" % "3.0.1" % "test",
      "org.scalacheck" %% "scalacheck" % "1.13.4" % "test",
      "com.holdenkarau" %% "spark-testing-base" % "2.4.3_0.12.0" % "test",
      //"ch.qos.logback"  %  "logback-classic"     % LogbackVersion,
      "io.chrisdavenport" %% "log4cats-core"     % Log4catsVersion,
      "io.chrisdavenport" %% "log4cats-slf4j"     % Log4catsVersion
    ).map(_.exclude("org.slf4j", "slf4j-log4j12")),
    addCompilerPlugin("org.typelevel" %% "kind-projector"     % "0.10.3"),
    addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.1"),
    addCompilerPlugin("org.scalamacros" % "paradise"            % "2.1.0" cross CrossVersion.full),

    // uses compile classpath for the run task, including "provided" jar (cf http://stackoverflow.com/a/21803413/3827)
    run in Compile := Defaults.runTask(fullClasspath in Compile, mainClass in (Compile, run), runner in (Compile, run)).evaluated,

    scalacOptions ++= Seq("-deprecation", "-unchecked"),
    pomIncludeRepository := { x => false },

   resolvers ++= Seq(
      "sonatype-releases" at "https://oss.sonatype.org/content/repositories/releases/",
      "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/",
      "Second Typesafe repo" at "https://repo.typesafe.com/typesafe/maven-releases/",
      Resolver.sonatypeRepo("public")
    ),

    pomIncludeRepository := { x => false },

    // publish settings
    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      if (isSnapshot.value)
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases"  at nexus + "service/local/staging/deploy/maven2")
    }
  )
