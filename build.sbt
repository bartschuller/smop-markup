lazy val sharedSettings = Seq(
  organization := "org.smop",
  version := "0.1.0-SNAPSHOT",
  scalaVersion := "2.11.8"
  //scalacOptions ++= Seq("-Ymacro-debug-lite")
)

lazy val root = (project in file(".")).
  settings(sharedSettings:_*).
  settings(
    name := "smop-markup",
    libraryDependencies <+= (scalaVersion)("org.scala-lang" % "scala-reflect" % _),
    libraryDependencies ++= Seq(
      "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.4",
      "org.scala-lang.modules" %% "scala-xml" % "1.0.5",
      "org.specs2" %% "specs2-core" % "3.8.4" % "test"
    ),
    scalacOptions in Test ++= Seq("-Yrangepos")
  )
