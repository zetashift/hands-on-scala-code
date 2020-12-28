package app
import scalatags.Text.all._

// This uses synchronized!

object MinimalApplication extends cask.MainRoutes {
  var messages = Vector(("alice", "Hello, World!"), ("bob", "I'm a cow!! MOO!"))
  var openConnections = Map.empty[cask.WsChannelActor, String]
  val bootstrap =
    "https://stackpath.bootstrapcdn.com/bootstrap/4.5.0/css/bootstrap.css"

  @cask.staticResources("/static")
  def staticResourceRoutes() = "static"

  @cask.get("/")
  def hello(filter: String = "") = doctype("html")(
    html(
      head(
        link(rel := "stylesheet", href := bootstrap),
        script(src := "/static/app.js"),
        // Pass it as a base64 encoded string for XSS
        script(raw(s"""subscribeSocket(atob("${java.util.Base64.getEncoder
          .encodeToString(filter.getBytes)}"))"""))
      ),
      body(
        div(cls := "container")(
          h1("Scala Chat!"),
          div(id := "messageList")(messageList(filter)),
          div(id := "errorDiv", color.red),
          form(onsubmit := "submitForm(); return false")(
            input(
              `type` := "text",
              id := "nameInput",
              placeholder := "Username"
            ),
            input(
              `type` := "text",
              id := "msgInput",
              placeholder := "Write a message."
            ),
            input(`type` := "submit")
          ),
          form(action := "/", method := "get")(
            input(
              `type` := "text",
              name := "filter",
              placeholder := "Filter messages from user",
              value := filter
            ),
            input(`type` := "submit")
          )
        )
      )
    )
  )

  def messageList(filter: String) = frag(
    for (
      (name, msg) <- synchronized(messages) if filter == "" || name == filter
    )
      yield p(b(name), " ", msg)
  )

  @cask.websocket("/subscribe")
  def subscribe() = cask.WsHandler { connection =>
    {
      println("Subscribed")
      cask.WsActor {
        case cask.Ws.Text(filter) =>
          synchronized { openConnections += (connection -> filter) }
          connection.send(cask.Ws.Text(messageList(filter).render))
        case cask.Ws.Close(_, _) =>
          synchronized { openConnections -= connection }

      }
    }
  }

  @cask.postJson("/")
  def postChatMsg(name: String, msg: String) = {
    if (name == "")
      ujson.Obj("success" -> false, "err" -> "Name cannot be empty")
    else if (msg == "")
      ujson.Obj("success" -> false, "err" -> "Message cannot be empty")
    else
      synchronized {
        synchronized { messages = messages :+ (name -> msg) }
        for ((conn, filter) <- synchronized(openConnections))
          conn.send(cask.Ws.Text(messageList(filter).render))
        ujson.Obj("success" -> true, "err" -> "")
      }

  }
  initialize()
}

