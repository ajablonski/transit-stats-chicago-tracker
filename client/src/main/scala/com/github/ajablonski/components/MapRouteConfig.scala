package com.github.ajablonski.components

import com.github.ajablonski.facades.{Icon, Leaflet}
import com.github.ajablonski.shared.model.{BusRouteType, Route, TrainRouteType}
import org.scalajs.dom.experimental.{HttpMethod, Request, RequestInit}

import scala.scalajs.js


case class MapRouteConfig(request: Request, idFn: js.Dynamic => String, icon: Icon)

object MapRouteConfig {
  def fromRoute(route: Route): MapRouteConfig = {
    val (routeType, idFn, icon) = route.`type` match {
      case BusRouteType => ("buses", {
        (_: js.Dynamic).properties.blockId.asInstanceOf[String]
      }, busIcon)
      case TrainRouteType => ("trains", {
        (_: js.Dynamic).properties.runId.asInstanceOf[String]
      }, trainIcon)
    }
    val request = new Request(f"/$routeType/routes/${route.routeId}/vehicles", new RequestInit() {
      method = HttpMethod.GET
      headers = js.Dictionary[String]("Accept" -> "application/geo+json")
    })
    MapRouteConfig(request, idFn, icon)
  }

  private val busIcon: Icon = {
    val size = 40
    Leaflet.icon(js.Dictionary(
      "iconUrl" -> "assets/images/chicken.png",
      "iconSize" -> js.Array(size, size),
      "iconAnchor" -> js.Array(size / 2, size / 2)
    ))
  }

  private val trainIcon: Icon = {
    val width = 556 / 14
    val height = 364 / 14
    Leaflet.icon(js.Dictionary(
      "iconUrl" -> "assets/images/corgi.png",
      "iconSize" -> js.Array(width, height),
      "iconAnchor" -> js.Array(width / 2, height / 2)
    ))
  }
}
