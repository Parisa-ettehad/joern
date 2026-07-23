package io.joern.json2cpg.passes

import io.joern.json2cpg.Config
import io.joern.json2cpg.GraphCreator
import io.joern.json2cpg.parser.JsonParser
import io.joern.x2cpg.ValidationMode
import io.shiftleft.codepropertygraph.generated.Cpg
import io.shiftleft.passes.ForkJoinParallelCpgPass
import org.slf4j.LoggerFactory

import java.nio.file.Paths
import scala.util.{Failure, Success}

class GraphCreationPass(
  cpg: Cpg,
  jsonFiles: List[String],
  config: Config
) extends ForkJoinParallelCpgPass[String](cpg) {

  private val logger = LoggerFactory.getLogger(classOf[GraphCreationPass])
  private val parser = JsonParser()

  override def generateParts(): Array[String] =
    jsonFiles.toArray

  override def runOnPart(
    diffGraph: DiffGraphBuilder,
    jsonFile: String
  ): Unit = {

    implicit val schemaValidation: ValidationMode =
      ValidationMode.Enabled

    parser.parseFile(Paths.get(jsonFile)) match {

      case Success(graph) =>
        logger.debug(s"Generating CPG for: ${graph.fileName}")

      val graphCreator =
        new GraphCreator(graph, diffGraph)

      graphCreator.create()

      case Failure(exception) =>
        logger.warn(
          s"Failed to parse '$jsonFile': ${exception.getMessage}"
        )
    }
  }
}