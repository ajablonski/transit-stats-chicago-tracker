package clients

import com.github.ajablonski.shared.model.Train
import play.api.Configuration
import play.api.cache.SyncCacheApi
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsPath, Reads}
import play.api.libs.ws.WSClient

import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@Singleton
class TrainTrackerClient @Inject()(ws: WSClient,
                                   implicit private val ec: ExecutionContext,
                                   config: Configuration,
                                   cache: SyncCacheApi) {
  private val vehiclesEndpoint = "ttpositions.aspx"

  private val baseUrl = config.get[String]("app.ctaTrainApi.baseUrl")
  private val apiKey = config.get[String]("app.ctaTrainApi.key")
  private val routeCacheTimeSeconds = config.get[Int]("app.ctaTrainApi.cacheTimeInSeconds")

  private implicit val trainReads: Reads[Train] = (
    (JsPath \ "destNm").read[String] and
      (JsPath \ "rn").read[String] and
      (JsPath \ "lat").read[String].map(_.toDouble) and
      (JsPath \ "lon").read[String].map(_.toDouble) and
      (JsPath \ "heading").read[String].map(_.toInt)
    ) (Train.apply _)

  def getVehicles(routeId: String): Future[List[Train]] = {
    cache.getOrElseUpdate(f"route/$routeId", routeCacheTimeSeconds.seconds) {
      ws.url(f"$baseUrl/$vehiclesEndpoint")
        .addQueryStringParameters(
          "key" -> apiKey,
          "outputType" -> "JSON",
          "rt" -> routeId
        )
        .get()
        .flatMap { response =>
          Future.fromTry(Try(
            (response.json \ "ctatt" \ "route" \ 0 \ "train")
              .as[List[Train]]))
        }
    }
  }
}
