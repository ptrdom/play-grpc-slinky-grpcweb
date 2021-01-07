import BuildEnvPlugin.autoImport
import BuildEnvPlugin.autoImport.BuildEnv
import com.example.BuildInfo
import com.typesafe.sbt.packager.docker.Cmd
import com.typesafe.sbt.packager.docker.CmdLike
import com.typesafe.sbt.packager.docker.DockerAlias
import com.typesafe.sbt.packager.docker.DockerChmodType
import com.typesafe.sbt.packager.docker.ExecCmd

scalaVersion in ThisBuild := "2.13.4"

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
lazy val slinkyVersion            = "0.6.6"
lazy val reactVersion             = "16.12.0"
lazy val reactProxyVersion        = "1.1.8"

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
      libraryDependencies += "me.shadaj" %%% "slinky-web" % slinkyVersion,
      libraryDependencies += "me.shadaj" %%% "slinky-hot" % slinkyVersion,
      scalacOptions += "-Ymacro-annotations",
      npmDependencies in Compile += "react"                  -> reactVersion,
      npmDependencies in Compile += "react-dom"              -> reactVersion,
      npmDependencies in Compile += "react-proxy"            -> reactProxyVersion,
      npmDevDependencies in Compile += "file-loader"         -> "6.0.0",
      npmDevDependencies in Compile += "style-loader"        -> "1.2.1",
      npmDevDependencies in Compile += "css-loader"          -> "3.5.3",
      npmDevDependencies in Compile += "html-webpack-plugin" -> "4.3.0",
      npmDevDependencies in Compile += "copy-webpack-plugin" -> "5.1.1",
      npmDevDependencies in Compile += "webpack-merge"       -> "4.2.2",
      scalaJSStage := {
        autoImport.buildEnv.value match {
          case BuildEnv.Production =>
            FullOptStage
          case _ =>
            FastOptStage
        }
      },
      webpackResources := baseDirectory.value / "webpack" * "*",
      webpackConfigFile in fastOptJS := Some(baseDirectory.value / "webpack" / "webpack-fastopt.config.js"),
      webpackConfigFile in fullOptJS := Some(baseDirectory.value / "webpack" / "webpack-opt.config.js"),
      webpackConfigFile in Test := Some(baseDirectory.value / "webpack" / "webpack-core.config.js"),
      webpackDevServerExtraArgs in fastOptJS := Seq("--inline", "--hot"),
      webpackBundlingMode in fastOptJS := BundlingMode.LibraryOnly(),
      requireJsDomEnv in Test := true
    )
    .dependsOn(protoJs)

lazy val server = project
  .enablePlugins(PlayScala, AkkaGrpcPlugin, PlayAkkaHttp2Support, WebScalaJSBundlerPlugin)
  .in(file("server"))
  .settings(
    scalaJSProjects := {
      autoImport.buildEnv.value match {
        case BuildEnv.Production =>
          Seq(client)
        case _ =>
          Seq.empty
      }
    },
    pipelineStages in Assets := {
      autoImport.buildEnv.value match {
        case BuildEnv.Production =>
          Seq(scalaJSPipeline)
        case _ =>
          Seq.empty
      }
    },
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
  .settings(
    dockerAliases in Docker += DockerAlias(None, None, "play-grpc-slinky-grpcweb", None),
    packageName in Docker := "play-grpc-slinky-grpcweb",
    dockerBaseImage := "openjdk:8-alpine",
    dockerCommands := {
      val (stage0, stage1)           = dockerCommands.value.splitAt(8)
      val (stage1part1, stage1part2) = stage1.splitAt(3)
      stage0 ++ stage1part1 ++ Seq(ExecCmd("RUN", "apk", "add", "--no-cache", "bash")) ++ stage1part2
    },
    dockerExposedPorts ++= Seq(9000),
    dockerEntrypoint := Seq(
      "/opt/docker/bin/server",
      "-Dconfig.resource=docker-application.conf"
    )
  )
  .dependsOn(protoJVM)

addCommandAlias("clientDev", "client/fastOptJS::startWebpackDevServer;~client/fastOptJS")
addCommandAlias("serverDev", "~server/run")
