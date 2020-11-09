class Foo(val i: Int, val s: String)
implicit val fooRw = upickle.default
  .readWriter[ujson.Value]
  .bimap[Thing](
    foo => ujson.Obj("i" -> foo.i, "s" -> foo.s),
    value => new Foo(value("i").num.toInt, value("s").str)
  )
