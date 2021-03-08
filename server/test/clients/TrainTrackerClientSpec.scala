package clients

import com.github.ajablonski.shared.model.Train
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito._
import org.scalatest.TestData
import org.scalatest.matchers.must.Matchers
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

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future}

class TrainTrackerClientSpec extends PlaySpec with GuiceOneAppPerTest with MockitoSugar with Matchers with Injecting {
  override def newAppForTest(testData: TestData): Application =
    new GuiceApplicationBuilder()
      .configure("app.ctaTrainApi.key" -> "fakekey",
        "app.ctaTrainApi.cacheTimeInSeconds" -> 1,
        "app.ctaTrainApi.baseUrl" -> "/trains")
      .build()

  "TrainTrackerClient" should {
    "correctly parse getVehicles response" in {
      Server.withRouterFromComponents()(TestHelpers.mockCta) { implicit port =>
        WsTestClient.withClient {
          client =>
            val trainTrackerClient = new TrainTrackerClient(client, ExecutionContext.global, app.configuration, inject[SyncCacheApi])
            val result = Await.result(trainTrackerClient.getVehicles("Red"), 1.minute)
            result mustEqual List(Train(
              destination = "Howard",
              runId = "813",
              latitude = 41.87815,
              longitude = -87.6276,
              heading = 357
            ))
        }
      }
    }

    "cache results from CTA Train Tracker" in {
      val client = mock[WSClient](RETURNS_DEEP_STUBS)
      val response = mock[WSResponse]
      val trainTrackerClient = new TrainTrackerClient(client, ExecutionContext.global, app.configuration, inject[SyncCacheApi])
      when(client.url(anyString()).addQueryStringParameters(any[(String, String)]()).get())
        .thenReturn(Future.successful(response))
      when(response.json).thenReturn(Json.parse(TestHelpers.mockTrainVehiclesJSON))

      Await.result(trainTrackerClient.getVehicles("Red"), 1.minute)
      verify(response).json

      Await.result(trainTrackerClient.getVehicles("Red"), 1.minute)
      verifyNoMoreInteractions(response)
      clearInvocations(response)

      Await.result(trainTrackerClient.getVehicles("Blue"), 1.minute)
      verify(response).json
      clearInvocations(response)

      Thread.sleep(1000)

      Await.result(trainTrackerClient.getVehicles("Red"), 1.minute)
      verify(response).json
    }
  }
}
