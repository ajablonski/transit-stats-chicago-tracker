package com.github.ajablonski

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport


@JSImport("leaflet", JSImport.Namespace)
@js.native
object Leaflet extends js.Object {
  def map(id: String, config: js.Dictionary[js.Any]): Map = js.native

  def tileLayer(url: String, config: js.Dictionary[js.Any]): TileLayer = js.native

  def icon(config: js.Dictionary[js.Any]): Icon = js.native

  def marker(coords: js.Array[Double], config: js.Dictionary[js.Any]): Marker = js.native
}

@JSImport("leaflet", "Map")
@js.native
class Map extends js.Object {}

@JSImport("leaflet", "TileLayer")
@js.native
class TileLayer extends js.Object {
  def addTo(map: Map): TileLayer = js.native
}

@JSImport("leaflet", "Marker")
@js.native
class Marker extends js.Object {
  def addTo(map: Map): Marker = js.native
}

@JSImport("leaflet", "Icon")
@js.native
class Icon extends js.Object {}