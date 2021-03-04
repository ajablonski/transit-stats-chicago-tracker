package com.github.ajablonski

import com.github.ajablonski.shared.SharedMessages
import com.github.ajablonski.shared.model._
import com.github.ajablonski.shared.serialization.RouteSerializers
import org.scalajs.dom
import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.raw.Event
import play.api.libs.json._

import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.util.Base64
import scala.scalajs.js


object Main {
  implicit val routeReads = RouteSerializers.routeFormat
  implicit val localDateTimeReads = Reads[LocalDateTime] {
    case JsString(value) => JsSuccess(LocalDateTime.parse(value))
    case _ => JsError("String expected")
  }
  implicit val busReads = Json.reads[Bus]

  private val busIcon = Leaflet.icon(js.Dictionary(
    "iconUrl" -> "assets/images/bus.png",
    "iconSize" -> js.Array(18, 18),
    "iconAnchor" -> js.Array(9, 9)
  ))

  val defaultRoute = "22"

  var icons: FeatureGroup = _

  def main(args: Array[String]): Unit = {

    dom.document.getElementById("scalajsShoutOut").textContent = SharedMessages.itWorks

    val map = initMap()
    initSelect(onRouteChange(map))
    icons = updateMap(map, defaultRoute)
  }

  def onRouteChange(map: Map): Event => Unit = { (event: Event) =>
    icons.remove()
    icons = updateMap(map, event.target.asInstanceOf[dom.html.Select].value)
  }

  def initSelect(routeChangeFunction: js.Function1[Event, _]): Unit = {
    import scala.concurrent.ExecutionContext.Implicits.global

    val routesSelect: dom.html.Select = dom.document.getElementById("routes").asInstanceOf[dom.html.Select]

    routesSelect.onchange = routeChangeFunction
    Ajax
      .get("/routes")
      .foreach(xhr => {

        Json.parse(xhr.responseText)
          .as[List[Route]]
          .foreach {
            route =>
              val option = dom.document.createElement("option").asInstanceOf[dom.html.Option]
              option.label = f"${route.routeId}: ${route.name}"
              option.innerText = f"${route.routeId}: ${route.name}"
              option.value = route.routeId

              routesSelect.appendChild(option)
          }
        routesSelect.value = defaultRoute
      })
  }

  def initMap(): Map = {
    val map = Leaflet.map("mapid", js.Dictionary(
      "center" -> js.Array(41.8781, -87.6298),
      "zoom" -> 13
    ))

    Leaflet.tileLayer("https://stamen-tiles-{s}.a.ssl.fastly.net/toner-lite/{z}/{x}/{y}{r}.{ext}", js.Dictionary(
      "attribution" -> """Map tiles by <a href="http://stamen.com">Stamen Design</a>, <a href="http://creativecommons.org/licenses/by/3.0">CC BY 3.0</a> &mdash; Map data &copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors""",
      "subdomains" -> "abcd",
      "minZoom" -> 0,
      "maxZoom" -> 20,
      "ext" -> "png"
    )).addTo(map)

    map
  }

  def updateMap(map: Map, route: String): FeatureGroup = {
    import scala.concurrent.ExecutionContext.Implicits.global

    val markerGroup = Leaflet.featureGroup()

    Ajax
      .get(f"/routes/$route")
      .foreach {
        xhr =>
          Json.parse(xhr.responseText)
            .as[List[Bus]]
            .foreach { bus =>
              val marker = Leaflet
                .marker(js.Array(bus.latitude, bus.longitude), js.Dictionary("icon" -> busIcon))
              val arrow = Leaflet
                .marker(js.Array(bus.latitude, bus.longitude), js.Dictionary("icon" -> buildArrow(bus.heading)))
              markerGroup.addLayer(marker)
              markerGroup.addLayer(arrow)
            }

          markerGroup.addTo(map)
          map.fitBounds(markerGroup.getBounds())
      }

    markerGroup
  }

  def buildArrow(rotation: Int): Icon = {
    val size = 60
    val arrowWidth = 8
    val arrowHeight = 12
    val arrowSvg =
      f"""<svg xmlns="http://www.w3.org/2000/svg" xmlns:se="http://svg-edit.googlecode.com" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:cc="http://creativecommons.org/ns#" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:inkscape="http://www.inkscape.org/namespaces/inkscape" width="$size" height="$size">
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
