# Play gRPC Slinky gRPC-web

Built on:
- https://github.com/akka/akka-grpc
- https://github.com/playframework/play-grpc
- https://github.com/playframework/playframework
- https://github.com/scalapb/scalapb-grpcweb
- https://github.com/sbt/sbt-web
- https://github.com/vmunier/sbt-web-scalajs
- https://github.com/scalacenter/scalajs-bundler
- https://github.com/shadaj/slinky

## How to run

Using `sbt`:
- `"project server" ~compile`
- `"project server" ~run`

Using `IntelliJ` (tested with `2020.3`):
- Use Run/Debug configurations provided in `/.run`.

## Slinky IntelliJ support

https://slinky.dev/docs/installation/ describes how to add support `@react` for macro.
If documented approach fails, manual plugin installation can be done by downloading 
https://mvnrepository.com/artifact/me.shadaj/slinky-core-ijext `.jar` 
and manually installing it as IntelliJ plugin through `Settings -> Plugins -> Install Plugin from Disk...`. 
Tested with IntelliJ IDEA `2020.3.1` and Slinky `0.6.6`.