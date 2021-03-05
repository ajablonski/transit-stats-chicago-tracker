package com.github.ajablonski.shared.model

import play.api.libs.json.{JsValue, Json, Writes}

sealed trait GeoJSON {
  val `type`: String
  val geometry: GeoJSONGeometry
  val properties: Map[String, Any]
}

case class GeoJSONFeature(geometry: GeoJSONGeometry, properties: Map[String, String]) extends GeoJSON {
  override val `type` = "Feature"
}

object GeoJSON {
  implicit val geoJsonFeatureWrites: Writes[GeoJSONFeature] = new Writes[GeoJSONFeature]() {
    override def writes(o: GeoJSONFeature): JsValue = Json.obj(
      "type" -> o.`type`,
      "geometry" -> GeoJSONGeometry.writes.writes(o.geometry),
      "properties" -> o.properties
    )
  }
}

sealed trait GeoJSONGeometry {
  val `type`: String
  val coordinates: Any
}

object GeoJSONGeometry {
  val writes: Writes[GeoJSONGeometry] = {
    case x: GeoJSONPoint => GeoJSONPoint.geoJsonPointWrites.writes(x)
  }
}

case class GeoJSONPoint(override val coordinates: List[Double]) extends GeoJSONGeometry {
  require(coordinates.length == 2, "Expecting a single coordinate pair")
  override val `type` = "Point"
}

object GeoJSONPoint {
  implicit val geoJsonPointWrites: Writes[GeoJSONPoint] = (o: GeoJSONPoint) => Json.obj(
    "type" -> o.`type`,
    "coordinates" -> o.coordinates
  )
}
