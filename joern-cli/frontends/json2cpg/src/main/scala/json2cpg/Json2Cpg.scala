package io.joern.json2cpg

import io.joern.x2cpg.{X2Cpg, X2CpgFrontend}
import io.joern.x2cpg.passes.frontend.{MetaDataPass, TypeNodePass}
import io.shiftleft.codepropertygraph.generated.{Cpg, Languages}

import scala.util.Try

class Json2Cpg extends X2CpgFrontend {
  override type ConfigType = Config
  override val defaultConfig: Config = Config()

  override def createCpg(config: Config): Try[Cpg] = {
    X2Cpg.withNewEmptyCpg(config.outputPath, config) { (cpg, _) =>

    MetaDataPass(cpg, "WASM", config.inputPath).createAndApply()

    new JsonCreationPass(cpg, config.inputPath).createAndApply()

        new ContainsEdgePass(cpg).createAndApply()
        TypeNodePass.withTypesFromCpg(cpg).createAndApply()
        new RefEdgePass(cpg).createAndApply()
        new AbapTypeInferencePass(cpg).createAndApply()
        new TypeEvalPass(cpg).createAndApply()
      }
    }
  }

  private val logger = org.slf4j.LoggerFactory.getLogger(classOf[Abap2Cpg])

