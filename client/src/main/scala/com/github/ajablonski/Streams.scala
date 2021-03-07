package com.github.ajablonski

import com.github.ajablonski.shared.model.Route
import com.github.ajablonski.shared.serialization.RouteSerializers
import com.raquo.airstream.core.EventStream
import com.raquo.airstream.web.AjaxEventStream
import com.raquo.domtypes.generic.codecs.StringAsIsCodec
import play.api.libs.json.{Json, OFormat}
import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import org.scalajs.dom.html


object Streams {
  val defaultRoute = "22"

  val routeStream = Var(defaultRoute)

  def routesEventStream(): EventStream[List[ReactiveHtmlElement[html.Option]]] = {
    implicit val routeReads: OFormat[Route] = RouteSerializers.routeFormat

    AjaxEventStream
      .get("/routes")
      .completeEvents
      .map { xhr =>
        Json.parse(xhr.responseText)
          .as[List[Route]]
          .map { route =>
            val opt = option(
              customHtmlAttr("label", StringAsIsCodec) := f"${route.routeId}: ${route.name}",
              value := route.routeId,
              f"${route.routeId}: ${route.name}"
            )
            if (route.routeId == routeStream.now()) {
              opt.amend(selected := true)
            } else {
              opt
            }
          }
      }
  }
}
