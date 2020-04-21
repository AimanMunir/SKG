val dottyVersion = "0.22.0-RC1"

lazy val root = project
  .in(file("."))
  .settings(
    name := "dotty-simple",
    version := "0.1.0",

    scalaVersion := dottyVersion,
    

    libraryDependencies += ("org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.2").withDottyCompat(scalaVersion.value),	
   
    libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test",
     

    scalacOptions ++= Seq(
     "-noindent"
    )
  )
