ThisBuild / scalaVersion := "3.2.0"
cancelable in Global := true
run / fork := true

lazy val cardscanner = (project in file("."))
  .settings (
    name := "cardscanner",
    Compile / scalaSource := baseDirectory.value / "src",
    libraryDependencies += "org.bytedeco" % "javacv-platform" % "1.5.8",
    libraryDependencies += "org.jocl" % "jocl" % "2.0.4",
    libraryDependencies += "net.sourceforge.tess4j" % "tess4j" % "5.4.0",
    libraryDependencies += "org.scala-lang.modules" %% "scala-swing" % "3.0.0"


  )




