package controllers

import clients.GtfsClient
import com.github.ajablonski.shared.model.{BusRouteType, Route}
import com.github.ajablonski.shared.serialization.RouteSerializers
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.OFormat
import play.api.test.FakeRequest
import play.api.test.Helpers.{stubControllerComponents, _}
import play.core.server.Server

import scala.concurrent.Future

class RouteControllerSpec extends PlaySpec with MockitoSugar {
  implicit private val routeJson: OFormat[Route] = RouteSerializers.routeFormat

  "RouteController" when {
    "calling the getAll endpoint" should {
      "return all provided routes" in {
        val gtfsClient = mock[GtfsClient]
        val routes = List(Route("22", "Clark", BusRouteType, "000000", "ffffff"))
        when(gtfsClient.getRoutes()).thenReturn(Future.successful(routes))
        val controller = new RouteController(stubControllerComponents(), gtfsClient, scala.concurrent.ExecutionContext.global)
        val response = controller.getAll().apply(FakeRequest("GET", "/routes"))
        status(response) mustBe OK
        contentAsJson(response).as[List[Route]] mustBe routes
      }
    }

    "wiring up the app" should {
      "route correctly" in {
        Server.withRouterFromComponents()(TestHelpers.mockCta()) { port =>
          val app = new GuiceApplicationBuilder()
            .configure("app" -> Map(
              "ctaBusApi" -> Map(
                "key" -> "fakeKey",
                "baseUrl" -> f"http://localhost:$port",
                "cacheTimeInSeconds" -> "1"
              ),
              "ctaTrainApi" -> Map(
                "key" -> "fakeKey",
                "baseUrl" -> f"http://localhost:$port",
                "cacheTimeInSeconds" -> "1"
              ),
              "cta" -> Map(
                "gtfsUrl" -> f"http://localhost:$port"
              )
            ))
            .build()
          val routesResponse = route(app, FakeRequest(GET, "/routes")).get

          status(routesResponse) mustBe OK
          contentType(routesResponse) mustBe Some("application/json")
          contentAsJson(routesResponse).as[List[Route]].find(_.name == "Clark").map(_.routeId) mustBe Some("22")
        }
      }
    }
  }
}
