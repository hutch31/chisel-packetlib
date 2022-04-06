import org.apache.logging.log4j.core.config.composite.MergeStrategy
import sun.security.tools.PathList
// See README.md for license details.
ThisBuild / scalaVersion     := "2.13.8"
ThisBuild / version          := "0.1.0"
ThisBuild / organization     := "org.ghutchis"

val chiselVersion = "3.5.1"

lazy val root = (project in file("."))
  .settings(
    name := "packetlib",
    libraryDependencies ++= Seq(
      "edu.berkeley.cs" %% "chisel3" % chiselVersion,
      "edu.berkeley.cs" %% "chiseltest" % "0.5.1" % "test",
      "net.sourceforge.argparse4j" % "argparse4j" % "0.9.0",
      "javax.xml.bind" % "jaxb-api" % "2.3.1"
    ),
    scalacOptions ++= Seq(
      "-language:reflectiveCalls",
      "-deprecation",
      "-feature",
      "-Xcheckinit",
      "-P:chiselplugin:genBundleElements",
    ),
    addCompilerPlugin("edu.berkeley.cs" % "chisel3-plugin" % chiselVersion cross CrossVersion.full),
  )

Compile / mainClass := Some("GeneratorMain")
//assembly / mainClass := Some("GeneratorMain")
//exportJars := true
