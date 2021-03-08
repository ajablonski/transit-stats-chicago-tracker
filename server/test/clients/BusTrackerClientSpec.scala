package clients

import com.github.ajablonski.shared.model.Bus
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito._
import org.scalatest.TestData
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.Application
import play.api.cache.SyncCacheApi
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.test.{Injecting, WsTestClient}
import play.core.server.Server

import java.time.LocalDateTime
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

class BusTrackerClientSpec extends PlaySpec with GuiceOneAppPerTest with MockitoSugar with Injecting {
  override def newAppForTest(testData: TestData): Application =
    new GuiceApplicationBuilder()
      .configure("app.ctaBusApi.key" -> "fakekey",
        "app.ctaBusApi.cacheTimeInSeconds" -> 1,
        "app.ctaBusApi.baseUrl" -> "/buses")
      .build()


  "BusTrackerClient" should {
    "correctly parse getVehicles response" in {
      Server.withRouterFromComponents()(TestHelpers.mockCta) { implicit port =>
        WsTestClient.withClient {
          client =>
            val busTrackerClient = new BusTrackerClient(client, ExecutionContext.global, app.configuration, inject[SyncCacheApi])
            val result = Await.result(busTrackerClient.getVehicles("76"), 1.minute)
            result mustEqual List(Bus(destination = "Nature Museum",
              tripId = "372",
              blockId = "76 -405",
              timestamp = LocalDateTime.parse("2021-02-27T15:42:55"),
              vehicleId = 8286,
              latitude = 41.93092727661133,
              longitude = -87.79379762922015,
              heading = 92))
        }
      }
    }

    "cache results from CTA Bus Tracker" in {
      val client = mock[WSClient](RETURNS_DEEP_STUBS)
      val response = mock[WSResponse]
      val busTrackerClient = new BusTrackerClient(client, ExecutionContext.global, app.configuration, inject[SyncCacheApi])
      when(client.url(anyString()).addQueryStringParameters(any[(String, String)]()).get())
        .thenReturn(Future.successful(response))
      when(response.json).thenReturn(Json.parse(TestHelpers.mockBusVehiclesJSON))

      Await.result(busTrackerClient.getVehicles("76"), 1.minute)
      verify(response).json

      Await.result(busTrackerClient.getVehicles("76"), 1.minute)
      verifyNoMoreInteractions(response)
      clearInvocations(response)

      Await.result(busTrackerClient.getVehicles("22"), 1.minute)
      verify(response).json
      clearInvocations(response)


      Thread.sleep(1000)

      Await.result(busTrackerClient.getVehicles("76"), 1.minute)
      verify(response).json
    }
  }
}
