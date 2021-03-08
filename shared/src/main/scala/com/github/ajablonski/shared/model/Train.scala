package com.github.ajablonski.shared.model

case class Train(destination: String,
                 runId: String,
                 latitude: Double,
                 longitude: Double,
                 heading: Int) extends GeoJSONGenerator {
  override def additionalProperties(): Map[String, String] = {
    Map(
      "heading" -> heading.toString,
      "runId" -> runId,
      "destination" -> destination
    )
  }
}
