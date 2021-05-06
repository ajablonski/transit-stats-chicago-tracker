package clients

import com.github.ajablonski.shared.model.{Point, Route, RouteType, Shape}
import com.github.tototoshi.csv.CSVReader
import net.lingala.zip4j.ZipFile
import play.api.Configuration
import play.api.cache.SyncCacheApi
import play.api.libs.ws.WSClient

import java.io.{File, FileNotFoundException}
import java.nio.file.{Files, Paths}
import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future}

@Singleton
class GtfsClient @Inject()(ws: WSClient,
                           implicit private val ec: ExecutionContext,
                           config: Configuration,
                           cache: SyncCacheApi) {
  private val baseUrl = config.get[String]("app.cta.gtfsUrl")
  private val gtfsPath = "downloads/sch_data/google_transit.zip"
  private val gtfsDirectory = config
    .getOptional[String]("app.filepath")
    .map(p => Paths.get(p, "gtfs"))
    .getOrElse(Paths.get("tmp", "gtfs"))
    .toAbsolutePath
  private var hasUnzipped = false
  Await.ready(fetchGtfsFile(), 1.minute)

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

  def getShapesForRoute(routeId: String): Future[List[Shape]] = {
    cache.getOrElseUpdate(routeId) {
      for (tripsFile <- getFeedFile("trips.txt");
           shapesFile <- getFeedFile("shapes.txt")) yield {
        val shapeIds = CSVReader.open(tripsFile)
          .toStreamWithHeaders
          .filter { line => line("route_id") == routeId }
          .map { line => line("shape_id") }
          .toSet
        CSVReader.open(shapesFile)
          .toStreamWithHeaders
          .filter { line => shapeIds.contains(line("shape_id")) }
          .groupBy { line => line("shape_id") }
          .map { case (shapeId, pointStream) =>
            val points = pointStream
              .map { line =>
                Point(line("shape_pt_lat").toDouble, line("shape_pt_lon").toDouble, line("shape_dist_traveled").toLong)
              }
              .toList
            Shape(shapeId, points)
          }
          .toList
      }
    }
  }

  private def getFeedFile(fileName: String): Future[File] = {
    val feedFile = gtfsDirectory.resolve(fileName).toFile

    if (feedFile.exists()) {
      Future.successful(feedFile)
    } else {
      Future.failed[File](new FileNotFoundException(s"$feedFile not found"))
    }
  }

  private def fetchGtfsFile(): Future[File] = {
    val zipFilePath = gtfsDirectory.resolve("gtfs.zip")

    if (!gtfsDirectory.toFile.exists()) {
      Files.createDirectories(gtfsDirectory)
    }

    ws.url(f"$baseUrl/$gtfsPath")
      .get()
      .map {
        response =>
          synchronized {
            if (!zipFilePath.toFile.exists()) {
              Files.createFile(zipFilePath)
              Files.write(zipFilePath, response.bodyAsBytes.toArray)
            }
          }

          zipFilePath.toFile
      }
      .map { file =>
        synchronized {
          if (!hasUnzipped) {
            new ZipFile(file).extractAll(gtfsDirectory.toString)
            hasUnzipped = true
          }
        }

        file
      }
  }
}
