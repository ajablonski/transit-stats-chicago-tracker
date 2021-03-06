package controllers

import clients.GtfsClient
import com.github.ajablonski.shared.model.Route
import com.github.ajablonski.shared.serialization.RouteSerializers
import play.api.libs.json.{Json, OFormat}
import play.api.mvc._

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext


@Singleton
class RouteController @Inject()(val controllerComponents: ControllerComponents,
                                gtfsClient: GtfsClient,
                                implicit private val ec: ExecutionContext)
  extends BaseController {
  implicit private val routeJson: OFormat[Route] = RouteSerializers.routeFormat

  def getAll: Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    gtfsClient
      .getRoutes()
      .map { routes =>
        Ok(Json.toJson(routes))
      }
  }
}
