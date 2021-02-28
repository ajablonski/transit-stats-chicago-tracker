package com.github.ajablonski

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport


@JSImport("leaflet", JSImport.Namespace)
@js.native
object Leaflet extends js.Object {
  def map(id: String, config: js.Dictionary[js.Any]): Map = js.native

  def tileLayer(url: String, config: js.Dictionary[js.Any]): TileLayer = js.native
}

@JSImport("leaflet", "Map")
@js.native
class Map extends js.Object {

}

@JSImport("leaflet", "TileLayer")
@js.native
class TileLayer extends js.Object {
  def addTo(map: Map) = js.native
}