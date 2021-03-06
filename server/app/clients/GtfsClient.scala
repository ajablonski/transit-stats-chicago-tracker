package clients

import com.github.ajablonski.shared.model.{Route, RouteType}
import com.github.tototoshi.csv.CSVReader
import net.lingala.zip4j.ZipFile
import play.api.Configuration
import play.api.libs.ws.WSClient

import java.io.File
import java.nio.file.{Files, Paths}
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GtfsClient @Inject()(ws: WSClient, implicit private val ec: ExecutionContext, config: Configuration) {
  private val baseUrl = config.get[String]("app.cta.gtfsUrl")
  private val gtfsPath = "downloads/sch_data/google_transit.zip"
  private val gtfsDirectory = config
    .getOptional[String]("app.filepath")
    .map(p => Paths.get(p, "gtfs"))
    .getOrElse(Paths.get("tmp", "gtfs"))

  def getRoutes(): Future[List[Route]] = {
    getFeedFile("routes.txt")
      .map { file =>
        CSVReader
          .open(file)
          .allWithHeaders()
          .map { line =>
              Route(routeId = line("route_id"),
                name = line("route_long_name"),
                `type` = RouteType.fromGtfsCode(line("route_type")),
                color = line("route_color"),
                textColor = line("route_text_color"))
          }
      }
  }

  private def getFeedFile(fileName: String): Future[File] = {
    if (gtfsDirectory.resolve(fileName).toFile.exists()) {
      Future.successful(gtfsDirectory.resolve(fileName).toFile)
    } else {
      if (!gtfsDirectory.toFile.exists()) {
        Files.createDirectories(gtfsDirectory)
      }

      ws.url(f"$baseUrl/$gtfsPath")
        .get()
        .map {
          response =>
            val zipFilePath = gtfsDirectory.resolve("gtfs.zip")
            Files.createFile(zipFilePath)
            Files.write(zipFilePath, response.bodyAsBytes.toArray)

            zipFilePath.toFile
        }
        .map { file =>
          new ZipFile(file).extractAll(gtfsDirectory.toString)

          gtfsDirectory.resolve(fileName).toFile
        }
    }
  }
}
