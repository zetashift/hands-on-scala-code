def traverse(v: ujson.Value): ujson.Value = traverseRec(v).get
def traverseRec(v: ujson.Value): Option[ujson.Value] = v match {
  case a: ujson.Arr => Some(ujson.Arr.from(a.arr.flatMap(traverseRec)))
  case o: ujson.Obj => Some(ujson.Obj.from(o.obj.flatMap{ case (k, v) => traverseRec(v).map(k -> _) }))
  case s: ujson.Str => if (!s.str.startsWith("https://")) Some(s) else None
  case _ => Some(v)
}