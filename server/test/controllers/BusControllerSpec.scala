package controllers

import clients.BusTrackerClient
import com.github.ajablonski.shared.model.{Bus, GeoJSON}
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.http.Status.OK
import play.api.libs.json.{JsArray, JsDefined, JsLookupResult, JsNumber, JsObject, JsString, JsSuccess, Json, OFormat}
import play.api.mvc.{AnyContentAsEmpty, Headers}
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Injecting}

import java.time.LocalDateTime
import scala.concurrent.{ExecutionContext, Future}

class BusControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting with ScalaFutures with MockitoSugar {
  implicit private val busJson: OFormat[Bus] = Json.format[Bus]
  private val fakeBus = Bus("Harlem", "123", "456", LocalDateTime.now(), 789, 41.93092727661133, -87.79379762922015, 92)

  "BusController GET" should {
    "return a list of all buses on route right now" in {
      val request = FakeRequest("GET", "/routes/76")
      val mockBusTrackerClient = mock[BusTrackerClient]
      when(mockBusTrackerClient.getVehicles("76")) thenReturn Future.successful(List(fakeBus))
      val response = new BusController(stubControllerComponents(), mockBusTrackerClient, inject[ExecutionContext]).getVehicles("76").apply(request)


      status(response) mustBe OK
      contentType(response) mustBe Some("application/json")
      val responseAsJsValue = contentAsJson(response)
      responseAsJsValue mustBe a[JsArray]
      responseAsJsValue.as[List[Bus]] mustBe List(fakeBus)
    }

    "return a geojson format list when geojson format requested" in {
      val request = FakeRequest[AnyContentAsEmpty.type]("GET", "/routes/76", body = AnyContentAsEmpty, headers = Headers("Accept" -> "application/geo+json"))
      val mockBusTrackerClient = mock[BusTrackerClient]
      when(mockBusTrackerClient.getVehicles("76")) thenReturn Future.successful(List(fakeBus))
      val response = new BusController(stubControllerComponents(), mockBusTrackerClient, inject[ExecutionContext]).getVehicles("76").apply(request)


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
          "heading" -> JsString("92"),
          "vehicleId" -> JsString("789"),
          "blockId" -> JsString("456"))
      ))
    }
  }
}
