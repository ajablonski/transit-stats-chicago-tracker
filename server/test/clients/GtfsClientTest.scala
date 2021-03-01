package clients

import akka.util.ByteString
import com.github.ajablonski.shared.model.{BusRouteType, Route}
import net.lingala.zip4j.ZipFile
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.{RETURNS_DEEP_STUBS, verify, verifyNoMoreInteractions, when}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.Configuration
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.test.WsTestClient
import play.core.server.Server

import java.io.File
import java.nio.file.Files
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.jdk.CollectionConverters._

class GtfsClientTest extends AnyWordSpec with Matchers with MockitoSugar {

  private val zipFile: File = {
    val filesToZip = new File(Thread.currentThread().getContextClassLoader.getResource("test_gtfs").getPath).listFiles()
    val tempFilePath = Files.createTempDirectory("GtfsClientTest").resolve("gtfs.zip").toFile
    new ZipFile(tempFilePath).addFiles(filesToZip.toList.asJava)
    tempFilePath.deleteOnExit()

    tempFilePath
  }


  "getRoutes" should {
    "return the routes provided in the associated zip file" in {

      Server.withRouterFromComponents()(TestHelpers.mockCta){ implicit port =>
        WsTestClient.withClient { wsClient =>
          val client = new GtfsClient(wsClient, ExecutionContext.global, Configuration(
            "app.cta.gtfsUrl" -> ""
          ))
          val routes = Await.result(client.getRoutes(), 1.minute)
          routes should have size 133
          routes.find(_.routeId == "22") shouldBe Some(Route("22", "Clark", BusRouteType, "565a5c", "ffffff"))
        }
      }
    }

    "use cached files on subsequent calls" in {

      val wsClient: WSClient = mock[WSClient](RETURNS_DEEP_STUBS)
      val mockResponse: WSResponse = mock[WSResponse]
      when(wsClient.url("http://fakeurl.com/downloads/sch_data/google_transit.zip").get())
        .thenReturn(Future.successful(mockResponse))
      when(mockResponse.bodyAsBytes)
        .thenReturn(ByteString(Files.readAllBytes(zipFile.toPath)))

      val client = new GtfsClient(wsClient, ExecutionContext.global, Configuration(
        "app.cta.gtfsUrl" -> "http://fakeurl.com"
      ))

      Await.result(client.getRoutes(), 1.minute)

      verify(wsClient.url(anyString())).get()
      verify(mockResponse).bodyAsBytes

      Await.result(client.getRoutes(), 1.minute)

      verifyNoMoreInteractions(wsClient.url(anyString()))
      verifyNoMoreInteractions(mockResponse)
    }
  }
}
