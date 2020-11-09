import $ivy.`org.jsoup:jsoup:1.13.1`, org.jsoup._
import collection.JavaConverters._

val doc = Jsoup.connect("https://developer.mozilla.org/en-US/docs/Web/API").get()

val annotationsList = {
  for (span <- doc.select("span.indexListRow").asScala)
  yield (
    span.text,
    span.select("span.indexListBadges .icon-only-inline").asScala.map(_.attr("title"))
  )
}

val annotationsMap = annotationsList.toMap
os.write(os.pwd / "annotations.json", upickle.default.write(annotationsMap))