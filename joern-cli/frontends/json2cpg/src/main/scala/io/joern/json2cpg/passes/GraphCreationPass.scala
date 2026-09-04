package io.joern.json2cpg.passes

import org.slf4j.LoggerFactory
import io.joern.json2cpg.Config
import io.joern.json2cpg.parser.JsonParser
import io.joern.x2cpg.ValidationMode
import io.joern.json2cpg.parser.JsonIntermediateGraph
import io.shiftleft.codepropertygraph.generated.Cpg
import io.shiftleft.passes.ForkJoinParallelCpgPass
import io.shiftleft.codepropertygraph.generated.DiffGraphBuilder


import java.nio.file.{Files, Paths}
import scala.util.{Failure, Success}

class GraphCreationPass(
  cpg: Cpg,
  jsonFiles: List[String],
  config: Config
) extends ForkJoinParallelCpgPass[String](cpg) {

  private val logger = LoggerFactory.getLogger(getClass)
  private val parser = JsonParser()

  override def generateParts(): Array[String] = {
  logger.info(s"Number of JSON files: ${jsonFiles.size}")
  jsonFiles.foreach(file =>
    logger.info(s"JSON input file: $file")
  )

  jsonFiles.toArray
}

  override def runOnPart(
    diffGraph: DiffGraphBuilder,
    jsonFile: String
  ): Unit = {

    logger.info(s"Processing JSON file: $jsonFile")

    implicit val schemaValidation: ValidationMode =
      ValidationMode.Enabled

    parser.parseFile(Paths.get(jsonFile)) match {

      case Success(graph) =>
        logger.info(
          s"Successfully parsed: ${graph.fileName}"
        )

        val graphCreator =
          new GraphCreator(graph, diffGraph)

        graphCreator.create()

      case Failure(exception) =>
        logger.warn(
          s"Failed to parse '$jsonFile': ${exception.getMessage}",
          exception
        )
    }
  }
}