package com.github.ajablonski

import com.github.ajablonski.shared.SharedMessages
import com.github.ajablonski.shared.model.Bus
import org.scalajs.dom

import scala.scalajs.js
import upickle.default._
import upickle.default.{macroRW, ReadWriter => RW}

import java.time.LocalDateTime


object ScalaJSExample {
  implicit val rw: RW[Bus] = macroRW[Bus]
  implicit val timeRw: RW[LocalDateTime] = readwriter[ujson.Value].bimap[LocalDateTime](
    _.toString,
    it => LocalDateTime.parse(it.str)
  )

  def main(args: Array[String]): Unit = {
    import dom.ext._

    import scala.concurrent.ExecutionContext.Implicits.global

    dom.document.getElementById("scalajsShoutOut").textContent = SharedMessages.itWorks
    val mymap = Leaflet.map("mapid", js.Dictionary(
      "center" -> js.Array(41.8781, -87.6298),
      "zoom" -> 13
    ))

    Leaflet.tileLayer("https://stamen-tiles-{s}.a.ssl.fastly.net/toner-lite/{z}/{x}/{y}{r}.{ext}", js.Dictionary(
      "attribution" -> """Map tiles by <a href="http://stamen.com">Stamen Design</a>, <a href="http://creativecommons.org/licenses/by/3.0">CC BY 3.0</a> &mdash; Map data &copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors""",
      "subdomains" -> "abcd",
      "minZoom" -> 0,
      "maxZoom" -> 20,
      "ext" -> "png"
    )).addTo(mymap)

    val busIcon = Leaflet.icon(js.Dictionary(
      "iconUrl" -> "assets/images/bus.png",
      "iconSize" -> js.Array(18, 18),
      "iconAnchor" -> js.Array(9, 9)
    ))

    Ajax
      .get("/routes/22")
      .foreach {
        xhr =>
          read[Seq[Bus]](xhr.responseText, trace = true)
            .foreach { bus =>
              Leaflet
                .marker(js.Array(bus.latitude, bus.longitude), js.Dictionary("icon" -> busIcon))
                .addTo(mymap)
            }
      }
  }
}
