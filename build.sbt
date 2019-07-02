
lazy val scallion = project
  .in(file("."))
  .settings(
    name               := "scallion",
    version            := "0.1.1",
    scalaVersion       := "2.12.8",
    crossScalaVersions := Seq("2.12.8", "2.13.0"),
    organization       := "ch.epfl.lara",

    scalacOptions ++= Seq(
      "-deprecation",
      "-feature"
    ),

    Compile / doc / scalacOptions ++= Seq(
      "-groups"
    ),

    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.0.8" % "test",
    ),

    bintrayOrganization := Some("epfl-lara"),
    licenses += ("Apache-2.0", url("https://opensource.org/licenses/Apache-2.0")),
    bintrayPackageLabels := Seq(
      "scala", "parser", "parsing",
      "ll1", "ll1-parsing", "ll1-grammar",
      "parser-combinators", "parsing-combinators"
    ),
  )


lazy val benchmarks = project
  .in(file("benchmarks"))
  .enablePlugins(JmhPlugin)
  .settings(
    name := "scallion-benchmarks",
    scalaVersion := "2.12.8",
    scalacOptions ++= Seq(
      "-deprecation",
      "-feature",
      "-opt:_",
    ),
    skip in publish := true,
  )
  .dependsOn(scallion)

