package com.github.ajablonski

import com.github.ajablonski.shared.model.Route
import com.github.ajablonski.shared.serialization.RouteSerializers
import com.raquo.airstream.core.EventStream
import com.raquo.airstream.web.AjaxEventStream
import com.raquo.domtypes.generic.codecs.StringAsIsCodec
import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import org.scalajs.dom.{html, window}
import play.api.libs.json.{Json, OFormat}


object Streams {
  val defaultRoute = "22"

  val routeStream = Var(Option(window.localStorage.getItem("route")).getOrElse(defaultRoute))

  def routesEventStream(): EventStream[List[Route]] = {
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
