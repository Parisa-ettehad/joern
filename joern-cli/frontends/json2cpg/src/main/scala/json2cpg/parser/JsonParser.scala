package io.joern.json2cpg.parser

import io.joern.json2cpg.parser.JsonIntermediateGraph.*
import ujson.*

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}
import scala.util.Try


class JsonParser {

  def parseFile(jsonPath: Path): Try[GraphRoot] =
    Try {
      val content = Files.readString(jsonPath, StandardCharsets.UTF_8)
      val json    = ujson.read(content)
      parseGraph(json)
    }

  private def parseGraph(json: Value): GraphRoot = {
    val fileName =
      json.obj
        .get("file")
        .map(_.str)
        .getOrElse("unknown")

    val nodes =
      json.obj
        .get("nodes")
        .map(_.arr.toSeq.map(parseNode))
        .getOrElse(Seq.empty)

    val edges =
      json.obj
        .get("edges")
        .map(_.arr.toSeq.map(parseEdge))
        .getOrElse(Seq.empty)

    GraphRoot(
      fileName = fileName,
      nodes = nodes,
      edges = edges
    )
  }

  private def parseNode(json: Value): GraphNode = {
    GraphNode(
      id = json("id").str,
      nodeType = json("type").str,
      label = json.obj.get("label").map(_.str),
      code = json.obj.get("code").map(_.str),
      line = json.obj.get("line").map(_.num.toInt)
    )
  }

  private def parseEdge(json: Value): GraphEdge = {
    GraphEdge(
      source = json("source").str,
      target = json("target").str,
      edgeType = json("edgeType").str
    )
  }
}

object JsonParser {
  def apply(): JsonParser = new JsonParser()
}