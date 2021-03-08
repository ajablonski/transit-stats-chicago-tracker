package com.github.ajablonski.shared.model

trait GeoJSONGenerator extends HasLatLon {
  def toGeoJSON: GeoJSONFeature = {
    GeoJSONFeature(
      geometry = GeoJSONPoint(
        List(longitude, latitude)
      ),
      properties = additionalProperties()
    )
  }

  def additionalProperties(): Map[String, String] = {
    Map()
  }
}

trait HasLatLon {
  val latitude: Double
  val longitude: Double
}
