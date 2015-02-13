resolvers ++= Seq(Classpaths.typesafeReleases, Classpaths.typesafeSnapshots)

addSbtPlugin("com.github.retronym" % "sbt-onejar" % "0.8")

addSbtPlugin("com.typesafe.sbt" % "sbt-pgp" % "0.8.1")

addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.3.0")

addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "0.2.1")

retrieveManaged := true
