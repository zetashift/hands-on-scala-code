// Let's write a program that syncs our 'src' folder to our 'dest' folder
def sync(src: os.Path, dest: os.Path) = {
  val srcPaths = os.walk(src)
  for (srcSubPath <- os.walk(src)) {
    val subPath = srcSubPath.subRelativeTo(src)
    val destSubPath = dest / subPath
    (os.isDir(srcSubPath), os.isDir(destSubPath)) match {
      case (false, true) | (true, false) =>
        os.copy.over(srcSubPath, destSubPath, createFolders = true)
      // Updating files, if src == dest but with different contents
      case (false, false)  
          if !os.exists(destSubPath) || !os.read
            .bytes(srcSubPath)
            .sameElements(os.read.bytes(destSubPath)) =>
        os.copy.over(srcSubPath, destSubPath, createFolders = true)
      case _ => // do nothing
    }
  }
  val srcPathSet = srcPaths.map(_.subRelativeTo(src)).toSet
  for (destPath <- os.walk(dest)) {
    val destSubPath = destPath.subRelativeTo(dest)
    if (!srcPathSet.contains(destSubPath)) os.remove.all(destPath)
  }
}
