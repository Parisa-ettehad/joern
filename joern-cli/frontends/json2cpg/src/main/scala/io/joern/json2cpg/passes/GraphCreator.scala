package io.joern.json2cpg.passes

import io.joern.json2cpg.parser.JsonIntermediateGraph.*
import io.shiftleft.codepropertygraph.generated.nodes.*
import io.shiftleft.codepropertygraph.generated.{DiffGraphBuilder, EdgeTypes}
import scala.collection.mutable

class GraphCreator(
  graph: GraphRoot, 
  diffGraph: DiffGraphBuilder
  ) {

  private val nodeByJsonId =
    mutable.HashMap.empty[String, NewNode]

  def create(): Unit = {
    createNodes()
    createEdges()
  }

  private def createNodes(): Unit = {
    graph.nodes
      .filterNot(n =>
        n.nodeType.equalsIgnoreCase("META_DATA") ||
        n.nodeType.equalsIgnoreCase("FILE")
      )
      .foreach { jsonNode =>
        val cpgNode = createNode(jsonNode)

        nodeByJsonId.put(jsonNode.id, cpgNode)
        diffGraph.addNode(cpgNode)
        cpgNode match {
          case method: NewMethod =>
            val hasMethodReturn = graph.edges.exists { edge =>
              edge.edgeType.equalsIgnoreCase("AST") &&
              edge.source == jsonNode.id &&
              graph.nodes
                .find(_.id == edge.target)
                .exists(_.nodeType.equalsIgnoreCase("METHOD_RETURN"))
            }

            if (!hasMethodReturn) {
              val methodReturn =
                NewMethodReturn()
                  .code("RET")
                  .typeFullName("ANY")
                  .order(1)
                  .lineNumber(jsonNode.line)

              diffGraph.addNode(methodReturn)
              diffGraph.addEdge(method, methodReturn, EdgeTypes.AST)
            }

          case _ =>
        }

      }
  }

  private def createNode(node: GraphNode): NewNode = {
    node.nodeType.toUpperCase match {
      case "METHOD" =>
        NewMethod()
          .name(node.label.getOrElse("unknown"))
          .fullName(node.label.getOrElse("unknown"))
          .code(node.code.getOrElse(""))
          .filename(graph.fileName)
          .lineNumber(node.line)

      case "CALL" =>
        NewCall()
          .name(node.label.getOrElse("unknown"))
          .methodFullName(node.label.getOrElse("unknown"))
          .code(node.code.getOrElse(""))
          .lineNumber(node.line)

      case "BLOCK" =>
        NewBlock()
          .code(node.code.getOrElse(""))
          .lineNumber(node.line)

      case "CONTROL_STRUCTURE" =>
        NewControlStructure()
          .controlStructureType(node.label.getOrElse("UNKNOWN"))
          .code(node.code.getOrElse(""))
          .lineNumber(node.line)

      case "IDENTIFIER" =>
        NewIdentifier()
          .name(node.label.getOrElse(""))
          .code(node.code.getOrElse(""))
          .typeFullName("ANY")
          .lineNumber(node.line)

      case "LITERAL" =>
        NewLiteral()
          .code(node.code.getOrElse(""))
          .typeFullName("ANY")
          .lineNumber(node.line)

      case "METHOD_PARAMETER_IN" =>
        NewMethodParameterIn()
          .name(node.label.getOrElse("unknown"))
          .code(node.code.getOrElse(""))
          .typeFullName(node.typeFullName.getOrElse("ANY"))
          .index(node.index.getOrElse(0))
          .order(node.order.getOrElse(0))
          .lineNumber(node.line)

      case "METHOD_RETURN" =>
        NewMethodReturn()
          .code(node.code.getOrElse("RET"))
          .typeFullName(node.typeFullName.getOrElse("ANY"))
          .order(node.order.getOrElse(0))
          .lineNumber(node.line)

      case other =>
        throw new IllegalArgumentException(
          s"Unsupported JSON node type: $other"
        )
    }
  }

  private def createEdges(): Unit = {
    graph.edges.foreach { edge =>
      val source = nodeByJsonId.getOrElse(
        edge.source,
        throw new IllegalArgumentException(
          s"Unknown source node: ${edge.source}"
        )
      )

      val target = nodeByJsonId.getOrElse(
        edge.target,
        throw new IllegalArgumentException(
          s"Unknown target node: ${edge.target}"
        )
      )

      edge.edgeType.toUpperCase match {
        case "AST" =>
          diffGraph.addEdge(source, target, EdgeTypes.AST)

        case "CFG" =>
          diffGraph.addEdge(source, target, EdgeTypes.CFG)

        case "CDG" =>
          diffGraph.addEdge(source, target, EdgeTypes.CDG)

        case "DDG" | "REACHING_DEF" =>
          diffGraph.addEdge(source, target, EdgeTypes.REACHING_DEF)

        case other =>
            println(s"Unknown edge type: $other")
      }
    }
  }
}