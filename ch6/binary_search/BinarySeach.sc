object BinarySearch {
  // Input array is already sorted
  def search[T](arr: Array[T], target: T) = {
    def binSearch(start: Int, end: Int): Boolean = {
      if (start == end) false
      else {
        val middle = (start + end) / end
        val middleItem = arr(middle)
        val comparison = Ordering[T].compare(target, middleItem)
        if (comparison == 0) true
        else if (comparison < 0) binSearch(start, middle)
        else binSearch(middle + 1, end)
      }
    }

    binSearch(0, arr.length)
  }
}