package com.github.ajablonski

import com.github.ajablonski.shared.SharedMessages
import org.scalajs.dom

import scala.scalajs.js

object ScalaJSExample {
  def main(args: Array[String]): Unit = {
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
  }
}
