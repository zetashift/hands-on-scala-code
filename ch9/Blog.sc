import java.text.SimpleDateFormat
import java.nio.file.attribute.FileTime
import $ivy.`com.lihaoyi::scalatags:0.9.1`, scalatags.Text.all._
import $ivy.`com.atlassian.commonmark:commonmark:0.15.2`

@main def main(targetGitRepo: String = ""): Unit = {

  def formatDate(ft: FileTime): String = {
    val df = new SimpleDateFormat("yyyy-MM-dd")
    "Written on " + df.format(ft.toMillis())
  }

  val bootstrapCss = link(
    rel := "stylesheet",
    href := "https://stackpath.bootstrapcdn.com/bootstrap/4.5.0/css/bootstrap.css"
  )

  interp.watch(os.pwd / "post")
  val postInfo = os
    .list(os.pwd / "post")
    .map { p =>
      val datePost = formatDate(os.stat(p).mtime)
      val s"$prefix - $suffix" = p.last
      (prefix, suffix, p, datePost)
    }
    .sortBy(_._1.toInt)

  def mdNameToHtml(name: String) =
    name.replace(" ", "-").toLowerCase() + ".html"

  os.remove.all(os.pwd / "out")
  os.makeDir.all(os.pwd / "out" / "post")
  os.write(
    os.pwd / "out" / "index.html",
    doctype("html")(
      html(
        head(bootstrapCss),
        body(
          h1("Blog"),
          for ((_, suffix, p, datePost) <- postInfo)
            yield div(
              h2(a(href := ("post/" + mdNameToHtml(suffix)), suffix)),
              h4(datePost)
            )
        )
      )
    )
  )

  for ((_, suffix, path, datePost) <- postInfo) {
    val parser = org.commonmark.parser.Parser.builder().build()
    val document = parser.parse(os.read(path))
    val renderer = org.commonmark.renderer.html.HtmlRenderer.builder().build()
    val output = renderer.render(document)
    os.write(
      os.pwd / "out" / "post" / mdNameToHtml(suffix),
      doctype("html")(
        html(
          head(bootstrapCss),
          body(
            h1(a(href := "../index.html")("Blog"), " / ", suffix),
            h4(datePost),
            raw(output)
          )
        )
      )
    )
  }

  if (targetGitRepo != "") {
    os.proc("git", "init").call(cwd = os.pwd / "out")
    os.proc("git", "add", "-A").call(cwd = os.pwd / "out")
    os.proc("git", "commit", "-am", ".").call(cwd = os.pwd / "out")
    os.proc("git", "push", targetGitRepo, "head", "-f")
      .call(cwd = os.pwd / "out")
  }
}
