import net.lingala.zip4j.ZipFile
import play.api.BuiltInComponents
import play.api.http.ContentTypes
import play.api.mvc.{Handler, RequestHeader}
import play.api.routing.sird._

import java.io.File
import java.nio.file.Files
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

  def mockCta(components: BuiltInComponents): PartialFunction[RequestHeader, Handler] = {
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
            |}""".stripMargin).as(ContentTypes.JSON)
      }
      case GET(p"/downloads/sch_data/google_transit.zip") => Action {
        Ok.sendFile(gtfsZipFile)(components.executionContext, components.fileMimeTypes)
      }
    }
  }
}
