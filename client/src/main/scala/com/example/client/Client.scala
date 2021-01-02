package com.example.client

import com.example.service.Request
import com.example.service.Response
import com.example.service.ServiceGrpcWeb
import io.grpc.stub.StreamObserver
import org.scalajs.dom
import org.scalajs.dom.document
import scalapb.grpc.Channels
import scalapb.grpcweb.Metadata
import slinky.core._
import slinky.core.annotations.react
import slinky.core.facade.Hooks._
import slinky.web.ReactDOM
import slinky.web.html._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js.timers.clearTimeout
import scala.scalajs.js.timers.setTimeout
import scala.util.Failure
import scala.util.Success

object Client {

  val stub = ServiceGrpcWeb.stub(Channels.grpcwebChannel("http://localhost:9000"))

  def main(args: Array[String]): Unit = {
    val container = Option(dom.document.getElementById("root")).getOrElse {
      val elem = dom.document.createElement("div")
      elem.id = "root"
      dom.document.body.appendChild(elem)
      elem
    }
    ReactDOM.render(
      span(
        Unary(),
        Stream(false),
        Stream(true)
      ),
      container
    )
  }

  @react object Unary {
    type Props = Unit
    val component: FunctionalComponent[Props] = FunctionalComponent { _ =>
      val (status, setStatus) = useState("Request pending")

      useEffect(
        () => {
          val req                = Request(payload = "Hello!")
          val metadata: Metadata = Metadata("custom-header-1" -> "unary-value")

          stub.unary(req, metadata).onComplete {
            case Success(value) =>
              setStatus(s"Request success: ${value.payload}")
            case Failure(ex) =>
              setStatus(s"Request failure: $ex")
          }
          setStatus("Request sent")
        },
        Seq.empty
      )

      div(
        h2("Unary request:"),
        p(status)
      )
    }
  }

  @react object Stream {
    case class Props(cancel: Boolean)
    val component: FunctionalComponent[Props] = FunctionalComponent { props =>
      val (status, setStatus) = useState("Request pending")

      useEffect(
        () => {
          val req                = Request(payload = "Hello!")
          val metadata: Metadata = Metadata("custom-header-2" -> "streaming-value")

          var resCount = 0

          val stream = stub.serverStreaming(
            req,
            metadata,
            new StreamObserver[Response] {
              override def onNext(value: Response): Unit = {
                resCount += 1
                setStatus(s"Received success [$resCount]")
              }

              override def onError(ex: Throwable): Unit = {
                setStatus(s"Received failure: $ex")
              }

              override def onCompleted(): Unit = {
                setStatus(s"Received completed")
              }
            }
          )
          setStatus("Request sent")

          val maybeTimer = if (props.cancel) {
            Some(
              setTimeout(5000) {
                setStatus(s"Stream stopped by client")
                stream.cancel()
              }
            )
          } else None
          () => {
            stream.cancel()
            maybeTimer.foreach(clearTimeout)
          }
        },
        Seq.empty
      )

      div(
        h2("Stream request:"),
        p(status)
      )
    }
  }
}
