resolvers += "sonatype-releases" at "https://oss.sonatype.org/content/repositories/releases/"

resolvers += "Spark Package Main Repo" at "https://dl.bintray.com/spark-packages/maven"

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.0.2")
addSbtPlugin("com.jsuereth" % "sbt-pgp" % "2.0.0")
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.6.0")
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.10")
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.10.0-RC1")
