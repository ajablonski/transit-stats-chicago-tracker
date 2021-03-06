package clients

import com.github.ajablonski.shared.model.Bus
import helpers.Helpers
import play.api.{Configuration, Logger}
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.libs.ws.WSClient

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@Singleton
class BusTrackerClient @Inject()(ws: WSClient, implicit private val ec: ExecutionContext, config: Configuration) {
  private val logger = Logger(this.getClass.getName)
  private val vehiclesEndpoint = "getvehicles"
  private implicit val busReads: Reads[Bus] = (
    (JsPath \ "des").read[String] and
      (JsPath \ "tatripid").read[String] and
      (JsPath \ "tablockid").read[String] and
      (JsPath \ "tmstmp").read[String].map(Helpers.convertCtaDate) and
      (JsPath \ "vid").read[String].map(_.toLong) and
      (JsPath \ "lat").read[String].map(_.toDouble) and
      (JsPath \ "lon").read[String].map(_.toDouble) and
      (JsPath \ "hdg").read[String].map(_.toInt)
    ) (Bus.apply _)
  private val baseUrl = config.get[String]("app.ctaBusApi.baseUrl")
  private val apiKey = config.get[String]("app.ctaBusApi.key")

  def getVehicles(routeId: String): Future[List[Bus]] = {
    ws.url(s"$baseUrl/$vehiclesEndpoint")
      .addQueryStringParameters(
        ("key", apiKey),
        ("tmres", "s"),
        ("rt", routeId),
        ("format", "json")
      )
      .get()
      .flatMap { response =>
        Future.fromTry(Try(
          (response.json \ "bustime-response" \ "vehicle")
            .as[List[Bus]]))
      }
      .recover { error =>
        logger.error("Request failed, exception was", error)
        List.empty[Bus]
      }
  }
}
