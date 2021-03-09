package com.github.ajablonski.components

import com.github.ajablonski.facades.{Icon, Leaflet}
import com.github.ajablonski.shared.model.{BusRouteType, Route, TrainRouteType}
import org.scalajs.dom.experimental.{HttpMethod, Request, RequestInit}

import java.nio.charset.StandardCharsets
import java.util.Base64
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
    val size = 30
    val busSvg =
      f"""
         |<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="black" width="${size}px" height="${size}px">
         |  <path d="M0 0h24v24H0z" fill="none"/>
         |  <path d="M4 16c0 .88.39 1.67 1 2.22V20c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-1h8v1c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-1.78c.61-.55 1-1.34 1-2.22V6c0-3.5-3.58-4-8-4s-8 .5-8 4v10zm3.5 1c-.83 0-1.5-.67-1.5-1.5S6.67 14 7.5 14s1.5.67 1.5 1.5S8.33 17 7.5 17zm9 0c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5-.67 1.5-1.5 1.5zm1.5-6H6V6h12v5z"/>
         |</svg>""".stripMargin
    Leaflet.icon(js.Dictionary(
      "iconUrl" -> f"data:image/svg+xml;base64,${Base64.getEncoder.encodeToString(busSvg.getBytes(StandardCharsets.UTF_8))}",
      "iconSize" -> js.Array(size, size),
      "iconAnchor" -> js.Array(size / 2, size / 2)
    ))
  }

  private val trainIcon: Icon = {
    val width = 42.389 * .5
    val height = 50.18 * .5
    val trainSvg =
      f"""
         |<svg xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:cc="http://creativecommons.org/ns#" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:svg="http://www.w3.org/2000/svg" xmlns="http://www.w3.org/2000/svg" xmlns:sodipodi="http://sodipodi.sourceforge.net/DTD/sodipodi-0.dtd" xmlns:inkscape="http://www.inkscape.org/namespaces/inkscape" version="1.1" x="0px" y="0px" width="$width" height="$height" viewBox="0 0 42.389 50.18" enable-background="new 0 0 612 792" xml:space="preserve" id="svg78" sodipodi:docname="train_new_new.svg" inkscape:version="1.0.2 (e86c8708, 2021-01-15)">
         |  <g id="g76" transform="translate(-76.938811,-701.42891)">
         |  <path fill-rule="evenodd" clip-rule="evenodd" d="m 79.026811,744.05291 0.14,0.409 0.233,0.349 0.292,0.339 0.338,0.291 0.361,0.292 0.409,0.232 0.454,0.234 0.922,0.303 0.909,0.21 0.897,0.059 0.198,-0.035 0.175,-0.023 0.338,-0.058 v -6.601 h 1.819 2.473 9.119 0.269 9.177999 2.624 1.399 v 6.658 l 0.338,0.082 0.374,0.034 h 0.104 0.315 l 0.467,-0.058 0.908,-0.175 0.922,-0.351 0.466,-0.198 0.396,-0.269 0.373,-0.279 0.339,-0.292 0.292,-0.339 0.231,-0.349 0.141,-0.408 0.081,-0.396 2.007,-18.716 -1.772,-16.49 -0.151,-0.757 -0.198,-0.688 -0.257,-0.642 -0.339,-0.596 -0.431,-0.559 -0.548,-0.491 -0.63,-0.432 -0.746,-0.372 -0.969,-0.433 -0.897,-0.314 -0.104,-0.035 -1.004,-0.303 -1.003,-0.292 -0.104,-0.023 -1.936,-0.384 -0.688,-0.094 -1.341,-0.198 -2.029,-0.198 -2.041,-0.116 -3.766999,-0.141 -0.268,-0.011 -4.069,0.151 -2.041,0.116 -2.007,0.198 -1.002,0.152 -1.026,0.14 -1.003,0.175 -0.443,0.093 -0.56,0.14 -0.967,0.292 -0.806,0.245 -0.198,0.058 -1.004,0.35 -0.979,0.433 -0.746,0.372 -0.629,0.432 -0.537,0.491 -0.432,0.559 -0.35,0.596 -0.28,0.642 -0.176,0.688 -0.139,0.757 -1.772,16.454 1.994,18.693 z m 30.527999,-10.763 -0.126,0.373 -0.188,0.326 -0.245,0.291 -0.292,0.245 -0.349,0.187 -0.374,0.117 -0.432,0.046 -0.396,-0.046 -0.386,-0.117 -0.337,-0.187 -0.327,-0.245 -0.245,-0.291 -0.186,-0.326 -0.094,-0.373 -0.058,-0.408 0.058,-0.408 0.094,-0.362 0.186,-0.361 0.245,-0.292 0.327,-0.245 0.337,-0.185 0.386,-0.105 0.396,-0.059 0.432,0.059 0.374,0.105 0.349,0.185 0.292,0.245 0.245,0.292 0.188,0.361 0.126,0.362 0.035,0.408 z m 4.806,0 -0.116,0.373 -0.187,0.326 -0.245,0.291 -0.315,0.245 -0.314,0.187 -0.407,0.117 -0.386,0.046 h -0.023 l -0.396,-0.046 -0.372,-0.117 -0.352,-0.187 -0.29,-0.245 -0.27,-0.291 -0.197,-0.326 -0.094,-0.373 -0.035,-0.408 0.035,-0.408 0.094,-0.362 0.197,-0.361 0.27,-0.292 0.29,-0.245 0.352,-0.185 0.372,-0.105 0.396,-0.059 h 0.023 l 0.386,0.059 0.407,0.105 0.314,0.185 0.315,0.245 0.245,0.292 0.187,0.361 0.116,0.362 0.046,0.408 z m -10.076,-24.186 0.035,-0.245 0.095,-0.268 0.139,-0.246 0.198,-0.245 0.175,-0.187 0.223,-0.162 0.232,-0.106 0.21,-0.034 h 1.959 2.624 2.216 0.537 l 0.384,0.058 0.233,0.083 0.245,0.128 0.269,0.221 0.21,0.279 0.162,0.339 0.082,0.433 1.505,15.159 0.046,0.513 -0.081,0.385 -0.163,0.269 -0.21,0.187 -0.245,0.105 -0.258,0.058 -0.43,0.023 h -2.286 -2.216 -2.624 -1.878 l -0.315,-0.105 -0.291,-0.186 -0.222,-0.187 -0.175,-0.211 -0.256,-0.466 -0.13,-0.385 v -15.207 z m -10.214999,-2.926 h 4.303 4.314999 v 29.351 h -4.314999 -4.303 z m -7.929,27.088 -0.095,0.373 -0.197,0.35 -0.232,0.291 -0.305,0.245 -0.349,0.187 -0.362,0.117 -0.407,0.046 h -0.013 l -0.407,-0.046 -0.386,-0.117 -0.35,-0.187 -0.292,-0.245 -0.231,-0.291 -0.188,-0.35 -0.116,-0.373 -0.047,-0.408 0.047,-0.396 0.116,-0.386 0.188,-0.349 0.231,-0.293 0.292,-0.256 0.35,-0.175 0.386,-0.14 0.407,-0.023 h 0.013 l 0.407,0.023 0.362,0.14 0.349,0.175 0.305,0.256 0.232,0.293 0.197,0.349 0.095,0.386 0.058,0.396 z m 4.769,0 -0.105,0.373 -0.198,0.35 -0.232,0.291 -0.291,0.245 -0.327,0.187 -0.374,0.117 -0.396,0.046 -0.408,-0.046 -0.373,-0.117 -0.351,-0.187 -0.291,-0.245 -0.244,-0.291 -0.175,-0.35 -0.117,-0.373 -0.023,-0.408 0.023,-0.396 0.117,-0.386 0.175,-0.349 0.244,-0.293 0.291,-0.256 0.351,-0.175 0.373,-0.14 0.408,-0.023 0.396,0.023 0.374,0.14 0.327,0.175 0.291,0.256 0.232,0.293 0.198,0.349 0.105,0.386 0.047,0.396 z m -10.227,-9.352 1.644,-14.705 0.105,-0.536 0.175,-0.409 0.222,-0.302 0.245,-0.176 0.21,-0.141 0.152,-0.058 0.139,-0.023 h 0.606 2.332 2.473 1.901 l 0.232,0.023 0.245,0.081 0.245,0.13 0.245,0.163 0.198,0.197 0.163,0.234 0.116,0.244 0.046,0.303 v 15.568 l -0.104,0.291 -0.104,0.245 -0.164,0.187 -0.15,0.187 -0.222,0.141 -0.245,0.104 -0.561,0.187 h -1.842 -2.472 -2.332 -1.666 l -0.352,-0.023 -0.35,-0.082 -0.349,-0.15 -0.305,-0.198 -0.232,-0.269 -0.164,-0.35 -0.104,-0.409 z" id="path66"/>
         |  <rect x="89.941811" y="745.00891" fill-rule="evenodd" clip-rule="evenodd" width="16.733999" height="2.4489999" id="rect68"/>
         |  <rect x="86.396812" y="740.84491" fill-rule="evenodd" clip-rule="evenodd" width="2.2969999" height="10.764" id="rect72"/>
         |  <rect x="107.73781" y="740.84491" fill-rule="evenodd" clip-rule="evenodd" width="2.296" height="10.764" id="rect74"/>
         |  </g>
         |</svg>
         |""".stripMargin
    Leaflet.icon(js.Dictionary(
      "iconUrl" -> f"data:image/svg+xml;base64,${Base64.getEncoder.encodeToString(trainSvg.getBytes(StandardCharsets.UTF_8))}",
      "iconSize" -> js.Array(width, height),
      "iconAnchor" -> js.Array(width / 2, height / 2)
    ))
  }
}
