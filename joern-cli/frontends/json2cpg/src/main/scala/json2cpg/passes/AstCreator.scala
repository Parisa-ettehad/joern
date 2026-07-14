package io.joern.json2cpg

import io.joern.x2cpg.{Ast, AstCreatorBase}
import io.shiftleft.codepropertygraph.generated.nodes.NewNode

class AstCreator(val fileName: String, parser: JsonParser) extends AstCreatorBase(fileName) {
  
  override def createAst(): Ast = {
    Ast()
  }
}