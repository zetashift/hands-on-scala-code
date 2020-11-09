import java.text.SimpleDateFormat
import java.nio.file.attribute.FileTime
import $ivy.`com.lihaoyi::scalatags:0.9.2`, scalatags.Text.all._
import $ivy.`com.atlassian.commonmark:commonmark:0.13.1`
import $ivy.`org.apache.pdfbox:pdfbox:2.0.18`,
pdfbox.multipdf.PDFMergerUtility._
import mill._

def formatDate(ft: FileTime): String = {
  val df = new SimpleDateFormat("yyyy-MM-dd")
  "Written on " + df.format(ft.toMillis())
}

def bootstrap =
  T {
    os.write(
      T.dest / "bootstrap.css",
      requests
        .get(
          "https://stackpath.bootstrapcdn.com/bootstrap/4.5.0/css/bootstrap.css"
        )
        .text()
    )
    PathRef(T.dest / "bootstrap.css")
  }

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

def links = T.input { postInfo.map(_._2) }
def dates = T.input { postInfo.map(_._4) }
def previews = T.sequence(postInfo.map(_._1).map(post(_).preview))

def index = // This generates the index html
  T {
    os.write(
      T.dest / "index.html",
      doctype("html")(
        html(
          head(link(rel := "stylesheet", href := "bootstrap.css")),
          body(
            h1("Blog"),
            // I know this can be done a lot cleaner, but for now this suffices
            for (
              (suffix, datePost, preview) <-
                links().zip(dates()).zip(previews()).map {
                  case ((a, b), c) => (a, b, c)
                }
            )
              yield div(
                h2(a(href := ("post/" + mdNameToHtml(suffix)), suffix)),
                raw(preview),
                i(datePost)
              )
          )
        )
      )
    )
    PathRef(T.dest / "index.html")
  }

// This cross module generates the posts html
object post extends Cross[PostModule](postInfo.map(_._1): _*)
class PostModule(number: String) extends Module {
  val Some((_, suffix, markdownPath, datePost)) = postInfo.find(_._1 == number)
  def path = T.source(markdownPath)
  def preview =
    T {
      val parser = org.commonmark.parser.Parser.builder().build()
      val firstPara = os.read.lines(path().path).takeWhile(_.nonEmpty)
      val document = parser.parse(firstPara.mkString("\n"))
      val renderer = org.commonmark.renderer.html.HtmlRenderer.builder().build()
      val output = renderer.render(document)
      output
    }
  def render =
    T {
      val parser = org.commonmark.parser.Parser.builder().build()
      val document = parser.parse(os.read(path().path))
      val renderer = org.commonmark.renderer.html.HtmlRenderer.builder().build()
      val output = renderer.render(document)
      os.write(
        T.dest / mdNameToHtml(suffix),
        doctype("html")(
          html(
            head(link(rel := "stylesheet", href := "../bootstrap.css")),
            body(
              h1(a(href := "../index.html")("Blog"), " / ", suffix),
              h4(datePost),
              raw(output)
            )
          )
        )
      )
      PathRef(T.dest / mdNameToHtml(suffix))
    }
}

// Combine individual posts and index.html into a single target
val posts = T.sequence(postInfo.map(_._1).map(post(_).render))
def dist =
  T {
    for (post <- posts()) {
      os.copy(post.path, T.dest / "post" / post.path.last, createFolders = true)
    }
    os.copy(index().path, T.dest / "index.html")
    os.copy(bootstrap().path, T.dest / "bootstrap.css")
    PathRef(T.dest)
  }

// PDF exercise code
def puppeteerInstall =
  T {
    os.proc("npm", "install", "puppeteer@4.0.1")
      .call(cwd = T.dest, stderr = os.Pipe)
    PathRef(T.dest)
  }

def pdfFiles = T.sequences(postInfo.map(_._1).map(post(_).pdf))
def pdfize = T.source(os.pwd / "pdfize.js")

def pdfs =
  T {
    for (p <- os.list(puppeteer().path)) os.copy.over(p, T.dest, p.last)
    os.copy(bootstrap().path, T.dest / "bootstrap.css")
    os.makeDir(T.dest / "post")
    val htmlPath = T.dest / "post" / render().path.last
    os.copy(render().path, htmlPath)

    val localPdfize = T.dest / pdfize().path.last

    os.copy.over(pdfize().path, localPdfize)
    val s"$baseName.html" = htmlPath.last
    val pdfPath = T.dest / pdfize().path.last
    os.proc("node", localPdfize, htmlPath, pdfPath).call(cwd = T.dest)
    PathRef(pdfPath)

  }

def concatPdf =
  T {
    val outPath = T.dest / "combined.pdf"
    val pdfUtil = new PDFMergerUtility
    for (pdf <- pdfFiles()) pdfUtil.addSource(pdf.path.toIO)
    val out = os.write.outputStream(outPath)
    try {
      pdfUtil.setDestinationStream(out)
      merger.mergeDocuments()
    } finally out.close()
    PathRef(outPath)
  }
