package controllers

import clients.BusTrackerClient
import com.github.ajablonski.shared.model.Bus
import play.api.libs.json.{JsError, JsSuccess, Json, OFormat}
import play.api.mvc._

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class BusController @Inject()(val controllerComponents: ControllerComponents, busTrackerClient: BusTrackerClient, implicit private val ec: ExecutionContext) extends BaseController {
  implicit private val busJson: OFormat[Bus] = Json.format[Bus]

  def get(routeId: String): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    busTrackerClient
      .getVehicles(routeId)
      .map { buses =>
        if (request.acceptedTypes.exists(_.accepts("application/geo+json"))) {
          Ok(Json.toJson(buses.map(_.toGeoJSON())))
        } else {
          Ok(Json.toJson(buses))
        }
      }
  }
}
