package com.github.ajablonski.shared.model

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.time.{LocalDateTime, ZoneId}

class BusSpec extends AnyWordSpec with Matchers {
  "Bus" should {
    "convert to GeoJSON" in {
      Bus("Harlem", "123", "456", LocalDateTime.now(ZoneId.of("UTC")), 789, 41.93092727661133, -87.79379762922015, 92)
        .toGeoJSON shouldBe GeoJSONFeature(
        GeoJSONPoint(List(-87.79379762922015, 41.93092727661133)),
        Map(
          "heading" -> "92",
          "vehicleId" -> "789",
          "blockId" -> "456"
        )
      )
    }
  }
}
