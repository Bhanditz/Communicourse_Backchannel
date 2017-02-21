name := "Backchannel"

version := "1.0"

lazy val `backchannel` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(cache , ws, evolutions  , specs2 % Test )

libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.2.8"

libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.5"

libraryDependencies += "com.typesafe.play" %% "play-slick-evolutions" % "1.1.1"

libraryDependencies += "com.typesafe.play" %% "play-slick" % "1.1.1"

libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.34"

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )  

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
