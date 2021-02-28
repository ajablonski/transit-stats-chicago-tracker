package controllers

import com.github.ajablonski.shared.model.Bus
import play.api.libs.json.{JsError, JsSuccess, Json, OFormat}
import play.api.mvc._
import services.BusTrackerClient

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class BusController @Inject()(val controllerComponents: ControllerComponents, busTrackerClient: BusTrackerClient, implicit private val ec: ExecutionContext) extends BaseController {
  implicit private val busJson: OFormat[Bus] = Json.format[Bus]

  def get(routeId: String): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    busTrackerClient
      .getVehicles(routeId)
      .map {
        case JsSuccess(value, _) => Ok(Json.toJson(value))
        case JsError(_) => InternalServerError("Error")
      }
  }
}
