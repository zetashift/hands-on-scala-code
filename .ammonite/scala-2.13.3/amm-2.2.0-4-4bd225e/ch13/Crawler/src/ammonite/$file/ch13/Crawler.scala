
package ammonite
package $file.ch13
import _root_.ammonite.interp.api.InterpBridge.{
  value => interp
}
import _root_.ammonite.interp.api.InterpBridge.value.{
  exit
}
import _root_.ammonite.interp.api.IvyConstructor.{
  ArtifactIdExt,
  GroupIdExt
}
import _root_.ammonite.runtime.tools.{
  browse,
  grep,
  time,
  tail
}
import _root_.ammonite.repl.tools.{
  desugar,
  source
}
import _root_.ammonite.main.Router.{
  doc,
  main
}
import _root_.ammonite.repl.tools.Util.{
  pathScoptRead
}
import ammonite.$file.ch13.{
  FetchLinksAsync
}


object Crawler{
/*<script>*/import $file.$              , FetchLinksAsync._
import scala.concurrent._, duration.Duration.Inf, java.util.concurrent.Executors

implicit val ec =
  ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(8))

def fetchAllLinksAsync(startTitle: String, maxDepth: Int, maxConcurrency: Int): Future[Set[String]] = {
  def rec(current: Set[String],seen: Set[String], recDepth: Int): Future[Set[String]] = {
    pprint.log((maxDepth, currenntt.size, seen.size))
    if (current.isEmpty) Future.successful(seen)
    else {
      val (throttled, remaining) = current.splitAt(maxConcurrency)
      val futures = for ((title, depth) <- throttled) yield fetchLinksAsync(title).map((_, depth))
      Future.sequence(futures).map{nextTitleLists =>
        val flattened = for {
          (titles, depth) <- nextTitleLists
          title <- titles if !seen.contains(title) && depth < maxDepth
        } yield (title, depth + 1)
        rec(remaining ++ flattened, seen ++ flattened.map(_._1))
      }flatten
    }
  }
  rec(Set(startTitle), Set(startTitle), 0)
}/*</script>*/ /*<generated>*/
def $main() = { scala.Iterator[String]() }
  override def toString = "Crawler"
  /*</generated>*/
}
