package com.example.server

import javax.inject.Inject
import play.api.Environment
import play.api.Mode
import play.api.mvc._
import play.twirl.api.StringInterpolation

class WebServiceController @Inject() (
    cc: ControllerComponents,
    environment: Environment
) extends AbstractController(cc) {

  def index(): Action[AnyContent] = Action {
    if (environment.mode == Mode.Prod) {
      Ok(views.html.com.example.server.indexFullOptJs())
    } else {
      Ok(views.html.com.example.server.indexFastOptJs())
    }
  }
}
