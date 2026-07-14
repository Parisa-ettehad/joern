package io.joern.json2cpg2cpg

import io.joern.x2cpg.{Ast, AstCreatorBase}
import io.shiftleft.codepropertygraph.generated.nodes.NewNode

class AstCreator(val fileName: String, parser: JsonParser) extends AstCreatorBase(fileName) {
  
  override def createAst(): Ast = {
    // Map your custom AST nodes to CPG nodes here
    Ast()
  }
}