package io.joern.json2cpg

import io.joern.json2cpg.parser.JsonIntermediateGraph.*
import io.shiftleft.codepropertygraph.generated.nodes.*
import overflowdb.BatchedUpdate.DiffGraphBuilder

import scala.collection.mutable

class GraphCreator(graph: GraphRoot, diffGraph: DiffGraphBuilder) {

  private val nodeByJsonId =
    mutable.HashMap.empty[String, NewNode]

  def create(): Unit = {
    createNodes()
    createEdges()
  }

  private def createNodes(): Unit = {
    graph.nodes.foreach { jsonNode =>
      val cpgNode = createNode(jsonNode)

      nodeByJsonId.put(jsonNode.id, cpgNode)
      diffGraph.addNode(cpgNode)
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
        case "CFG" =>
          diffGraph.addEdge(source, target, EdgeTypes.CFG)

        case "CDG" | "CONTROL_DEPENDENCE" =>
          // نیازمند بررسی schema و edge type پشتیبانی‌شده در نسخه Joern تو
          diffGraph.addEdge(source, target, "CDG")

        case "DDG" | "DATA_DEPENDENCE" =>
          diffGraph.addEdge(source, target, EdgeTypes.REACHING_DEF)

        case other =>
          throw new IllegalArgumentException(
            s"Unsupported edge type: $other"
          )
      }
    }
  }
}