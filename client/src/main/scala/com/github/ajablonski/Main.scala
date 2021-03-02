package com.github.ajablonski

import com.github.ajablonski.shared.SharedMessages
import com.github.ajablonski.shared.model._
import org.scalajs.dom
import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.raw.Event
import upickle.default._

import java.time.LocalDateTime
import scala.scalajs.js


object Main {
  implicit val busRw: Reader[Bus] = macroR[Bus]
  implicit val timeRw: Reader[LocalDateTime] = reader[ujson.Value].map(
    it => LocalDateTime.parse(it.str)
  )
  implicit val routeRw: Reader[Route] = macroR[Route]
  implicit val routeTypeReader: Reader[RouteType] = reader[ujson.Value].map(
    _.obj.get("_type") match {
      case Some(ujson.Str("com.github.ajablonski.shared.model.BusRouteType")) => BusRouteType
      case Some(ujson.Str("com.github.ajablonski.shared.model.TrainRouteType")) => TrainRouteType
    }
  )

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
        read[Seq[Route]](xhr.responseText, trace = true)
          .foreach { route =>
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
          read[Seq[Bus]](xhr.responseText, trace = true)
            .foreach { bus =>
              val marker = Leaflet
                .marker(js.Array(bus.latitude, bus.longitude), js.Dictionary("icon" -> busIcon))
              markerGroup.addLayer(marker)
            }
          markerGroup.addTo(map)
          map.fitBounds(markerGroup.getBounds())
      }

    markerGroup
  }
}
