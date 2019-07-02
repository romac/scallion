// In sbt, run with:
//
//   benchmarks/jmh:run -prof jmh.extras.JFR -i 10 -wi 10 -f1 -t1 JSONBench
//

import java.util.concurrent.TimeUnit
import org.openjdk.jmh.annotations._

import scallion.parsing._
import example.json._

trait JSONData {
  def colleagues(size: Int): String = {
    for (i ← 1 to size) yield {
      s"""{"name": "person-$i", "age": $i}"""
    }
  }.mkString(",\n    ")

  def json(size: Int): String = {
    s"""|{
        |  "colleagues": [
        |    ${colleagues(size)}
        |  ]
        |}
    """.stripMargin
  }
}

@State(Scope.Thread)
@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
class JSONBench extends JSONData {

  lazy val smallJSON = io.Source.fromString(json(10))
  lazy val bigJSON   = io.Source.fromString(json(10000))

  @Benchmark
  def parseSmallJSON(): Value = {
    JSONParser(JSONLexer(smallJSON)) match {
      case JSONParser.Parsed(value, _) => value
      case err => throw new Exception(s"Got error: $err")
    }
  }

  @Benchmark
  def parseBigJSON(): Value = {
    JSONParser(JSONLexer(bigJSON)) match {
      case JSONParser.Parsed(value, _) => value
      case err => throw new Exception(s"Got error: $err")
    }
  }

}

