package io.joern.json2cpg.parser

object JsonIntermediateGraph {

  case class GraphNode(
    id: String,
    nodeType: String,
    label: Option[String],
    code: Option[String],
    line: Option[Int]
  )

  case class GraphEdge(
    source: String,
    target: String,
    edgeType: String
  )

  case class GraphRoot(
    fileName: String,
    nodes: Seq[GraphNode],
    edges: Seq[GraphEdge]
  )
}