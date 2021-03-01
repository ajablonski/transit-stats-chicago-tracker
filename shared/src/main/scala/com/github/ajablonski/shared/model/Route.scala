package com.github.ajablonski.shared.model

case class Route(routeId: String,
                 name: String,
                 `type`: RouteType,
                 color: String,
                 textColor: String)

sealed trait RouteType
case object BusRouteType extends RouteType
case object TrainRouteType extends RouteType

object RouteType {
  def fromGtfsCode(code: String): RouteType = code match {
    case "1" => TrainRouteType
    case "3" => BusRouteType
  }
}
