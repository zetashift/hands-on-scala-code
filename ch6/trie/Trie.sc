class Trie() {
  class Node(
      var hasValue: Boolean,
      val children: collection.mutable.Map[Char, Node] =
        collection.mutable.Map()
  )
  val root = new Node(false)

  def add(s: String): Unit = {
    var current = root
    for (char <- s)
      current = current.children.getOrElseUpdate(char, new Node(false))
    current.hasValue = true
  }

  def contains(s: String): Boolean = {
    var current = Option(root)
    for (char <- s if current.nonEmpty) current = current.get.children.get(char)
    current.exists(_.hasValue)
  }

  def prefixesMatchesString0(s: String): Set[Int] = {
    var current = Option(root)
    val output = Set.newBuilder[Int]
    for ((c, i) <- s.zipWithIndex if current.nonEmpty) {
      if (current.get.hasValue) output += i
      current = current.get.children.get(c)
    }
    if (current.exists(_.hasValue)) output += s.length
    output.result()
  }

  def stringsMatchingPrefix(s: String): Set[String] = {
    var current = Option(root)
    for (char <- s if current.nonEmpty) current = current.get.children.get(char)
    if (current.isEmpty) Set()
    else {
      val output = Set.newBuilder[String]
      def recurse(current: Node, path: List[Char]): Unit = {
        if (current.hasValue) output += (s + path.reverse.mkString)
        for ((char, n) <- current.children) recurse(n, c :: path)
      }
      recurse(current.get, Nil)
      output.result()
    }
  }
}
