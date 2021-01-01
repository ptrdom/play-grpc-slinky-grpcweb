import com.example.BuildInfo

scalaVersion in ThisBuild := "2.13.2"

resolvers in ThisBuild ++= Seq(
  Resolver.sonatypeRepo("snapshots"),
  Resolver.sonatypeRepo("releases")
)

lazy val akkaVersion              = play.core.PlayVersion.akkaVersion
lazy val akkaHttpVersion          = play.core.PlayVersion.akkaHttpVersion
lazy val akkaGrpcVersion          = "1.0.2"
lazy val playVersion              = play.core.PlayVersion.current
lazy val playGrpcVersion          = BuildInfo.playGrpcVersion
lazy val scalaTestPlusPlayVersion = "5.0.0"
lazy val scalaJsDomVersion        = "1.1.0"
lazy val scalaJsScriptsVersion    = "1.1.4"

lazy val `play-grpc-scala-js-grpcweb` = (project in file("."))
  .aggregate(
    client,
    server
  )

lazy val proto =
  crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Pure)
    .in(file("proto"))
    .enablePlugins(AkkaGrpcPlugin)
    .settings(
      PB.protoSources in Compile := Seq(
        (baseDirectory in ThisBuild).value / "proto" / "src" / "main" / "protobuf"
      )
    )
    .jvmSettings(
      akkaGrpcExtraGenerators += play.grpc.gen.scaladsl.PlayScalaServerCodeGenerator,
      libraryDependencies += "com.lightbend.play" %% "play-grpc-runtime"   % playGrpcVersion,
      libraryDependencies += "com.lightbend.play" %% "play-grpc-scalatest" % playGrpcVersion % Test
    )
    .jsSettings(
      libraryDependencies += "com.thesamet.scalapb"         %%% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion,
      libraryDependencies += "com.thesamet.scalapb.grpcweb" %%% "scalapb-grpcweb" % scalapb.grpcweb.BuildInfo.version,
      PB.targets in Compile := Seq(
        scalapb.gen(grpc = false)            -> (sourceManaged in Compile).value,
        scalapb.grpcweb.GrpcWebCodeGenerator -> (sourceManaged in Compile).value
      )
    )

lazy val protoJs  = proto.js
lazy val protoJVM = proto.jvm

lazy val client =
  project
    .in(file("client"))
    .enablePlugins(ScalaJSBundlerPlugin)
    .settings(
      scalaJSUseMainModuleInitializer := true,
      libraryDependencies += "org.scala-js" %%% "scalajs-dom" % scalaJsDomVersion,
      webpackBundlingMode in fastOptJS := BundlingMode.LibraryOnly(),
      webpackEmitSourceMaps in fastOptJS := false
    )
    .dependsOn(protoJs)

lazy val server = project
  .enablePlugins(PlayScala, AkkaGrpcPlugin, PlayAkkaHttp2Support, WebScalaJSBundlerPlugin)
  .in(file("server"))
  .settings(
    scalaJSProjects := Seq(client),
    pipelineStages in Assets := Seq(scalaJSPipeline),
    pipelineStages := Seq(digest, gzip),
    libraryDependencies ++= Seq(
      guice,
      "com.typesafe.akka"      %% "akka-discovery"       % akkaVersion,
      "com.typesafe.akka"      %% "akka-http"            % akkaHttpVersion,
      "com.typesafe.akka"      %% "akka-http-spray-json" % akkaHttpVersion,
      "com.vmunier"            %% "scalajs-scripts"      % scalaJsScriptsVersion,
      "com.typesafe.play"      %% "play-test"            % playVersion % Test,
      "org.scalatestplus.play" %% "scalatestplus-play"   % scalaTestPlusPlayVersion % Test
    )
  )
  .dependsOn(protoJVM)
