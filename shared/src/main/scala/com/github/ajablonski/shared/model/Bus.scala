package com.github.ajablonski.shared.model

import java.time.LocalDateTime

case class Bus(destination: String,
               tripId: String,
               blockId: String,
               timestamp: LocalDateTime,
               vehicleId: Long,
               latitude: Double,
               longitude: Double,
               heading: Int) {
  def toGeoJSON(): GeoJSONFeature = {
    GeoJSONFeature(
      geometry = GeoJSONPoint(
        List(longitude, latitude)
      ),
      properties = Map(
        "heading" -> heading.toString,
        "blockId" -> blockId,
        "vehicleId" -> vehicleId.toString
      )
    )
  }
}
