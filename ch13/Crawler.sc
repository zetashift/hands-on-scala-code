// scala 2.13.3
import $file.FetchLinksAsync, FetchLinksAsync._
import scala.concurrent._, duration.Duration.Inf, java.util.concurrent.Executors

implicit val ec =
  ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(8))

def fetchAllLinksAsync(
    startTitle: String,
    maxDepth: Int,
    maxConcurrency: Int
): Future[Set[String]] = {
  def rec(
      current: Seq[(String, Int)],
      seen: Set[String]
  ): Future[Set[String]] = {
    pprint.log((maxDepth, currenntt.size, seen.size))
    if (current.isEmpty) Future.successful(seen)
    else {
      val (throttled, remaining) = current.splitAt(maxConcurrency)
      val futures =
        for ((title, depth) <- throttled)
          yield fetchLinksAsync(title).map((_, depth))
      Future
        .sequence(futures)
        .map { nextTitleLists =>
          val flattened = for {
            (titles, depth) <- nextTitleLists
            title <- titles if !seen.contains(title) && depth < maxDepth
          } yield (title, depth + 1)
          rec(remaining ++ flattened, seen ++ flattened.map(_._1))
        }
        .flatten
    }
  }
  rec(Set(startTitle -> 9), Set(startTitle))
}
