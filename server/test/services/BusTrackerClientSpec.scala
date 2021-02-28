package services

import model.Bus
import org.scalatest.TestData
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsArray, JsSuccess}
import play.api.routing.sird._
import play.api.test.WsTestClient
import play.core.server.Server

import java.time.LocalDateTime
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}

class BusTrackerClientSpec extends PlaySpec with GuiceOneAppPerTest {
  implicit override def newAppForTest(testData: TestData): Application =
    new GuiceApplicationBuilder()
      .configure(Map("app.ctaBusApi.key" -> "fakekey",
      "app.ctaBusApi.baseUrl" -> ""))
      .build()


  "BusTrackerClient" should {
    "correctly parse getVehicles response" in {
      Server.withRouterFromComponents() { components =>
        import components.{defaultActionBuilder => Action}
        import play.api.mvc.Results._
        {
          case GET(p"/getvehicles") => Action {
            Ok(
              """
                |{
                |    "bustime-response": {
                |        "vehicle": [
                |            {
                |                "des": "Nature Museum",
                |                "dly": false,
                |                "hdg": "92",
                |                "lat": "41.93092727661133",
                |                "lon": "-87.79379762922015",
                |                "pdist": 3338,
                |                "pid": 4619,
                |                "rt": "76",
                |                "tablockid": "76 -405",
                |                "tatripid": "372",
                |                "tmstmp": "20210227 15:42:55",
                |                "vid": "8286",
                |                "zone": ""
                |            }
                |        ]
                |    }
                |}""".stripMargin).withHeaders(("Content-Type", "application/json;charset=utf-8"))
          }
        }
      } { implicit port =>
        WsTestClient.withClient {
          client =>
            val busTrackerClient = new BusTrackerClient(client, ExecutionContext.global, app.configuration)
            val result = Await.result(busTrackerClient.getVehicles(76), 1.minute)
            result mustEqual JsSuccess(List(Bus(destination = "Nature Museum",
              tripId = "372",
              blockId = "76 -405",
              timestamp = LocalDateTime.parse("2021-02-27T15:42:55"),
              vehicleId = 8286,
              latitude = BigDecimal("41.93092727661133"),
              longitude = BigDecimal("-87.79379762922015"))))
        }
      }
    }
  }
}
