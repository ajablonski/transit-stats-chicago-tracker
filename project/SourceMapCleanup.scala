import org.scalajs.linker.interface.Report
import play.api.libs.json.{Json, OFormat}
import sbt.Attributed

import java.io.File
import java.nio.file.{Files, StandardOpenOption}

object SourceMapCleanup {
  implicit val sourceMapFileReads: OFormat[SourceMapFile] = Json.format[SourceMapFile]

  def cleanup(baseDirectory: File, inputFile: Attributed[Report]): Unit = {
    inputFile.data.publicModules.flatMap { module =>
      module.sourceMapName
    }.foreach { sourceMapFilename =>
      val sourceMapFile = new File(baseDirectory, sourceMapFilename)
      println(f"Reading source map at $sourceMapFile")
      val sourceMap = Json.parse(Files.readString(sourceMapFile.toPath))
        .as[SourceMapFile]
      val newSourceMap = sourceMap.copy(sources = sourceMap.sources.map { source =>
        source
          .replaceAll(
            raw"(?:\.\./)+home/travis/build/playframework/",
            "https://raw.githubusercontent.com/playframework/play-json/2.9.2/")
          .replaceAll(
            raw"(?:\.\./)+localhome/doeraene/projects/reflect/",
            "https://raw.githubusercontent.com/portable-scala/portable-scala-reflect/v1.1.0/")
          .replaceAll(
            raw"(?:\.\./)+home/runner/work/sbt-locales/sbt-locales/",
            "https://raw.githubusercontent.com/cquiroz/sbt-locales/v2.2.0/")
      })
      Files.writeString(sourceMapFile.toPath, Json.prettyPrint(Json.toJson(newSourceMap)), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
      println("Replaced known entries incorrectly mapped to local path")
    }
  }
}

case class SourceMapFile(version: Long,
                         file: String,
                         mappings: String,
                         sources: List[String],
                         names: List[String],
                         lineCount: Long)