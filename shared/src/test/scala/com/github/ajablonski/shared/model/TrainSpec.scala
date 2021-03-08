package com.github.ajablonski.shared.model

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class TrainSpec extends AnyWordSpec with Matchers {
  "Train" should {
    "convert to GeoJSON" in {
      Train("Howard", "800", 41.93092727661133, -87.79379762922015, 2)
        .toGeoJSON shouldBe GeoJSONFeature(
        GeoJSONPoint(List(-87.79379762922015, 41.93092727661133)),
        Map(
          "heading" -> "2",
          "runId" -> "800",
          "destination" -> "Howard"
        )
      )
    }
  }
}
