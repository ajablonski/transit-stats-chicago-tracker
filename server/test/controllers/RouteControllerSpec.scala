package controllers

import clients.GtfsClient
import com.github.ajablonski.shared.model.{BusRouteType, Point, Route, Shape}
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
  implicit private val shapeJson: OFormat[Shape] = RouteSerializers.shapeFormat

  "RouteController" when {
    val gtfsClient = mock[GtfsClient]

    val controller = new RouteController(stubControllerComponents(), gtfsClient, scala.concurrent.ExecutionContext.global)

    "calling the getAll endpoint" should {
      "return all provided routes" in {
        val routes = List(Route("22", "Clark", BusRouteType, "000000", "ffffff"))
        when(gtfsClient.getRoutes()).thenReturn(Future.successful(routes))

        val response = controller.getAll().apply(FakeRequest("GET", "/routes"))
        status(response) mustBe OK
        contentAsJson(response).as[List[Route]] mustBe routes
      }
    }

    "calling the get endpoint for a specific route" should {
      "return all applicable shapes for the route" in {
        val pointsShape1 = List(
          Point(41.999893, -87.671272, 0),
          Point(41.999893, -87.671272, 53),
          Point(41.999752, -87.671225, 107),
          Point(41.999617, -87.671177, 166),
          Point(41.999457, -87.671145, 229),
        )
        val pointsShape2 = List(
          Point(41.976387, -87.66849, 0),
          Point(41.976387, -87.66849, 95),
          Point(41.97614, -87.66838, 171),
          Point(41.975933, -87.668363, 224),
          Point(41.97579, -87.668332, 279),
        )
        val shapes = List(
          Shape("63803935", pointsShape1),
          Shape("63803929", pointsShape2)
        )
        when(gtfsClient.getShapesForRoute("22")).thenReturn(Future.successful(shapes))

        val response = controller.getRoute("22")(FakeRequest())
        status(response) mustBe OK
        contentAsJson(response).as[List[Shape]] mustBe shapes
      }
    }

    "the app" when {
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

        "wired up" should {
          "route requests for getting all routes correctly" in {

            val routesResponse = route(app, FakeRequest(GET, "/routes")).get

            status(routesResponse) mustBe OK
            contentType(routesResponse) mustBe Some("application/json")
            contentAsJson(routesResponse).as[List[Route]].find(_.name == "Clark").map(_.routeId) mustBe Some("22")
          }

          "route requests for getting specific routes correctly" in {

            val routesResponse = route(app, FakeRequest(GET, "/routes/22")).get

            status(routesResponse) mustBe OK
            contentType(routesResponse) mustBe Some("application/json")
            val shapes = contentAsJson(routesResponse).as[List[Shape]]
            shapes.find(_.shapeId == "63800246").map(_.path.size) mustBe Some(1146)
          }
        }
      }
    }
  }
}
