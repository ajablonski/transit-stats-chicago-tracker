package helpers

import org.scalatest.{Matchers, WordSpec}

import java.time.LocalDateTime

class HelpersSpec extends WordSpec with Matchers {
  "convertCtaDate" should {
    "convert CTA Date to LocalDateTime" in {
      Helpers.convertCtaDate("20210227 15:42:55") shouldBe LocalDateTime.parse("2021-02-27T15:42:55")
    }
  }
}
