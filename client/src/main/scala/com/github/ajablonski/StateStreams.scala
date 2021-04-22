package com.github.ajablonski

import com.github.ajablonski.shared.model.Route
import com.github.ajablonski.shared.serialization.RouteSerializers
import com.raquo.airstream.core.EventStream
import com.raquo.airstream.web.AjaxEventStream
import com.raquo.laminar.api.L._
import org.scalajs.dom.window
import play.api.libs.json.{Json, OFormat}


object StateStreams {
  private val defaultRoute = "22"

  val currentRouteStream: Var[String] =
    Var(
      Option(
        window
          .localStorage
          .getItem("route"))
        .getOrElse(defaultRoute))

  lazy val routeListStream: EventStream[List[Route]] = {
    implicit val routeReads: OFormat[Route] = RouteSerializers.routeFormat

    AjaxEventStream
      .get("/routes")
      .completeEvents
      .map { xhr =>
        Json.parse(xhr.responseText)
          .as[List[Route]]
      }
  }
}
