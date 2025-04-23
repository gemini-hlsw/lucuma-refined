ThisBuild / tlBaseVersion := "0.1"

ThisBuild / scalaVersion       := "3.7.0-RC3"
ThisBuild / crossScalaVersions := Seq("3.7.0-RC3")

ThisBuild / tlCiReleaseBranches := Seq("main")

val refinedVersion = "0.11.3"

lazy val root = tlCrossRootProject.aggregate(refined)

lazy val refined = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .in(file("refined"))
  .settings(
    name := "lucuma-refined",
    libraryDependencies ++= Seq(
      "eu.timepit"    %%% "refined" % refinedVersion,
      "org.scalameta" %%% "munit"   % "1.0.4" % Test
    )
  )
