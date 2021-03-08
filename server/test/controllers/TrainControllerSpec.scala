package controllers

import clients.TrainTrackerClient
import com.github.ajablonski.shared.model.Train
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.http.Status.OK
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json._
import play.api.mvc.{AnyContentAsEmpty, Headers}
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Injecting}
import play.core.server.Server

import scala.concurrent.{ExecutionContext, Future}

class TrainControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting with ScalaFutures with MockitoSugar {
  implicit private val trainJson: OFormat[Train] = Json.format[Train]
  private val fakeTrain = Train("Howard", "800", 41.93092727661133, -87.79379762922015, 355)

  "TrainController GET" should {
    "return a list of all buses on route right now" in {
      val request = FakeRequest("GET", "/routes/Red")
      val mockTrainTrackerClient = mock[TrainTrackerClient]
      when(mockTrainTrackerClient.getVehicles("Red")) thenReturn Future.successful(List(fakeTrain))
      val response = new TrainController(stubControllerComponents(), mockTrainTrackerClient, inject[ExecutionContext]).getVehicles("Red").apply(request)

      status(response) mustBe OK
      contentType(response) mustBe Some("application/json")
      val responseAsJsValue = contentAsJson(response)
      responseAsJsValue mustBe a[JsArray]
      responseAsJsValue.as[List[Train]] mustBe List(fakeTrain)
    }

    "return a geojson format list when geojson format requested" in {
      val request = FakeRequest[AnyContentAsEmpty.type]("GET", "/routes/Red", body = AnyContentAsEmpty, headers = Headers("Accept" -> "application/geo+json"))
      val mockTrainTrackerClient = mock[TrainTrackerClient]
      when(mockTrainTrackerClient.getVehicles("Red")) thenReturn Future.successful(List(fakeTrain))
      val response = new TrainController(stubControllerComponents(), mockTrainTrackerClient, inject[ExecutionContext]).getVehicles("Red").apply(request)

      status(response) mustBe OK
      contentType(response) mustBe Some("application/json")
      val responseAsJsValue = contentAsJson(response)
      responseAsJsValue mustBe a[JsArray]
      responseAsJsValue.as[JsArray].head mustEqual JsDefined(Json.obj(
        "type" -> JsString("Feature"),
        "geometry" -> Json.obj(
          "type" -> JsString("Point"),
          "coordinates" -> Json.arr(
            JsNumber(-87.79379762922015),
            JsNumber(41.93092727661133))),
        "properties" -> Json.obj(
          "heading" -> JsString("355"),
          "runId" -> JsString("800"),
          "destination" -> JsString("Howard"))
      ))
    }

    "route correctly" in {
      Server.withRouterFromComponents()(TestHelpers.mockCta) { port =>
        val app = new GuiceApplicationBuilder()
          .configure("app" -> Map(
            "ctaTrainApi" -> Map(
              "key" -> "fakeKey",
              "baseUrl" -> f"http://localhost:$port/trains"
            ),
            "ctaBusApi" -> Map(
              "key" -> "fakeKey"
            ),
            "cta" -> Map(
              "gtfsUrl" -> f"http://localhost:$port"
            )
          ))
          .build()
        val routesResponse = route(app, FakeRequest(GET, "/trains/routes/Red/vehicles")).get

        status(routesResponse) mustBe OK
        contentType(routesResponse) mustBe Some("application/json")
        contentAsJson(routesResponse).as[List[Train]].find(_.destination == "Howard").map(_.runId) mustBe Some("813")
      }
    }
  }
}
