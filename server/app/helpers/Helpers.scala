package helpers

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object Helpers {
  private val ctaFormat = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss")

  def convertCtaDate(dateString: String): LocalDateTime = {
    LocalDateTime.parse(dateString, ctaFormat)
  }
}
