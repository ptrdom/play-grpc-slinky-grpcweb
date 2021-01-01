enablePlugins(BuildInfoPlugin)
buildInfoPackage := "com.example"

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.0.0")

addSbtPlugin("org.scala-js"  % "sbt-scalajs"             % "1.3.1")
addSbtPlugin("ch.epfl.scala" % "sbt-web-scalajs-bundler" % "0.20.0")

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.2")

libraryDependencies += "com.thesamet.scalapb.grpcweb" %% "scalapb-grpcweb-code-gen" % "0.4.2"

addSbtPlugin("com.lightbend.akka.grpc" % "sbt-akka-grpc" % "1.0.2")

addSbtPlugin("com.typesafe.sbt" % "sbt-gzip" % "1.0.2")

addSbtPlugin("com.typesafe.sbt" % "sbt-digest" % "1.1.4")

val playGrpcVersion = "0.9.1"
buildInfoKeys ++= Seq[BuildInfoKey]("playGrpcVersion" -> playGrpcVersion)
addSbtPlugin("com.typesafe.play"                      % "sbt-plugin" % "2.8.7")
resolvers += Resolver.bintrayRepo("playframework", "maven")
libraryDependencies += "com.lightbend.play" %% "play-grpc-generators" % playGrpcVersion

addDependencyTreePlugin
