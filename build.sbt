ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.12"

lazy val root = (project in file("."))
  .settings(
    name := "KardiaCataloger",
    idePackagePrefix := Some("org.bpc"),
    libraryDependencies += "org.bytedeco" % "javacv-platform" % "1.5.8",
    libraryDependencies += "org.jocl" % "jocl" % "2.0.4",
    libraryDependencies += "net.sourceforge.tess4j" % "tess4j" % "5.4.0",
    libraryDependencies += "org.scala-lang.modules" %% "scala-swing" % "3.0.0",
    libraryDependencies += "com.lihaoyi" %% "upickle" % "2.0.0",
    libraryDependencies += "org.apache.commons" % "commons-text" % "1.10.0",
      libraryDependencies += "com.formdev" % "flatlaf" % "3.2.5",
      unmanagedResourceDirectories in Compile += baseDirectory.value / "src" / "main" / "resources"
  )
