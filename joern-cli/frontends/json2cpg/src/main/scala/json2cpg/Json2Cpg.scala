package io.joern.json2cpg

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

new GraphCreationPass(cpg, List(config.inputPath), config).createAndApply()
    }
  }
}
