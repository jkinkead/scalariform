resolvers ++= Seq(Classpaths.typesafeReleases, Classpaths.typesafeSnapshots)

addSbtPlugin("com.github.retronym" % "sbt-onejar" % "0.8")

addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.0.0")

addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.3.0")

addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "0.2.2")

retrieveManaged := true
