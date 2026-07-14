package io.joern.json2cpg

import io.joern.x2cpg.{X2CpgConfig, X2CpgFrontend}

case class Config() extends X2CpgConfig[Config] {
  var inputPath: String = ""
  var outputPath: String = "cpg.bin"
}

class Json2Cpg extends X2CpgFrontend[Config] {
  override def createCpg(config: Config): scala.util.Try[io.shiftleft.codepropertygraph.generated.Cpg] = {
    // Implement AST generation logic here
  }
}