package com.github.ajablonski.components

import com.github.ajablonski.facades
import com.github.ajablonski.facades._
import com.github.ajablonski.shared.model.{Route, Shape}
import com.github.ajablonski.shared.serialization.RouteSerializers
import com.raquo.airstream.web.AjaxEventStream
import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveElement
import org.scalajs.dom.html
import play.api.libs.json.{Json, OFormat}

import java.nio.charset.StandardCharsets
import java.util.Base64
import scala.scalajs.js
import scala.scalajs.js.UndefOr


object LeafletMapManager {
  def apply(routeListStream: EventStream[List[Route]], routeStream: Signal[String]): ReactiveElement[html.Div] = {
    new LeafletMapManager(routeListStream, routeStream).render()
  }
}


private class LeafletMapManager(routeListStream: EventStream[List[Route]], routeStream: Signal[String]) {
  implicit val shapeReads: OFormat[Shape] = RouteSerializers.shapeFormat

  private val mapId = "mapid"
  private val routeRequestsSignal = routeListStream
    .map {
      _.map { route =>
        route.routeId -> MapRouteConfig.fromRoute(route)
      }.toMap
    }
    .startWith(Map())
  private val selectedRoutePolyLines: Signal[List[Polyline]] = routeStream
    .flatMap { route =>
      AjaxEventStream
        .get(s"/routes/$route")
        .map { xhr =>
          Json.parse(xhr.responseText)
            .as[List[Shape]]
            .map(shape => Leaflet.polyline(
              Leaflet.LineUtil.simplify(
                js.Array(shape.path.map(point => js.Array(point.lat, point.lon)): _*),
                js.undefined),
              js.undefined))
        }
        .startWith(List())
    }
  private val loadedLines = Var(List[Polyline]())
  private val hasReBounded = Var(false)
  private val realtimeIcons = Var(Option[Realtime](null))


  def render(): ReactiveElement[html.Div] = {
    div(
      idAttr := mapId,

      onMountCallback(ctx => {
        val map = initMap()

        selectedRoutePolyLines
          .combineWith(routeRequestsSignal, routeStream)
          .addObserver(Observer({ case (polyLines, routeConfigs, route) =>
            Var.update(
              loadedLines -> { currentValue: List[Polyline] =>
                currentValue.foreach(_.removeFrom(map))
                polyLines.foreach(_.addTo(map))
                polyLines
              },
              realtimeIcons -> { currentValue: Option[Realtime] =>
                currentValue.foreach(_.removeFrom(map))
                routeConfigs
                  .get(route)
                  .map(routeConfig =>
                    updateRealtimeRefresh(map, routeConfig)
                  )
              },
              hasReBounded -> { _: Boolean => false }
            )
          }))(ctx.owner)
      })
    )
  }

  private def initMap(): facades.Map = {
    val map = Leaflet.map(mapId, js.Dictionary(
      "center" -> js.Array(41.8781, -87.6298),
      "zoom" -> 13
    ))

    Leaflet.tileLayer("https://stamen-tiles-{s}.a.ssl.fastly.net/toner-lite/{z}/{x}/{y}{r}.{ext}", js.Dictionary(
      "attribution" -> """Map tiles by <a href="http://stamen.com">Stamen Design</a>, <a href="http://creativecommons.org/licenses/by/3.0">CC BY 3.0</a> &mdash; Map data &copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors""",
      "minZoom" -> 0,
      "maxZoom" -> 20,
      "ext" -> "png"
    )).addTo(map)

    map
  }

  private def updateRealtimeRefresh(map: facades.Map, routeConfig: MapRouteConfig): Realtime = {
    val pointToLayer = buildPointToLayerFn(routeConfig.icon)
    val realtime = new Realtime(routeConfig.request, js.Dictionary(
      "pointToLayer" -> pointToLayer,
      "interval" -> 10_000,
      "getFeatureId" -> routeConfig.idFn,
      "onlyRunWhenAdded" -> true,
      "updateFeature" -> {
        (feature: js.Dynamic, oldLayer: UndefOr[FeatureGroup]) => {
          if (oldLayer.isDefined) {
            oldLayer.get.remove()
            val latLng = Leaflet.GeoJSON.coordsToLatLng(feature.geometry.coordinates.asInstanceOf[js.Array[Double]])
            pointToLayer(feature, latLng)
          } else {
            oldLayer
          }
        }
      }
    )).addTo(map)

    realtime.on("update", () => {
      if (!hasReBounded.now()) {
        map.fitBounds(realtime.getBounds())
        hasReBounded.set(true)
      }
    })

    realtime
  }

  private def buildPointToLayerFn(icon: Icon): (js.Dynamic, LatLng) => FeatureGroup = (geoJsonPoint: js.Dynamic, latLon: LatLng) => {
    val group = Leaflet.featureGroup()
    val marker = Leaflet
      .marker(latLon.asInstanceOf[js.Dictionary[Double]], js.Dictionary("icon" -> icon))
    val arrow = Leaflet
      .marker(latLon.asInstanceOf[js.Dictionary[Double]], js.Dictionary("icon" -> buildArrow(geoJsonPoint.properties.heading.asInstanceOf[String].toInt)))
    group.addLayer(marker)
    group.addLayer(arrow)
    group
  }

  private def buildArrow(rotation: Int): Icon = {
    val size = 60
    val arrowWidth = 8
    val arrowHeight = 12

    val arrowSvg =
      f"""<svg xmlns="http://www.w3.org/2000/svg" width="$size" height="$size">
         |  <g transform="rotate($rotation ${size / 2} ${size / 2})">
         |    <polyline stroke="#303f9f" fill="#303f9f" points="${size / 2 - arrowWidth / 2}, $arrowHeight
         |                                                      ${size / 2}, 0
         |                                                      ${size / 2 + arrowWidth / 2}, $arrowHeight"/>
         |  </g>
         |</svg>
         |""".stripMargin
    Leaflet.icon(js.Dictionary(
      "iconUrl" -> f"data:image/svg+xml;base64,${Base64.getEncoder.encodeToString(arrowSvg.getBytes(StandardCharsets.UTF_8))}",
      "iconSize" -> js.Array(size, size),
      "iconAnchor" -> js.Array(size / 2, size / 2)
    ))
  }
}
