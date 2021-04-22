package com.github.ajablonski.components

import com.github.ajablonski.shared.model.Route
import com.raquo.airstream.core.EventStream
import com.raquo.domtypes.generic.codecs.StringAsIsCodec
import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import org.scalajs.dom.{html, window}

object RouteDropDown {
  def apply(routesEventStream: EventStream[List[Route]],
            routeStream: Var[String]): ReactiveHtmlElement[html.Select] = {
    new RouteDropDown(routesEventStream, routeStream).render()
  }
}


private class RouteDropDown(routesEventStream: EventStream[List[Route]],
                            routeStream: Var[String]) {
  def render(): ReactiveHtmlElement[html.Select] = {
    select(
      idAttr := "routes",
      children <-- routesDomElementEventStream,
      controlled(
        value <-- routeStream.signal,
        onChange.mapToValue --> routeStream.writer
      ),
      onChange.mapToValue --> {
        window.localStorage.setItem("route", _)
      }
    )
  }

  private val routesDomElementEventStream = routesEventStream.map {
    _.map { route =>
      option(
        customHtmlAttr("label", StringAsIsCodec) := f"${route.routeId}: ${route.name}",
        value := route.routeId,
        f"${route.routeId}: ${route.name}",
        selected := route.routeId == routeStream.now()
      )
    }
  }
}
