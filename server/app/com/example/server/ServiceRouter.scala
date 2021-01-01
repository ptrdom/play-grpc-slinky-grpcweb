package com.example.server

import akka.NotUsed
import akka.actor.ActorSystem
import akka.pattern.after
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import com.example.AbstractServiceRouter
import com.example.Request
import com.example.Response
import javax.inject.Inject

import scala.concurrent.Future
import scala.concurrent.duration._

class ServiceRouter @Inject() (implicit val actorSystem: ActorSystem) extends AbstractServiceRouter(actorSystem) {
  import actorSystem.dispatcher

  /**
    * Unary request.
    */
  override def unary(in: Request): Future[Response] = {
    after(2.seconds)(Future.successful(Response(s"Received [${in.payload}]")))
  }

  /**
    * Server streaming request.
    */
  override def serverStreaming(in: Request): Source[Response, NotUsed] = {
    Source
      .repeat(in)
      .zipWithIndex
      .map {
        case (in, idx) =>
          Response(s"Received [${in.payload}] idx [$idx]")
      }
      .throttle(1, 0.5.seconds)
      .take(20)
  }

  /**
    * Bidi streaming request. gRPC-web does not implement it.
    */
  override def bidiStreaming(in: Source[Request, NotUsed]): Source[Response, NotUsed] = {
    in.map(in => Response(s"Received [${in.payload}]")).throttle(1, 0.5.seconds)
  }

  /**
    * Client streaming request. gRPC-web does not implement it.
    */
  override def clientStreaming(in: Source[Request, NotUsed]): Future[Response] = {
    in.zipWithIndex
      .map {
        case (in, idx) =>
          (in, idx)
      }
      .runWith(Sink.lastOption)
      .map {
        case Some((lastIn, lastIdx)) =>
          Response(s"Received last [$lastIn] idx [$lastIdx]")
        case None =>
          Response(s"Received nothing")
      }
  }
}
