package com.github.ajablonski.facades

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport


@js.native
@JSImport("leaflet-realtime", JSImport.Namespace)
class Realtime(source: org.scalajs.dom.experimental.Request, config: js.Dictionary[js.Any]) extends Layer {
  def on(str: String, function: js.Function0[Unit]): Unit = js.native

  def getBounds(): LatLngBounds = js.native
}
