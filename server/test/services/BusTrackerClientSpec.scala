package services

import clients.BusTrackerClient
import com.github.ajablonski.shared.model.Bus
import org.scalatest.TestData
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.JsSuccess
import play.api.test.WsTestClient
import play.core.server.Server

import java.time.LocalDateTime
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}

class BusTrackerClientSpec extends PlaySpec with GuiceOneAppPerTest {
  override def newAppForTest(testData: TestData): Application =
    new GuiceApplicationBuilder()
      .configure("app.ctaBusApi.key" -> "fakekey",
        "app.ctaBusApi.baseUrl" -> "")
      .build()


  "BusTrackerClient" should {
    "correctly parse getVehicles response" in {
      Server.withRouterFromComponents()(TestHelpers.mockCta) { implicit port =>
        WsTestClient.withClient {
          client =>
            val busTrackerClient = new BusTrackerClient(client, ExecutionContext.global, app.configuration)
            val result = Await.result(busTrackerClient.getVehicles("76"), 1.minute)
            result mustEqual JsSuccess(List(Bus(destination = "Nature Museum",
              tripId = "372",
              blockId = "76 -405",
              timestamp = LocalDateTime.parse("2021-02-27T15:42:55"),
              vehicleId = 8286,
              latitude = 41.93092727661133,
              longitude = -87.79379762922015,
              heading = 92)))
        }
      }
    }
  }
}
