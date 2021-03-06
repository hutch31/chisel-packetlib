
// Only needed if not organized according to the sbt standard
//unmanagedSourceDirectories in Compile += "ip/src"
Compile / unmanagedSourceDirectories += baseDirectory.value / "ip" / "src" / "main" / "scala" / "chisel" / "lib" / "dclib"

// This is the latest release from UCB
// libraryDependencies += "edu.berkeley.cs" %% "chisel3" % "latest.release"

// libraryDependencies += "edu.berkeley.cs" %% "chisel3" % "3.1-SNAPSHOT"

// This is from a locally published version
// libraryDependencies += "edu.berkeley.cs" %% "chisel" % "2.3-SNAPSHOT"

def scalacOptionsVersion(scalaVersion: String): Seq[String] = {
  Seq() ++ {
    // If we're building with Scala > 2.11, enable the compile option
    //  switch to support our anonymous Bundle definitions:
    //  https://github.com/scala/bug/issues/10047
    CrossVersion.partialVersion(scalaVersion) match {
      case Some((2, scalaMajor: Long)) if scalaMajor < 12 => Seq()
      case _ => Seq("-Xsource:2.11")
    }
  }
}

def javacOptionsVersion(scalaVersion: String): Seq[String] = {
  Seq() ++ {
    // Scala 2.12 requires Java 8. We continue to generate
    //  Java 7 compatible code for Scala 2.11
    //  for compatibility with old clients.
    CrossVersion.partialVersion(scalaVersion) match {
      case Some((2, scalaMajor: Long)) if scalaMajor < 12 =>
        Seq("-source", "1.7", "-target", "1.7")
      case _ =>
        Seq("-source", "1.8", "-target", "1.8")
    }
  }
}

name := "chisel-packetlib"

version := "1.0.0"

val scala211 = "2.11.12"
val scala212 = "2.12.8"

scalaVersion := scala212

crossScalaVersions := Seq(scala212, scala211)

resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots"),
  Resolver.sonatypeRepo("releases")
)

// Provide a managed dependency on X if -DXVersion="" is supplied on the command line.
val defaultVersions = Map(
  "chisel-iotesters" -> "1.5-SNAPSHOT",
  "chiseltest"       -> "0.3-SNAPSHOT"
)

libraryDependencies ++= Seq("chiseltest", "chisel-iotesters").map {
  dep: String => "edu.berkeley.cs" %% dep % sys.props.getOrElse(dep + "Version", defaultVersions(dep)) }

scalacOptions ++= scalacOptionsVersion(scalaVersion.value)

javacOptions ++= javacOptionsVersion(scalaVersion.value)
