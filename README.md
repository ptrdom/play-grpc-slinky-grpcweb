# Play gRPC Slinky gRPC-web

This template project was the result of investigation into full-sack `Scala` web development experience with following requirements:

- Compile time type safety
- Streaming API
- Quick hot reload in development
- Single deployment in production

Decision to base the implementation on `gRPC-Web` came after testing out popular 
implementations of `REST`([autowire](https://github.com/lihaoyi/autowire), 
[endpoints4s](https://github.com/endpoints4s/endpoints4s)) and `GraphQL` 
([sangria](https://github.com/sangria-graphql/sangria), 
[caliban](https://github.com/ghostdogpr/caliban)) 
and realisation that none of existing solutions satisfy each requirement, 
while `gRPC`'s basic functionality has all the required features.   

## How to run

### Development mode

Using `sbt`:
- `~serverDev`
- `~clientDev`

Using `IntelliJ` (tested with `2020.3`):
- Enable `sbt shell` for compilation.
- Use Run/Debug configurations provided in `/.run`.

`~serverDev` starts back-end based on Play Framework in `watch` mode. 
It serves both `HTTP` and `gRPC` endpoints for front-end to consume.
`~clientDev` starts `webpack dev server` with `HMR` enabled for `Scala.js`, specifically `Slinky`, front-end development.
After running these commands and opening `localhost:9000`, back-end returns index page with
script definition that wires-in front-end.

### Production mode

Using `sbt`:
- Start `sbt` with production flag enabled - `sbt "-Denv=prod"`
- `server/docker:publishLocal`

In production mode, optimized front-end bundle is packaged and stored in back-end's assets,
then served with aggressive caching enabled by fingerprinting.

## Slinky IntelliJ support

https://slinky.dev/docs/installation/ describes how to add support `@react` for macro.
If documented approach fails, manual plugin installation can be done by downloading 
https://mvnrepository.com/artifact/me.shadaj/slinky-core-ijext `.jar` 
and manually installing it as IntelliJ plugin through `Settings -> Plugins -> Install Plugin from Disk...`. 
Tested with IntelliJ IDEA `2020.3.1` and Slinky `0.6.6`.

## Built on:
- https://github.com/akka/akka-grpc
- https://github.com/playframework/play-grpc
- https://github.com/playframework/playframework
- https://github.com/scalapb/scalapb-grpcweb
- https://github.com/sbt/sbt-web
- https://github.com/vmunier/sbt-web-scalajs
- https://github.com/scalacenter/scalajs-bundler
- https://github.com/shadaj/slinky