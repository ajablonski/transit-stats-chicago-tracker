import net.lingala.zip4j.ZipFile
import play.api.BuiltInComponents
import play.api.http.ContentTypes
import play.api.mvc.{Handler, RequestHeader}
import play.api.routing.sird._

import java.io.File
import java.nio.file.Files
import scala.concurrent.duration.{Duration, DurationInt}
import scala.jdk.CollectionConverters._

package object TestHelpers {
  val gtfsZipFile: File = {
    val filesToZip = new File(Thread.currentThread().getContextClassLoader.getResource("test_gtfs").getPath)
      .listFiles()
      .toList
    val gtfsFile = Files
      .createTempDirectory("GtfsClientTest")
      .resolve("gtfs.zip")
      .toFile
    new ZipFile(gtfsFile).addFiles(filesToZip.asJava)
    gtfsFile.deleteOnExit()
    gtfsFile
  }

  val mockBusVehiclesJSON =
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
      |}""".stripMargin

  val mockTrainVehiclesJSON =
    """
      |{
      |  "ctatt": {
      |    "tmst": "2021-03-07T19:50:58",
      |    "errCd": "0",
      |    "errNm": null,
      |    "route": [
      |      {
      |        "@name": "red",
      |        "train": [
      |          {
      |            "rn": "813",
      |            "destSt": "30173",
      |            "destNm": "Howard",
      |            "trDr": "1",
      |            "nextStaId": "41090",
      |            "nextStpId": "30211",
      |            "nextStaNm": "Monroe",
      |            "prdt": "2021-03-07T19:50:38",
      |            "arrT": "2021-03-07T19:51:38",
      |            "isApp": "1",
      |            "isDly": "0",
      |            "flags": null,
      |            "lat": "41.87815",
      |            "lon": "-87.6276",
      |            "heading": "357"
      |          }
      |        ]
      |      }
      |    ]
      |  }
      |}""".stripMargin

  def mockCta(waitTime: Duration = 0.seconds)(components: BuiltInComponents): PartialFunction[RequestHeader, Handler] = {
    import components.{defaultActionBuilder => Action}
    import play.api.mvc.Results._
    {
      case GET(p"/buses/getvehicles") => Action {
        Ok(mockBusVehiclesJSON).as(ContentTypes.JSON)
      }
      case GET(p"/trains/ttpositions.aspx") => Action {
        Ok(mockTrainVehiclesJSON).as(ContentTypes.JSON)
      }
      case GET(p"/downloads/sch_data/google_transit.zip") => Action {
        Thread.sleep(waitTime.toMillis)
        Ok.sendFile(gtfsZipFile)(components.executionContext, components.fileMimeTypes)
      }
    }
  }
}
