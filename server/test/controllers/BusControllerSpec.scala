package controllers

import com.github.ajablonski.shared.model.Bus
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.http.Status.OK
import play.api.libs.json.{JsArray, JsSuccess, Json}
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Injecting}
import services.BusTrackerClient

import java.time.LocalDateTime
import scala.concurrent.{ExecutionContext, Future}

class BusControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting with ScalaFutures {
  implicit private val busJson = Json.format[Bus]

  "RouteController GET" should {
    "return a list of all buses on route right now" in {
      val request = FakeRequest("GET", "/routes/76")
      val mockBusTrackerClient = mock[BusTrackerClient]
      when(mockBusTrackerClient.getVehicles("76")) thenReturn Future.successful(JsSuccess(List(Bus("Harlem", "123", "456", LocalDateTime.now(), 789, 41.93092727661133, -87.79379762922015))))
      val response = new BusController(stubControllerComponents(), mockBusTrackerClient, inject[ExecutionContext]).get("76").apply(request)


      status(response) mustBe OK
      contentType(response) mustBe Some("application/json")
      val responseAsJsValue = contentAsJson(response)
      responseAsJsValue mustBe a[JsArray]
      val buses = responseAsJsValue.as[List[Bus]]
      buses
    }
  }
}
