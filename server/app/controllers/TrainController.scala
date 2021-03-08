package controllers

import clients.TrainTrackerClient
import com.github.ajablonski.shared.model.Train
import play.api.libs.json.{Json, OFormat}
import play.api.mvc._

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class TrainController @Inject()(val controllerComponents: ControllerComponents, trainTrackerClient: TrainTrackerClient, implicit private val ec: ExecutionContext) extends BaseController {
  implicit private val busJson: OFormat[Train] = Json.format[Train]

  def getVehicles(routeId: String): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    trainTrackerClient
      .getVehicles(routeId)
      .map { trains =>
        if (request.acceptedTypes.exists(_.accepts("application/geo+json"))) {
          Ok(Json.toJson(trains.map(_.toGeoJSON)))
        } else {
          Ok(Json.toJson(trains))
        }
      }
  }
}
