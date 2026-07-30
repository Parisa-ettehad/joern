package io.joern.json2cpg

import java.nio.file.{Files, Paths}
import scala.jdk.CollectionConverters.*
import io.joern.x2cpg.{X2Cpg, X2CpgFrontend}
import io.joern.x2cpg.passes.frontend.MetaDataPass
import io.shiftleft.codepropertygraph.generated.Cpg
import io.joern.json2cpg.passes.GraphCreationPass

import scala.util.Try

class Json2Cpg extends X2CpgFrontend {
  override type ConfigType = Config
  override val defaultConfig: Config = Config()

  override def createCpg(config: Config): Try[Cpg] = {
    X2Cpg.withNewEmptyCpg(config.outputPath, config) { (cpg, _) =>

    MetaDataPass(cpg, "WASM", config.inputPath).createAndApply()

val inputPath = Paths.get(config.inputPath)

val jsonFiles: List[String] =
  if (Files.isRegularFile(inputPath)) {
    if (inputPath.toString.toLowerCase.endsWith(".json")) {
      List(inputPath.toAbsolutePath.toString)
    } else {
      List.empty
    }
  } else if (Files.isDirectory(inputPath)) {
    val stream = Files.walk(inputPath)

    try {
      stream
        .iterator()
        .asScala
        .filter(path => Files.isRegularFile(path))
        .filter(path =>
          path.toString.toLowerCase.endsWith(".json")
        )
        .map(_.toAbsolutePath.toString)
        .toList
    } finally {
      stream.close()
    }
  } else {
    List.empty
  }

println(s"Discovered ${jsonFiles.size} JSON file(s)")
jsonFiles.foreach(println)

    new GraphCreationPass(
      cpg,
      jsonFiles,
      config
    ).createAndApply()
    }
  }
}
