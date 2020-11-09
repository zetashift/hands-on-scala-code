
*"Micro-optimization"* is normally used to describe low-level optimizations that
do not change the overall structure of the program; this is as opposed to 
"high level" optimizations (e.g. choosing efficient algorithms, caching things, 
or parallelizing things) that often require broader changes to your code. 
Things like removing intermediate objects to minimize memory allocations, or 
using bit-sets rather than `HashSet`s to speed up lookups, are examples of 
micro-optimizations.

Micro-optimization has a bad reputation, and is especially uncommon in the 
Scala programming language where the community is more interested in other 
things such as proofs, fancy usage of static types, or distributed systems. 
Often, it is viewed as a maintainability cost with few benefits. This post will 
demonstrate the potential benefit of micro-optimizations, and how it can be 
a valuable technique to have in your toolbox of programming techniques.

-------------------------------------------------------------------------------

This post will use the [Fansi library](https://github.com/lihaoyi/fansi) as a 
case-study for what benefits you get from micro-optimizing Scala: swapping out 
elegant collection transformations for raw while-loops over mutable Arrays, 
elegant `case class`s for bit-packed integers. While not changing the 
asymptotic performance at all, we will show an order-of-magnitude improvement 
in performance and memory-footprint, and demonstrate the place that these 
techniques have in a Scala codebase.

- [Methodology](#methodology)
- [The Use Case: Fansi](#the-use-case-fansi)
- [Micro-optimized: How Fansi Works](#micro-optimized-how-fansi-works)
    - [fansi.Str](#fansistr)   
    - [fansi.Attrs](#fansiattrs)   
    - [fansi.Str.State](#fansistrstate)   
- [Micro De-optimization](#micro-de-optimization) 
    - [Baseline (Optimized)](#baseline-optimized)
    - [arraycopy and copyOfRange](#arraycopy-and-copyofrange)
    - [Local Array Caching](#local-array-caching)
    - [Speed through While Loops](#speed-through-while-loops)
    - [Tries instead of String-Maps](#tries-instead-of-string-maps)
    - [Arrays instead of Int-Maps](#arrays-instead-of-int-maps)
    - [Bit Packing](#bit-packing)
- [Conclusion](#conclusion) 
    - [Is it Worth It?](#is-it-worth-it) 
    - [What is "Fast Enough"?](#what-is-fast-enough) 
    - [How often is it modified?](#how-often-is-it-modified) 
    - [What are the alternatives?](#what-are-the-alternatives) 


## Methodology

In order to provide a realistic setting for this post, I'm going to use the 
[Fansi library](https://github.com/lihaoyi/fansi) as an example. This is a new 
library that was extracted from the codebase of the 
[Ammonite-REPL](http://www.lihaoyi.com/Ammonite/), and has been in use (in some 
form) by thousands of people to provide syntax highlighting to their Scala REPL 
code. 

Typically, when you are micro-optimizing a library like Fansi, you spend time 
with a profiler and see what takes up the most time. For example, we may use 
[JProfiler](https://www.ej-technologies.com/products/jprofiler/overview.html) 
and pull up a profile that looks like this:

![UnoptimizedPerf.png](optimizing/UnoptimizedPerf.png)

This is the profile for the un-optimized version of Fansi. From this profile, 
we can see where the time is going when we run our code:

- The top time-waste (48%) is some kind of `foreach` call inside our `emitDiff` 
  method
- Next (21%) is a `HashMap#get` call inside `State.equals`
- Next (8%) is initializing iterators, again inside `State.equals`

And from there, you figure out ways to make the code run faster. For example,
if all our time is spent inside `foreach`, we may replace that `.foreach`
with a while-loop that runs much faster in Scala. If our time is all spent
in a `HashMap#get`, we may see if we can replace the `HashMap` with an 
`Array` and give our items indexes, which would let us look things up much
more quickly. As we make progress, the profile changes, and hopefully the
code gets faster each time. 

In the case of Fansi, after optimization the above profile turns into:

![OptimizedPerf.png](optimizing/OptimizedPerf.png)

At which point, all our time is being spent inside this `render` method, and
not in any other helpers or auxiliary code. There are two possibilities:

- `render` is a huge waste of time and should be eliminated, leaving more time
  to run the "real work"
- `render` **is** the real work, and cannot be avoided.

In this case, it is the latter, and we are done micro-optimizing `render`.

The Fansi library has already been optimized, and thus I have already gone
through this process, identified the various bottlenecks, and optimized them
one by one. Thus, this post will take the opposite tack: 

- We will start off with a tour of the *already-optimized* Fansi library

- Discuss the internals and highlight the micro-optimizations that are meant 
  to make Fansi fast
  
- Then roll back the optimizations one by one in order to see what kind of 
  performance impact they had. 
  
By the end, you should have a good sense of what these micro-optimization 
techniques are, what benefit they provide, and where they could possibly be 
used in your own code. 

## The Use Case: Fansi

As a real-world use case to demonstrate these techniques, I am going to use the 
[Fansi](https://github.com/lihaoyi/fansi) library. This is a tiny library that 
I wrote to make it easier to deal with color-coded Ansi strings:
 
![FansiIntro.png](optimizing/FansiIntro.png)

This library exists because dealing with raw `java.lang.String`s with [Ansi
escape codes](https://en.wikipedia.org/wiki/ANSI_escape_code) inside is 
troublesome, slow and error-prone. 

For example, if you
want to take an Ansi-colored `java.lang.String` and find out how many printable
characters are in it, the most common way is to use a regex to remove all the
Ansi escapes e.g. red being `\u001b[31m`, underlined `\u001b[4m`, and remove
all of them before being counting the length. This is slow to run, and error
prone: if you forget to remove them, you end up with subtle bugs where you're
treating a string as if it is 27 characters long on-screen but it's actually 
only 22 characters long since 5 characters are an Ansi color-code that takes
up no space.

Similarly, combining colored strings is error-prone: you can easily mess up existing colors when splicing
strings together.

![AnsiMistake.png](optimizing/AnsiMistake.png)

In this case, the mistake was that we used `Console.RESET` at the end of the 
snippet we're splicing, without considering the fact that the larger-string may
already have a color that we need to re-enable after inserting our snippet. As
it turns out, the only way we could know this is to parse the whole 
outer-string and figure out what the color at the splice-point is: something 
that is both tedious and slow.

The goal of Fansi is to make such mistakes impossible, and to have such simple 
operations behave as you'd expect with regard to colors: 

![FansiNoMistake.png](optimizing/FansiNoMistake.png)

The [Fansi documentation](https://github.com/lihaoyi/fansi) has a lot more to
say about why Fansi exists, but this should have given you a flavor of the 
problem it's trying to solve

## Micro-optimized: How Fansi Works

At its core, Fansi is currently built on three data-structures: 

- [fansi.Str](#fansistr)
- [fansi.Attrs](#fansiattrs)
- [fansi.Str.State](#fansistrstate)

If you want to follow along with the version of the code used for this post,
take a look at the source on github:

- [Fansi 0.1.1](https://github.com/lihaoyi/fansi/tree/35b48525af8894484d2bc2f9403fbb3994774749)

### fansi.Str

```scala
case class Str(chars: Array[Char], colors: Array[Str.State]) {
  require(chars.length == colors.length)

  def ++(other: Str): Str
  def splitAt(index: Int): (Str, Str)
  def substring(start: Int = 0, end: Int = length): String
  def length: Int
  def overlay(attrs: Attrs, start: Int, end: Int): Str
  def plainText: java.lang.String
  def render: java.lang.String
}
```

- [Github Source](https://github.com/lihaoyi/fansi/blob/35b48525af8894484d2bc2f9403fbb3994774749/fansi/shared/src/main/scala/fansi/Fansi.scala#L23-L176)

This is the main representation of a colored string. It stores its characters
and their colors in two parallel `Array`s. The `colors` array stores 
`Str.State`s, which is really just a type-alias for `Int`. All the color 
information for each character (along with other decorations like underline,
bold, reverse, ...) are all stored bit-packed into those `Int`s. The `.render`
method serializes this into a single `java.lang.String` with Ansi escape-codes
embedded.

Like `java.lang.String`, this `Str` is immutable and the two arrays (along with
the constructor) are `private` and can only be accessed through getters with
give you a defensive copy of the innards (not shown in the signature above).

The benefit of this data-structure is that doing operations on the `Str` is 
really fast and easy: 

- `++` concatenating two `Str`s is just concatenating their `chars` and `colors`
- `length` is just the `chars.length`
- `substring` is just calling `Arrays.copyOfRange` on both `chars` and `colors`

Without having to worry about removing Ansi codes first, or having our colors
get mixed up as we slice and concatenate them. In general, it behaves exactly 
like a `java.lang.String`, just with color.

Furthermore, all these operations are implemented as fast `System.arraycopy`s
and `Arrays.copyOfRange`s:

```scala
def splitAt(index: Int) = (
  new Str(
    util.Arrays.copyOfRange(chars, 0, index),
    util.Arrays.copyOfRange(colors, 0, index)
  ),
  new Str(
    util.Arrays.copyOfRange(chars, index, length),
    util.Arrays.copyOfRange(colors, index, length)
  )
)
```

Which perform much faster than copying the data yourself using a for-loop
or Scala collections operations like `.drop` and `.take`.

The downside is it does not have any special properties apart from 
`java.lang.String`: it is not a [Rope], it is not a [Persistent Data Structure]
with fancy structural-sharing, none of that stuff. It shares all the properties
of `java.lang.String`, for better or worse.

[Persistent Data Structure]: https://en.wikipedia.org/wiki/Persistent_data_structure
[Rope]: https://en.wikipedia.org/wiki/Rope_(data_structure)

### fansi.Attrs

```scala
sealed trait Attrs{

  /**
    * Apply these [[Attrs]] to the given [[fansi.Str]], making it take effect
    * across the entire length of that string.
    */
  def apply(s: fansi.Str): fansi.Str

  /**
    * Which bits of the [[Str.State]] integer these [[Attrs]] will
    * override when it is applied
    */
  def resetMask: Int

  /**
    * Which bits of the [[Str.State]] integer these [[Attrs]] will
    * set to `1` when it is applied
    */
  def applyMask: Int

  /**
    * Apply the current [[Attrs]] to the [[Str.State]] integer,
    * modifying it to represent the state after all changes have taken
    * effect
    */
  def transform(state: Str.State): State

  /**
    * Combine this [[fansi.Attrs]] with other [[fansi.Attrs]]s, returning one
    * which when applied is equivalent to applying this one and then the `other`
    * one in series.
    */
  def ++(other: fansi.Attrs): fansi.Attrs
}
```

- [Github Source](https://github.com/lihaoyi/fansi/blob/35b48525af8894484d2bc2f9403fbb3994774749/fansi/shared/src/main/scala/fansi/Fansi.scala#L331-L365)

This is the datatype representing zero or more decorations that can be applied
to a `Str`. This includes 

- No-op decorations like `Attrs.Empty`
- Individual decorations like `Colors.Red`, `Underlined.On`
- Resets like `Colors.Reset`, `Underlined.Off`
- Any combination of these (e.g. `Colors.Red ++ Underlined.On ++ Bold.Off`

Some of the methods on `Attrs` are relatively straightforward: you can `apply` 
them to `fansi.Str`s to provide color, you can `++` them to combine their 
effects. `transform` takes the decoration-state as a argument and 
returns the decoration-state after these `Attrs` have been applied. 

Others, like `resetMask`, `applyMask`, are more obscure. These will be easier 
to understand in the context of what exactly goes into the `Str.State` integer:

### fansi.Str.State

`Str.State` is an alias for `Int`, defined as

```scala
/**
  * An [[fansi.Str]]'s `color`s array is filled with Ints, each representing
  * the ANSI state of one character encoded in its bits. Each [[Attr]] belongs
  * to a [[Category]] that occupies a range of bits within each int:
  *
  * 31... 22 21 20 19 18 17 16 15 14 13 12 11 10  9  8  7  6  5  4  3  2  1  0
  *  |--------|  |-----------------------|  |-----------------------|  |  |  |bold
  *           |                          |                          |  |  |reversed
  *           |                          |                          |  |underlined
  *           |                          |                          |foreground-color
  *           |                          |background-color
  *           |unused
  *
  *
  * The `0000 0000 0000 0000` int corresponds to plain text with no decoration
  *
  */
type State = Int
```

- [Github Source](https://github.com/lihaoyi/fansi/blob/35b48525af8894484d2bc2f9403fbb3994774749/fansi/shared/src/main/scala/fansi/Fansi.scala#L221-L238)

In short, the different "kinds" of decoration each take up a separate bit-range
within the `State` integer. *Bold* takes the first bit, *reversed* the second 
bit, *underlined* the third bit. After that, the *Foreground Color* of the text 
takes up the next 9 bits (16 base colors + 256 extended-colors), and the 
*Background Color* the 9 bits after that. The remaining bits are un-used.

Hence, the `resetMask` of `Attrs` tells you which bit-ranges need to be cleared
in order for the `Attrs` to be applied, and the `applyMask` tells you what 
those bit-ranges will get set to. For example, `fansi.Color.LightGreen` has

```scala
scala> Integer.toBinaryString(fansi.Color.LightGreen.resetMask)
res8: String = 111111111000

scala> Integer.toBinaryString(fansi.Color.LightGreen.applyMask)
res9: String =      1011000
```

Thus, to turn the state `Int`'s foreground-color light green, you first zero 
out 4th to the 12th bit, and then set the 4th, 5th and 7th bits to `1`.

The `applyMask` and `resetMask` for combinations of `Attrs` can be computed
from those of each individual `Attrs` object.

That means that applying a set of `Attrs` to the current state `Int` is always
just three integer instructions:

```scala
def transform(state: Str.State) = (state & ~resetMask) | applyMask
```

And thus much faster than any design using structured data like `Set` objects 
and the like. Furthermore, storing all the data relevant to the current state
requires only 32 bits, far less than would be required to store a hash-table 
or tree or whatever data-structures a `Set` requires.

## Micro De-optimization

Now that we've gone through roughly how Fansi works, we will start with the 
step-by-step de-optimization of the library, removing the existing 
micro-optimizations one by one and seeing how the performance is affected by
each one.

If you want to browse the code, in the state where this exercise kicks off 
from, take a look at the commit in the Fansi repository:

- [Fansi 0.1.1](https://github.com/lihaoyi/fansi/tree/35b48525af8894484d2bc2f9403fbb3994774749)

If you want to follow along with the changes we're making, download the git 
bundle:

- [fansi.bundle](optimizing/fansi.bundle)

And `git clone fansi.bundle` on the downloaded file to get your own personal 
checkout of the Fansi repository, as is used in this post:
 
```
aoyi-mbp:demo haoyi$ git clone fansi.bundle
Cloning into 'fansi'...
Receiving objects: 100% (233/233), 1.22 MiB | 0 bytes/s, done.
Resolving deltas: 100% (72/72), done.
Checking connectivity... done.
haoyi-mbp:demo haoyi$ cd fansi
```

The last 7 commits:

```
haoyi-mbp:fansi haoyi$ git log --oneline
f9418b5 Bit Packing
d016d88 Arrays instead of Int-Maps
4a74c51 Tries instead of String-Maps
8841540 Speed through While Loops
a64a2fb Local Array Caching
777dfa8 arraycopy and copyOfRange
fd55742 Baseline (Optimized)
...
```

Correspond to the 7 stages being described in this post:

- [Baseline (Optimized)](#baseline-optimized)
- [arraycopy and copyOfRange](#arraycopy-and-copyofrange)
- [Local Array Caching](#local-array-caching)
- [Speed through While Loops](#speed-through-while-loops)
- [Tries instead of String-Maps](#tries-instead-of-string-maps)
- [Arrays instead of Int-Maps](#arrays-instead-of-int-maps)
- [Bit Packing](#bit-packing)

You can install 
[SBT](http://www.scala-sbt.org/) and run `sbt fansiJVM/test` to run the test 
suite and benchmarks yourself. At all points throughout this post, as the 
various optimizations are removed one by one, the full test suite is passing.

### Baseline (Optimized)

To measure baseline performance, before removing any optimizations, we first 
have to benchmark a few basic operations. For the purposes of this post, we'll 
be using really simplistic microbenchmarks:

```scala
val start = System.currentTimeMillis()
var count = 0
while(System.currentTimeMillis() < start + 5000){
  count += 1
  ...
}
val end = System.currentTimeMillis()
count
```

This isn't as accurate as a real benchmarking framework - but it will do for 
now. The changes we'll be seeing are large enough that they'll be obvious
despite the noise in the results, but if you want to be fancy you could use 
[JMH](http://openjdk.java.net/projects/code-tools/jmh/) or similar to get more 
precise or reliable benchmarks. 

And for this we'll benchmark a few basic operations:
 
- **Parsing** using `fansi.Str.apply` ([Source](https://github.com/lihaoyi/fansi/blob/35b48525af8894484d2bc2f9403fbb3994774749/fansi/shared/src/test/scala/fansi/FansiTests.scala#L269-L279))
- **Rendering** using `parsed.render` ([Source](https://github.com/lihaoyi/fansi/blob/35b48525af8894484d2bc2f9403fbb3994774749/fansi/shared/src/test/scala/fansi/FansiTests.scala#L280-L291))
- **Concatenation** using `fansiStr ++ fansiStr` ([Source](https://github.com/lihaoyi/fansi/blob/35b48525af8894484d2bc2f9403fbb3994774749/fansi/shared/src/test/scala/fansi/FansiTests.scala#L292-L302))
- **Splitting** using `fansiStr.splitAt` ([Source](https://github.com/lihaoyi/fansi/blob/35b48525af8894484d2bc2f9403fbb3994774749/fansi/shared/src/test/scala/fansi/FansiTests.scala#L303-L313))
- **Substring** using `fansiStr.substring` ([Source](https://github.com/lihaoyi/fansi/blob/35b48525af8894484d2bc2f9403fbb3994774749/fansi/shared/src/test/scala/fansi/FansiTests.scala#L314-L326))
- **Oerlay** using `fansiStr.overlay` ([Source](https://github.com/lihaoyi/fansi/blob/35b48525af8894484d2bc2f9403fbb3994774749/fansi/shared/src/test/scala/fansi/FansiTests.scala#L327-L347))

All these will be operating on the following sample input:

```scala
val input = s"+++$R---$G***$B///" * 1000
```

Which is just a generic 20,000 character input with cycling red/green/blue colors.

The baseline level of performance is approximately:

|               |    **Baseline** |
|               | **(Optimized)** |
|:--------------|----------------:|
| **Parsing**   |          29,267 |
| **Rendering** |          35,001 |
| **Concat**    |         249,353 |
| **Splitting** |         539,879 |
| **Substring** |       2,077,567 |
| **Overlay**   |         630,863 |

Where the numbers being shown are the numbers of iterations completed in the
5 second benchmark. If you want to try it on your own hardware, check out the 
code from [Github](https://github.com/lihaoyi/fansi/tree/35b48525af8894484d2bc2f9403fbb3994774749)
and run `fansiJVM/test` yourself.

These numbers are expected to vary, especially with the simplistic 
micro-benchmarking technique that we're doing, but even so the change in
performance due to our changes should be significant enough to easily see
despite the noise in our measurement.

### arraycopy and copyOfRange

The first step of making this "idiomatic" or "typical" Scala is to replace 
all our usage of `System.arraycopy` and `java.util.Arrays.*` with the 
corresponding `RichArray` operations. There are a number of usages:

- [++](https://github.com/lihaoyi/fansi/blob/35b48525af8894484d2bc2f9403fbb3994774749/fansi/shared/src/main/scala/fansi/Fansi.scala#L35-L44)
- [splitAt](https://github.com/lihaoyi/fansi/blob/35b48525af8894484d2bc2f9403fbb3994774749/fansi/shared/src/main/scala/fansi/Fansi.scala#L53-L62)
- [substring](https://github.com/lihaoyi/fansi/blob/35b48525af8894484d2bc2f9403fbb3994774749/fansi/shared/src/main/scala/fansi/Fansi.scala#L69-L80)

The diff to replace them is:

```diff
haoyi-mbp:fansi haoyi$ git --no-pager diff
diff --git a/fansi/shared/src/main/scala/fansi/Fansi.scala b/fansi/shared/src/main/scala/fansi/Fansi.scala
index fa5fd8d..ff29499 100644
--- a/fansi/shared/src/main/scala/fansi/Fansi.scala
+++ b/fansi/shared/src/main/scala/fansi/Fansi.scala
@@ -33,14 +33,7 @@ case class Str private(private val chars: Array[Char], private val colors: Array
     * avoiding any interference between them
     */
   def ++(other: Str) = {
-    val chars2 = new Array[Char](length + other.length)
-    val colors2 = new Array[Str.State](length + other.length)
-    System.arraycopy(chars, 0, chars2, 0, length)
-    System.arraycopy(other.chars, 0, chars2, length, other.length)
-    System.arraycopy(colors, 0, colors2, 0, length)
-    System.arraycopy(other.colors, 0, colors2, length, other.length)
-
-    Str(chars2, colors2)
+    Str(chars ++ other.chars, colors ++ other.colors)
   }

   /**
@@ -51,14 +44,8 @@ case class Str private(private val chars: Array[Char], private val colors: Array
     *              you want to use to split it.
     */
   def splitAt(index: Int) = (
-    new Str(
-      util.Arrays.copyOfRange(chars, 0, index),
-      util.Arrays.copyOfRange(colors, 0, index)
-    ),
-    new Str(
-      util.Arrays.copyOfRange(chars, index, length),
-      util.Arrays.copyOfRange(colors, index, length)
-    )
+    new Str(chars.take(index), colors.take(index)),
+    new Str(chars.drop(index), colors.drop(index))
   )

   /**
@@ -73,10 +60,7 @@ case class Str private(private val chars: Array[Char], private val colors: Array
     if (end < 0 || end >= length || end < start) throw new IllegalArgumentException(
       s"substring end parameter [$end] must be between start $start and $length"
     )
-    new Str(
-      util.Arrays.copyOfRange(chars, start, end),
-      util.Arrays.copyOfRange(colors, start, end)
-    )
+    new Str(chars.slice(start, end), colors.slice(start, end))
   }


@@ -291,10 +275,7 @@ object Str{
       }
     }

-    Str(
-      util.Arrays.copyOfRange(chars, 0, destIndex),
-      util.Arrays.copyOfRange(colors, 0, destIndex)
-    )
+    Str(chars.take(destIndex), colors.take(destIndex))
   }

   /**
```

This is a relatively straightforward change; it makes the code considerably
shorter, and is probably what most Scala programmers would do if they were 
implementing this themselves. After all, it isn't uncommmon for people to 
treat `Array[T]`s as normal Scala collections using the extension methods
in `RichArray`!

This doesn't quite make all the tests pass - the out-of-bounds behavior changes
since `.take` and `.drop` and `.slice` are more forgiving than their 
`java.util` counterparts. To pass we have to aggressively throw out-of-bounds
exceptions ourselves:

```scala
haoyi-mbp:fansi haoyi$ git diff
diff --git a/fansi/shared/src/main/scala/fansi/Fansi.scala b/fansi/shared/src/main/scala/fansi/Fansi.scala
index ff29499..f41ec41 100644
--- a/fansi/shared/src/main/scala/fansi/Fansi.scala
+++ b/fansi/shared/src/main/scala/fansi/Fansi.scala
@@ -43,10 +43,16 @@ case class Str private(private val chars: Array[Char], private val colors: Array
     * @param index the plain-text index of the point within the [[fansi.Str]]
     *              you want to use to split it.
     */
-  def splitAt(index: Int) = (
-    new Str(chars.take(index), colors.take(index)),
-    new Str(chars.drop(index), colors.drop(index))
-  )
+  def splitAt(index: Int) = {
+    if (index < 0 || index > chars.length){
+      throw new IllegalArgumentException("lol")
+    }
+
+    (
+      new Str(chars.take(index), colors.take(index)),
+      new Str(chars.drop(index), colors.drop(index))
+    )
+  }

   /**
     * Returns an [[fansi.Str]] which is a substring of this string,
```

And with this, all tests pass. 

It turns out that this works, and all the test cases pass, but at a cost of
some performance:

|               |    **Baseline** |    **No arraycopy** |
|               | **(Optimized)** | **and copyOfRange** |
|:--------------|----------------:|--------------------:|
| **Parsing**   |          29,267 |              15,977 |
| **Rendering** |          35,001 |              32,678 |
| **Concat**    |         249,353 |             227,241 |
| **Splitting** |         539,879 |              46,305 |
| **Substring** |       2,077,567 |             157,834 |
| **Overlay**   |         630,863 |             837,962 |


There's some noise in this measure, as you'd expect: **Rendering** and **Concat**
has seemed to have gotten faster. On the other hand we can see that **Parsing**
has slowed down by a factor of 2x, and **Splitting** and **Substring** seem to have
slowed down by a actor of ~12x! 

That's a huge slowdown for using `.slice` and `.take` and `.drop` instead of 
`Arrays.copyOfRange`. Equivalently, it's a huge 12x speedup for using 
`Arrays.copyOfRange` instead of `.slice`, `.take` and `.drop`! If you find 
yourself using `Array`s for performance reasons, `.copyOfRange` is definitely
something that's worth thinking of!

### Local Array Caching

The next micro-optimization we can try removing is the local `categoryArray` 
variable:

- [Github Source](https://github.com/lihaoyi/fansi/blob/35b48525af8894484d2bc2f9403fbb3994774749/fansi/shared/src/main/scala/fansi/Fansi.scala#L126-L128)

This was introduced to make the `while`-loop going over the `Attr.categories`
vector faster inside the `render` method. Iterating over an `Array` is faster 
than iterating over a `Vector`, and this one is in the critical path for the 
`.render` method converting our `fansi.Str`s into `java.lang.String`s.

Although allocating this array costs something, it's the `Attr.categories` 
vector only has 5 items in it, so allocating a 5-element array should be cheap.
For `render`ing any non-trivial `Str` the speed up from faster iteration would 
outweigh the cost of allocating that array.

Removing it:

```diff
haoyi-mbp:fansi haoyi$ git diff
diff --git a/fansi/shared/src/main/scala/fansi/Fansi.scala b/fansi/shared/src/main/scala/fansi/Fansi.scala
index f41ec41..7ad45fe 100644
--- a/fansi/shared/src/main/scala/fansi/Fansi.scala
+++ b/fansi/shared/src/main/scala/fansi/Fansi.scala
@@ -113,9 +113,6 @@ case class Str private(private val chars: Array[Char], private val colors: Array

     var currentState: Str.State = 0

-    // Make a local array copy of the immutable Vector, for maximum performance
-    // since the Vector is small and we'll be looking it up over & over & over
-    val categoryArray = Attr.categories.toArray
     /**
       * Emit the ansi escapes necessary to transition
       * between two states, if necessary.
@@ -132,8 +129,8 @@ case class Str private(private val chars: Array[Char], private val colors: Array
       }

       var categoryIndex = 0
-      while(categoryIndex < categoryArray.length){
-        val cat = categoryArray(categoryIndex)
+      while(categoryIndex < Attr.categories.length){
+        val cat = Attr.categories(categoryIndex)
         if ((cat.mask & currentState) != (cat.mask & nextState)){
           val attr = cat.lookupAttr(nextState & cat.mask)
```

Results in the following benchmarks:

|               |    **Baseline** |    **No arraycopy** | **Arrays to** |
|               | **(Optimized)** | **and copyOfRange** |   **Vectors** |
|:--------------|----------------:|--------------------:|--------------:|
| **Parsing**   |          29,267 |              15,977 |        11,663 |
| **Rendering** |          35,001 |              32,678 |        24,470 |
| **Concat**    |         249,353 |             227,241 |       242,999 |
| **Splitting** |         539,879 |              46,305 |        31,116 |
| **Substring** |       2,077,567 |             157,834 |       171,585 |
| **Overlay**   |         630,863 |             837,962 |       751,720 |


Again we have a bunch of noise,  but it seems that **Rendering** has gotten
a good amount slower: maybe about 25%. Not as large or obvious as the earlier
change, but not nothing either. 

What's the take-away? Even if you want your public APIs to be immutable and
"idiomatic", if you are going to be doing a lot of work with a data-structure
it could be worth copying it into a more optimal representation for how you
are using it: the speed up on the lot-of-work may well outweight the cost of
copying! In this case, I just need to iterate over the `Category`s that
are available, and there's no faster data-structure to iterate over than a 
flat `Array`.

### Speed through While Loops

The next micro de-optimization we're going to make is to convert a bunch of our
`while`-loops to `for`-loops. These are loops that would have been `for`-loops
in a language like Java, but unfortunately in Scala `for`-loops are slow and 
inefficient. Nevertheless, people often write `for`-loops naturally and only 
optimize it later. Let's convert these back to `for`-loops:

- [Category Loop](https://github.com/lihaoyi/fansi/blob/35b48525af8894484d2bc2f9403fbb3994774749/fansi/shared/src/main/scala/fansi/Fansi.scala#L144-L156)
- [Color Loop](https://github.com/lihaoyi/fansi/blob/35b48525af8894484d2bc2f9403fbb3994774749/fansi/shared/src/main/scala/fansi/Fansi.scala#L158-L169)
- [Overlay Loop](https://github.com/lihaoyi/fansi/blob/35b48525af8894484d2bc2f9403fbb3994774749/fansi/shared/src/main/scala/fansi/Fansi.scala#L207-L213)

```diff
haoyi-mbp:fansi haoyi$ git diff
haoyi-mbp:fansi haoyi$ git --no-pager diff
diff --git a/fansi/shared/src/main/scala/fansi/Fansi.scala b/fansi/shared/src/main/scala/fansi/Fansi.scala
index 7ad45fe..8ad7a99 100644
--- a/fansi/shared/src/main/scala/fansi/Fansi.scala
+++ b/fansi/shared/src/main/scala/fansi/Fansi.scala
@@ -128,9 +128,7 @@ case class Str private(private val chars: Array[Char], private val colors: Array
         currentState = 0
       }

-      var categoryIndex = 0
-      while(categoryIndex < Attr.categories.length){
-        val cat = Attr.categories(categoryIndex)
+      for(cat <- Attr.categories){
         if ((cat.mask & currentState) != (cat.mask & nextState)){
           val attr = cat.lookupAttr(nextState & cat.mask)

@@ -138,12 +136,10 @@ case class Str private(private val chars: Array[Char], private val colors: Array
             output.append(attr.escapeOpt.get)
           }
         }
-        categoryIndex += 1
       }
     }

-    var i = 0
-    while(i < colors.length){
+    for(i <- colors.indices){
       // Emit ANSI escapes to change colors where necessary
       // fast-path optimization to check for integer equality first before
       // going through the whole `enableDiff` rigmarole
@@ -152,7 +148,6 @@ case class Str private(private val chars: Array[Char], private val colors: Array
         currentState = colors(i)
       }
       output.append(chars(i))
-      i += 1
     }

     // Cap off the left-hand-side of the rendered string with any ansi escape
@@ -192,10 +187,8 @@ case class Str private(private val chars: Array[Char], private val colors: Array
       )

       {
-        var i = start
-        while (i < end) {
+        for(i <- start until end){
           colorsOut(i) = attrs.transform(colorsOut(i))
-          i += 1
         }
       }
     }
```

And see how that affects performance:

|               |    **Baseline** |    **No arraycopy** | **Arrays to** | **No While** |
|               | **(Optimized)** | **and copyOfRange** |   **Vectors** |    **Loops** |
|:--------------|----------------:|--------------------:|--------------:|-------------:|
| **Parsing**   |          29,267 |              15,977 |        11,663 |       12,108 |
| **Rendering** |          35,001 |              32,678 |        24,470 |       16,367 |
| **Concat**    |         249,353 |             227,241 |       242,999 |      252,083 |
| **Splitting** |         539,879 |              46,305 |        31,116 |       28,684 |
| **Substring** |       2,077,567 |             157,834 |       171,585 |      159,858 |
| **Overlay**   |         630,863 |             837,962 |       751,720 |      782,994 |

It turns out, most of the `while`-loops we converted were in the `.render` 
method, and we can see our **Rendering** benchmark slowing down by 1.5x. The only 
other `while` loop is in `.overlayAll` which, although used in `.overlay`, 
doesn't seem to affect the benchmarks much at all

Note we did not change the `while` loop in the `Str.apply` method we use to 
parse the `fansi.Str`s out of `java.lang.String`s:

```scala
def apply(raw: CharSequence, strict: Boolean = false): fansi.Str = {
  // Pre-allocate some arrays for us to fill up. They will probably be
  // too big if the input has any ansi codes at all but that's ok, we'll
  // trim them later.
  val chars = new Array[Char](raw.length)
  val colors = new Array[Int](raw.length)

  var currentColor = 0
  var sourceIndex = 0
  var destIndex = 0
  val length = raw.length
  while(sourceIndex < length){
    val char = raw.charAt(sourceIndex)
    ...
  }
  ...
}
```

This `while`-loop skips forward by varying numbers of characters each iteration,
and cannot be changed into a trivial for-loop like the others.

If you find the bottle-neck your program involves fancy Scala collections
methods like `.map` or `.foreach` on arrays, it's worth trying to re-write it 
in a `while`-loop to see if it gets any faster! Although it's a bit tedious and 
ugly, it's a relatively straightforward conversion to do and shouldn't take too
long to measure if it had any effect.

### Tries instead of String-Maps

One optimization that is present in `Fansi.scala` is the character-trie
at the bottom of the file, `Trie[T]`:

```scala
/**
  * An string trie for quickly looking up values of type [[T]]
  * using string-keys. Used to speed up
  */
private[this] final class Trie[T](strings: Seq[(String, T)]){
  ...
}
```

- [Github Source](https://github.com/lihaoyi/fansi/blob/35b48525af8894484d2bc2f9403fbb3994774749/fansi/shared/src/main/scala/fansi/Fansi.scala#L610-L660)

This is used to make prefix-matching of the various
Ansi escape codes much faster: matching can be done in 
`O(number-of-characters)`, regardless of how many different patterns need to
be matched, and is done in a single iteration over the characters without
needing to do any hashing or string-comparisons.

This is a custom-written trie. Although it only took about 50 characters to 
implement, it isn't something that a typical Scala programmer would reach for
out of the box. Typically, we would reach for a `Map[String, T]` first. It's
relatively straightforward to convert the `new Trie` construction into a 
`.toMap` call:

```diff
haoyi-mbp:fansi haoyi$ git diff
diff --git a/fansi/shared/src/main/scala/fansi/Fansi.scala b/fansi/shared/src/main/scala/fansi/Fansi.scala
index 8ad7a99..e773380 100644
--- a/fansi/shared/src/main/scala/fansi/Fansi.scala
+++ b/fansi/shared/src/main/scala/fansi/Fansi.scala
@@ -223,6 +223,8 @@ object Str{
     */
   implicit def implicitApply(raw: CharSequence): fansi.Str = apply(raw)

+  val ansiRegex = "\u001b\\[[0-9]+m".r.pattern
+
   /**
     * Creates an [[fansi.Str]] from a non-fansi `java.lang.String` or other
     * `CharSequence`.
@@ -245,13 +247,17 @@ object Str{
     var sourceIndex = 0
     var destIndex = 0
     val length = raw.length
+
     while(sourceIndex < length){
       val char = raw.charAt(sourceIndex)
       if (char == '\u001b' || char == '\u009b') {
-        ParseMap.query(raw, sourceIndex) match{
-          case Some(tuple) =>
-            currentColor = tuple._2.transform(currentColor)
-            sourceIndex += tuple._1
+
+        val m = ansiRegex.matcher(raw)
+        val snippet = if (m.find(sourceIndex)) Some(m.group()) else None
+        snippet.flatMap(ParseMap.get) match{
+          case Some(attr) =>
+            currentColor = attr.transform(currentColor)
+            sourceIndex += snippet.get.length
           case None =>
             if (strict) {
               // If we found the start of an escape code that was missed by our
@@ -293,7 +299,7 @@ object Str{
       color <- cat.all
       str <- color.escapeOpt
     } yield (str, color)
-    new Trie(pairs :+ (Console.RESET -> Attr.Reset))
+    (pairs :+ (Attr.Reset.escapeOpt.get, Attr.Reset)).toMap
   }
 }
```

The only slightly-tricky thing is that a `Map[String, Attr]` does not let you
easily check for prefix-matches. Thus we have a choice of either

- Iterating over ever key/value in the map and checking if it matches starting
  at `sourceIndex`
- Using a regex to try and pull out the Ansi color code, before putting it into
  the map
  
In this case we did the second option, and here's how the numbers look:

|               |    **Baseline** |    **No arraycopy** | **Arrays to** | **No While** |   **No** |
|               | **(Optimized)** | **and copyOfRange** |   **Vectors** |    **Loops** | **Trie** |
|:--------------|----------------:|--------------------:|--------------:|-------------:|---------:|
| **Parsing**   |          29,267 |              15,977 |        11,663 |       12,108 |    4,963 |
| **Rendering** |          35,001 |              32,678 |        24,470 |       16,367 |   11,455 |
| **Concat**    |         249,353 |             227,241 |       242,999 |      252,083 |  225,972 |
| **Splitting** |         539,879 |              46,305 |        31,116 |       28,684 |   30,168 |
| **Substring** |       2,077,567 |             157,834 |       171,585 |      159,858 |  158,109 |
| **Overlay**   |         630,863 |             837,962 |       751,720 |      782,994 |  676,621 |

Again, there is a great deal of noise in these results. Nevertheless, one thing
is clear: the **Parsing** performance has dropped by half, *again*! It's now down
to under a quarter of what it started off as, and even the significant noise in
the measurements can't hide that.

Tries are great data-structures. If you're dealing with a lot of 
`Map[String, T]`s, and find that looking up things in those maps is the 
bottleneck in your code, swapping in a Trie could give a great performance
boost.

### Arrays instead of Int-Maps

One bit of unusual code is the `val lookupAttrTable: Array[Attr]` that's part
 of the `Category` class
 
```scala
def lookupAttr(applyState: Int) = lookupAttrTable(applyState >> offset)
// Allows fast lookup of categories based on the desired applyState
private[this] lazy val lookupAttrTable = {
  val arr = new Array[Attr](1 << width)
  for(attr <- all){
    arr(attr.applyMask >> offset) = attr
  }
  arr
}
```

- [Github Source](https://github.com/lihaoyi/fansi/blob/35b48525af8894484d2bc2f9403fbb3994774749/fansi/shared/src/main/scala/fansi/Fansi.scala#L491-L499)

The purpose of this method is to make it quick to look up an `Attr` based on its
`.applyMask`. The `applyMask` is a unique ID for each `Attr`, and no two 
`Attr`s will share it. However the `.applyMask` itself is a bit-mask that could 
correspond to a relatively large integer, e.g. for setting the background color 
via `Back.LightGreen`

```scala
scala> Integer.toBinaryString(fansi.Back.LightGreen.applyMask)
res1: String = 1011000000000000

scala> fansi.Back.LightGreen.applyMask
res2: Int = 45056
```

Nevertheless, much of the size of that integer is due to the `offset` of the 
category to stop it from overlapping with others; in this case, for example, 
the `applyMask` of `Back.LightGreen` can only really start after the twelfth
bit (the area which the `resetMask` covers)

```scala
scala> Integer.toBinaryString(fansi.Back.LightGreen.resetMask)
res3: String = 111111111000000000000
```

Hence, by looking up the `Attr` via it's `applyMask >> offset`, we are able to
keep the lookup to a relatively integer, in the hundreds. By pre-filling
the `lookupAttrTable` array, we can make the lookup really fast, without wasting
any space storing huge, empty `Array`s. And `Array` lookup is much, much faster 
than if we had used a `Map`.

However, a typical Scala programmer taking a first cut at this problem won't do
all this stuff; they'll simply take the `.applyMask` and dump it in a 
`Map[Int, Attr]` and be done with it! It turns out that this is far less code:

```diff
haoyi-mbp:fansi haoyi$ git diff
diff --git a/fansi/shared/src/main/scala/fansi/Fansi.scala b/fansi/shared/src/main/scala/fansi/Fansi.scala
index 8ad7a99..55e7671 100644
--- a/fansi/shared/src/main/scala/fansi/Fansi.scala
+++ b/fansi/shared/src/main/scala/fansi/Fansi.scala
@@ -465,15 +465,7 @@ sealed abstract class Category(val offset: Int, val width: Int)(implicit catName
   def mask = ((1 << width) - 1) << offset
   val all: Seq[Attr]

-  def lookupAttr(applyState: Int) = lookupAttrTable(applyState >> offset)
-  // Allows fast lookup of categories based on the desired applyState
-  private[this] lazy val lookupAttrTable = {
-    val arr = new Array[Attr](1 << width)
-    for(attr <- all){
-      arr(attr.applyMask >> offset) = attr
-    }
-    arr
-  }
+  lazy val lookupAttr: Map[Int, Attr] = all.map{attr => attr.applyMask -> attr}.toMap
   def makeAttr(s: String, applyValue: Int)(implicit name: sourcecode.Name) = {
     new EscapeAttr(s, mask, applyValue << offset)(catName.value + "." + name.value)
   }
```

But you end up paying a performance cost for it:

|               |    **Baseline** |    **No arraycopy** | **Arrays to** | **No While** |   **No** |     **No Fast** |
|               | **(Optimized)** | **and copyOfRange** |   **Vectors** |    **Loops** | **Trie** | **Attr Lookup** |
|:--------------|----------------:|--------------------:|--------------:|-------------:|---------:|----------------:|
| **Parsing**   |          29,267 |              15,977 |        11,663 |       12,108 |    4,963 |           5,343 |
| **Rendering** |          35,001 |              32,678 |        24,470 |       16,367 |   11,455 |          10,971 |
| **Concat**    |         249,353 |             227,241 |       242,999 |      252,083 |  225,972 |         229,399 |
| **Splitting** |         539,879 |              46,305 |        31,116 |       28,684 |   30,168 |          34,835 |
| **Substring** |       2,077,567 |             157,834 |       171,585 |      159,858 |  158,109 |         169,332 |
| **Overlay**   |         630,863 |             837,962 |       751,720 |      782,994 |  676,621 |         624,217 |

Among the noise, it seems **Overlay** has gotten about 10% slower as a result of 
this change. Not a huge jump, but not insignificant! In general, if you find 
yourself dealing with `Map[Int, T]`s, if you can figure out a way to keep the 
`Int`s you're looking up in the map small then using an `Array` would be a lot
faster.

### Bit Packing

The last, and perhaps most significant micro-optimization that we are going
to remove, is the use of bit-packed `Int`s to implement the `Str.State` type.
A more "idiomatic" implementation would be using some kind of `case class`
with different fields representing the different categories of attributes that
can take effect, or perhaps a `Map[Category, Attr]` to ensure that we only
ever have one `Attr` in place for each `Category`. Perhaps replacing:

```scala

/**
  * An [[fansi.Str]]'s `color`s array is filled with Ints, each representing
  * the ANSI state of one character encoded in its bits. Each [[Attr]] belongs
  * to a [[Category]] that occupies a range of bits within each int:
  *
  * 31... 22 21 20 19 18 17 16 15 14 13 12 11 10  9  8  7  6  5  4  3  2  1  0
  *  |--------|  |-----------------------|  |-----------------------|  |  |  |bold
  *           |                          |                          |  |  |reversed
  *           |                          |                          |  |underlined
  *           |                          |                          |foreground-color
  *           |                          |background-color
  *           |unused
  *
  *
  * The `0000 0000 0000 0000` int corresponds to plain text with no decoration
  *
  */
type State = Int
```

With something like

```scala
case class State(mapping: Map[Category, Attr] = Map(
  Bold -> Bold.Off,
  Reversed -> Reversed.Off,
  Underlined -> Underlined.Off,
  Color -> Color.Reset,
  Back -> Back.Reset
))
```

That is to say, rather than trying to fit everything into bits, storing it as
a proper map of `Category` to `Attr`, ensuring that we only have one `Attr` for
any given category.

The actual change to implement this idea is somewhat long - a lot of code in
Fansi touches the `Str.State` in various ways and needs to be tweaked! - but
it's not fundamentally difficult. 

- `0`s get replaced by `Str.State()`
- Bit operations like `(attr.resetMask & ~resetMask) != 0` get replaced by 
  set-operations like `!resetMask.contains(attr.cat)`
- All the `applyMask` and `resetMask` attributes get removed, and are replaced
  by a `cats` attribute representing the categories than an `Attrs` touches and
  an `attrs` set that maintains which `Attr`s this `Attrs` is aggregating.
- Bit-mask-specific unit tests get removed

Nevertheless, it is a long diff, so feel free to skip over it to continue 
reading if it doesn't interest you:

```diff
haoyi-mbp:fansi haoyi$ git --no-pager diff f132a0b9738599d4024e748ae7bd452e6fd80617
diff --git a/fansi/shared/src/main/scala/fansi/Fansi.scala b/fansi/shared/src/main/scala/fansi/Fansi.scala
index f3bad04..bf5b155 100644
--- a/fansi/shared/src/main/scala/fansi/Fansi.scala
+++ b/fansi/shared/src/main/scala/fansi/Fansi.scala
@@ -22,10 +22,10 @@ import scala.collection.mutable
   */
 case class Str private(private val chars: Array[Char], private val colors: Array[Str.State]) {
   require(chars.length == colors.length)
-  override def hashCode() = util.Arrays.hashCode(chars) + util.Arrays.hashCode(colors)
+  override def hashCode() = util.Arrays.hashCode(chars) + colors.map(_.hashCode()).sum
   override def equals(other: Any) = other match{
     case o: fansi.Str =>
-      util.Arrays.equals(chars, o.chars) && util.Arrays.equals(colors, o.colors)
+      util.Arrays.equals(chars, o.chars) && colors.toSeq == o.colors.toSeq
     case _ => false
   }
   /**
@@ -111,27 +111,26 @@ case class Str private(private val chars: Array[Char], private val colors: Array
     val output = new StringBuilder(chars.length + colors.length * 5)


-    var currentState: Str.State = 0
+    var currentState: Str.State = Str.State()

     /**
       * Emit the ansi escapes necessary to transition
       * between two states, if necessary.
       */
-    def emitDiff(nextState: Int) = if (currentState != nextState){
-      val hardOffMask = Bold.mask
+    def emitDiff(nextState: Str.State) = if (currentState != nextState){
+
       // Any of these transitions from 1 to 0 within the hardOffMask
       // categories cannot be done with a single ansi escape, and need
       // you to emit a RESET followed by re-building whatever ansi state
       // you previous had from scratch
-      if ((currentState & ~nextState & hardOffMask) != 0){
+      if (nextState.mapping(Bold) == Bold.Off && currentState.mapping(Bold) == Bold.On){
         output.append(Console.RESET)
-        currentState = 0
+        currentState = Str.State()
       }

       for(cat <- Attr.categories){
-        if ((cat.mask & currentState) != (cat.mask & nextState)){
-          val attr = cat.lookupAttr(nextState & cat.mask)
-
+        val attr = nextState.mapping(cat)
+        if (currentState.mapping(cat) != nextState.mapping(cat)){
           if (attr.escapeOpt.isDefined) {
             output.append(attr.escapeOpt.get)
           }
@@ -152,7 +151,7 @@ case class Str private(private val chars: Array[Char], private val colors: Array

     // Cap off the left-hand-side of the rendered string with any ansi escape
     // codes necessary to rest the state to 0
-    emitDiff(0)
+    emitDiff(Str.State())

     output.toString
   }
@@ -198,24 +197,13 @@ case class Str private(private val chars: Array[Char], private val colors: Array

 object Str{

-  /**
-    * An [[fansi.Str]]'s `color`s array is filled with Ints, each representing
-    * the ANSI state of one character encoded in its bits. Each [[Attr]] belongs
-    * to a [[Category]] that occupies a range of bits within each int:
-    *
-    * 31... 22 21 20 19 18 17 16 15 14 13 12 11 10  9  8  7  6  5  4  3  2  1  0
-    *  |--------|  |-----------------------|  |-----------------------|  |  |  |bold
-    *           |                          |                          |  |  |reversed
-    *           |                          |                          |  |underlined
-    *           |                          |                          |foreground-color
-    *           |                          |background-color
-    *           |unused
-    *
-    *
-    * The `0000 0000 0000 0000` int corresponds to plain text with no decoration
-    *
-    */
-  type State = Int
+  case class State(mapping: Map[Category, Attr] = Map(
+    Bold -> Bold.Off,
+    Reversed -> Reversed.Off,
+    Underlined -> Underlined.Off,
+    Color -> Color.Reset,
+    Back -> Back.Reset
+  ))

   /**
     * Make the construction of [[fansi.Str]]s from `String`s and other
@@ -241,9 +229,9 @@ object Str{
     // too big if the input has any ansi codes at all but that's ok, we'll
     // trim them later.
     val chars = new Array[Char](raw.length)
-    val colors = new Array[Int](raw.length)
+    val colors = new Array[Str.State](raw.length)

-    var currentColor = 0
+    var currentColor = Str.State()
     var sourceIndex = 0
     var destIndex = 0
     val length = raw.length
@@ -299,7 +287,7 @@ object Str{
       color <- cat.all
       str <- color.escapeOpt
     } yield (str, color)
-    (pairs :+ (Attr.Reset.escapeOpt.get, Attr.Reset)).toMap
+    (pairs :+ (Console.RESET, Attr.Reset)).toMap
   }
 }

@@ -319,24 +307,21 @@ sealed trait Attrs{
     */
   def apply(s: fansi.Str) = s.overlay(this, 0, s.length)

-  /**
-    * Which bits of the [[Str.State]] integer these [[Attrs]] will
-    * override when it is applied
-    */
-  def resetMask: Int
+  def cats: Set[Category]

-  /**
-    * Which bits of the [[Str.State]] integer these [[Attrs]] will
-    * set to `1` when it is applied
-    */
-  def applyMask: Int
+  def attrs: Seq[Attr]

   /**
     * Apply the current [[Attrs]] to the [[Str.State]] integer,
     * modifying it to represent the state after all changes have taken
     * effect
     */
-  def transform(state: Str.State) = (state & ~resetMask) | applyMask
+  def transform(state: Str.State) = {
+    attrs.foldLeft(state)(
+      (s, attr) => s.copy(mapping = s.mapping.updated(attr.cat, attr))
+    )
+
+  }

   /**
     * Combine this [[fansi.Attrs]] with other [[fansi.Attrs]]s, returning one
@@ -353,8 +338,8 @@ object Attrs{

   def apply(attrs: Attr*): Attrs = {
     var output = List.empty[Attr]
-    var resetMask = 0
-    var applyMask = 0
+    var resetMask = Set.empty[Category]
+    var applyMask = Set.empty[Attr]
     // Walk the list of attributes backwards, and aggregate only those whose
     // `resetMask` is not going to get totally covered by the union of all
     // `resetMask`s that come after it.
@@ -363,20 +348,19 @@ object Attrs{
     // all aggregated `attr`s whose own `applyMask` is not totally covered by
     // the union of all `resetMask`s that come after.
     for(attr <- attrs.reverseIterator){
-      if ((attr.resetMask & ~resetMask) != 0){
-        if ((attr.applyMask & resetMask) == 0) applyMask = applyMask | attr.applyMask
-        resetMask = resetMask | attr.resetMask
+      if (!resetMask.contains(attr.cat)){
+        resetMask = resetMask + attr.cat
         output = attr :: output
       }
     }

+
     if (output.length == 1) output.head
-    else new Multiple(resetMask, applyMask, output.toArray.reverse:_*)
+    else new Multiple(output.reverse:_*)
   }

-  class Multiple private[Attrs] (val resetMask: Int,
-                                 val applyMask: Int,
-                                 val attrs: Attr*) extends Attrs{
+  class Multiple private[Attrs] (val attrs: Attr*) extends Attrs{
+    val cats = attrs.flatMap(_.cats).toSet
     assert(attrs.length != 1)
     override def hashCode() = attrs.hashCode()
     override def equals(other: Any) = (this, other) match{
@@ -409,7 +393,11 @@ object Attrs{
   * http://misc.flogisoft.com/bash/tip_colors_and_formatting
   */
 sealed trait Attr extends Attrs {
+
+  def cat: Category
+  lazy val cats = Set(cat)
   def attrs = Seq(this)
+  def applyValue: Int
   /**
     * escapeOpt the actual ANSI escape sequence corresponding to this Attr
     */
@@ -428,7 +416,8 @@ object Attr{
     * Represents the removal of all ansi text decoration. Doesn't fit into any
     * convenient category, since it applies to them all.
     */
-  val Reset = new EscapeAttr(Console.RESET, Int.MaxValue, 0)
+  val Reset = Color.Reset ++ Back.Reset ++ Bold.Off ++ Reversed.Off ++ Underlined.Off
+

   /**
     * A list of possible categories
@@ -444,8 +433,9 @@ object Attr{
 /**
   * An [[Attr]] represented by an fansi escape sequence
   */
-case class EscapeAttr private[fansi](escape: String, resetMask: Int, applyMask: Int)
+case class EscapeAttr private[fansi](escape: String, cat: Category, applyValue: Int)
                                     (implicit sourceName: sourcecode.Name) extends Attr{
+
   def escapeOpt = Some(escape)
   val name = sourceName.value
   override def toString = escape + name + Console.RESET
@@ -454,7 +444,7 @@ case class EscapeAttr private[fansi](escape: String, resetMask: Int, applyMask:
 /**
   * An [[Attr]] for which no fansi escape sequence exists
   */
-case class ResetAttr private[fansi](resetMask: Int, applyMask: Int)
+case class ResetAttr private[fansi](cat: Category, applyValue: Int)
                                    (implicit sourceName: sourcecode.Name) extends Attr{
   def escapeOpt = None
   val name = sourceName.value
@@ -467,23 +457,23 @@ case class ResetAttr private[fansi](resetMask: Int, applyMask: Int)
   * Represents a set of [[fansi.Attr]]s all occupying the same bit-space
   * in the state `Int`
   */
-sealed abstract class Category(val offset: Int, val width: Int)(implicit catName: sourcecode.Name){
-  def mask = ((1 << width) - 1) << offset
+sealed abstract class Category()(implicit catName: sourcecode.Name){
+
   val all: Seq[Attr]

-  lazy val lookupAttr: Map[Int, Attr] = all.map{attr => attr.applyMask -> attr}.toMap
+
   def makeAttr(s: String, applyValue: Int)(implicit name: sourcecode.Name) = {
-    new EscapeAttr(s, mask, applyValue << offset)(catName.value + "." + name.value)
+    new EscapeAttr(s, this, applyValue)(catName.value + "." + name.value)
   }
   def makeNoneAttr(applyValue: Int)(implicit name: sourcecode.Name) = {
-    new ResetAttr(mask, applyValue << offset)(catName.value + "." + name.value)
+    new ResetAttr(this, applyValue)(catName.value + "." + name.value)
   }
 }

 /**
   * [[Attr]]s to turn text bold/bright or disable it
   */
-object Bold extends Category(offset = 0, width = 1){
+object Bold extends Category(){
   val On  = makeAttr(Console.BOLD, 1)
   val Off = makeNoneAttr(          0)
   val all = Seq(On, Off)
@@ -493,7 +483,7 @@ object Bold extends Category(offset = 0, width = 1){
   * [[Attr]]s to reverse the background/foreground colors of your text,
   * or un-reverse them
   */
-object Reversed extends Category(offset = 1, width = 1){
+object Reversed extends Category(){
   val On  = makeAttr(Console.REVERSED,   1)
   val Off = makeAttr("\u001b[27m",       0)
   val all = Seq(On, Off)
@@ -501,7 +491,7 @@ object Reversed extends Category(offset = 1, width = 1){
 /**
   * [[Attr]]s to enable or disable underlined text
   */
-object Underlined extends Category(offset = 2, width = 1){
+object Underlined extends Category(){
   val On  = makeAttr(Console.UNDERLINED, 1)
   val Off = makeAttr("\u001b[24m",       0)
   val all = Seq(On, Off)
@@ -510,7 +500,7 @@ object Underlined extends Category(offset = 2, width = 1){
 /**
   * [[Attr]]s to set or reset the color of your foreground text
   */
-object Color extends Category(offset = 3, width = 9){
+object Color extends Category(){

   val Reset        = makeAttr("\u001b[39m",     0)
   val Black        = makeAttr(Console.BLACK,    1)
@@ -545,7 +535,7 @@ object Color extends Category(offset = 3, width = 9){
 /**
   * [[Attr]]s to set or reset the color of your background
   */
-object Back extends Category(offset = 12, width = 9){
+object Back extends Category(){

   val Reset        = makeAttr("\u001b[49m",       0)
   val Black        = makeAttr(Console.BLACK_B,    1)
diff --git a/fansi/shared/src/test/scala/fansi/FansiTests.scala b/fansi/shared/src/test/scala/fansi/FansiTests.scala
index b812d6f..3af0ee8 100644
--- a/fansi/shared/src/test/scala/fansi/FansiTests.scala
+++ b/fansi/shared/src/test/scala/fansi/FansiTests.scala
@@ -16,7 +16,7 @@ object FansiTests extends TestSuite{
   val REV = fansi.Reversed.On.escape
   val DREV = fansi.Reversed.Off.escape
   val DCOL = fansi.Color.Reset.escape
-  val RES = fansi.Attr.Reset.escape
+  val RES = Console.RESET
   /**
     * ANSI escape sequence to reset text color
     */
@@ -213,41 +213,6 @@ object FansiTests extends TestSuite{
       }
     }
     'multipleAttrs{
-      'identicalMasksGetCollapsed{
-        val redRed = fansi.Color.Red ++ fansi.Color.Red
-        assert(
-          redRed.resetMask == fansi.Color.Red.resetMask,
-          redRed.applyMask == fansi.Color.Red.applyMask
-        )
-      }
-      'overlappingMasksGetReplaced{
-        val redBlue = fansi.Color.Red ++ fansi.Color.Blue
-        assert(
-          redBlue.resetMask == fansi.Color.Blue.resetMask,
-          redBlue.applyMask == fansi.Color.Blue.applyMask
-        )
-      }
-      'semiOverlappingMasks{
-        val resetRed = fansi.Attr.Reset ++ fansi.Color.Red
-        val redReset = fansi.Color.Red ++ fansi.Attr.Reset
-        assert(
-          resetRed != fansi.Attr.Reset,
-          resetRed != fansi.Color.Red,
-          redReset == fansi.Attr.Reset,
-          redReset != fansi.Color.Red,
-          redReset != resetRed,
-          resetRed.resetMask == fansi.Attr.Reset.resetMask,
-          resetRed.applyMask == fansi.Color.Red.applyMask
-        )
-      }
-      'separateMasksGetCombined{
-        val redBold = fansi.Color.Red ++ fansi.Bold.On
-
-        assert(
-          redBold.resetMask == (fansi.Color.Red.resetMask | fansi.Bold.On.resetMask),
-          redBold.applyMask == (fansi.Color.Red.applyMask | fansi.Bold.On.applyMask)
-        )
-      }
       'applicationWorks{
         val redBlueBold = fansi.Color.Red ++ fansi.Color.Blue ++ fansi.Bold.On
         val colored = redBlueBold("Hello World")
```

Despite the fact that this is a widespread change, all tests pass after (except
the not-applicable ones we deleted)

```
[info] Tests: 63
[info] Passed: 63
[info] Failed: 0
```

So we can be confident that despite being implemented totally differently, the
externally-visible behavior is exactly the same.
 
So what does the performance look like?
    
|               |    **Baseline** |    **No arraycopy** | **Arrays to** | **No While** |   **No** |     **No Fast** |  **No Bit** |
|               | **(Optimized)** | **and copyOfRange** |   **Vectors** |    **Loops** | **Trie** | **Attr Lookup** | **Packing** |
|:--------------|----------------:|--------------------:|--------------:|-------------:|---------:|----------------:|------------:|
| **Parsing**   |          29,267 |              15,977 |        11,663 |       12,108 |    4,963 |           5,343 |       3,773 |
| **Rendering** |          35,001 |              32,678 |        24,470 |       16,367 |   11,455 |          10,971 |       3,766 |
| **Concat**    |         249,353 |             227,241 |       242,999 |      252,083 |  225,972 |         229,399 |     253,105 |
| **Splitting** |         539,879 |              46,305 |        31,116 |       28,684 |   30,168 |          34,835 |      54,804 |
| **Substring** |       2,077,567 |             157,834 |       171,585 |      159,858 |  158,109 |         169,332 |     163,447 |
| **Overlay**   |         630,863 |             837,962 |       751,720 |      782,994 |  676,621 |         624,217 |      16,631 |

Among all the ups and downs, it looks like in doing so we've made **Rendering**
3x slower, **Parsing** about 2.5x slower, and **Overlay** a whooping 40x 
slower! On the other hand, other benchmarks like **Concat**, **Splitting** and 
**Substring** seem unaffected.

The huge slowdown to **Overlay** is not unexpected: after all, we do the most 
of our heavy lifting regarding `Str.State` inside `.overlay`, where we need to 
apply the modifications to the state of every character our `Attrs` are being 
overlayed on. The main cause for the slowdown is likely this change: 
 
```diff
   /**
     * Apply the current [[Attrs]] to the [[Str.State]] integer,
     * modifying it to represent the state after all changes have taken
     * effect
     */
-  def transform(state: Str.State) = (state & ~resetMask) | applyMask
+  def transform(state: Str.State) = {
+    attrs.foldLeft(state)(
+      (s, attr) => s.copy(mapping = s.mapping.updated(attr.cat, attr))
+    )
+
+  }
```

Which replaces three tiny bit-wise operations that perform the state update
all at once with a relatively slow `.foldLeft` and `.copy` over the `attrs`

**Parsing** and **Rendering** are similar, but have other considerations
e.g. the speed of the actual parser and the speed of the 
output-string-generation as part of the benchmark, while **Overlay** is almost
entirely bottlenecked on the `Attrs#transform` operations. It's not at all 
surprising that performing a bunch of `Map` operations on structured data is 
40x slower than performing a few bit-shifts on an `Int`!

It turns out there's a memory cost too. Unlike all the other changes we made 
earlier, this one actually changes the representation of the data-structure.
Measuring memory usage in Java is somewhat tedious, but any modern Java profiler
(e.g. JProfiler) should do it just fine. You can easily define objects and 
values in the Scala REPL:

```scala
scala> val hundredThousand = fansi.Str(s"+++$R---$G***$B///" * 100000)
```

and ask JProfiler how big they are via it's *Biggest Objects* tab:

![MemoryMeasurement.png](optimizing/MemoryMeasurement.png)

Spending a few minutes running this over and over on a range of string lengths
using different kinds of strings, we can quickly see how much memory is being
taken up by various data structures:

|   String Length | Case Class | Bit Packed |          Colored |        Uncolored |
| (Visible Chars) |  fansi.Str |  fansi.Str | java.lang.String | java.lang.String |
|----------------:|-----------:|-----------:|-----------------:|-----------------:|
|     12000 chars |      456kb |       72kb |             54kb |             24kb |
|    120000 chars |    4,560kb |      720kb |            540kb |            240kb |
|   1200000 chars |   45,600kb |    7,200kb |          5,400kb |          2,400kb |

It turns out that the `case class`/`Map` representation of a `Str.State` takes 
~6.3 times as much memory as the bit-packed version! And ~8.5 times as much 
memory as the colored `java.lang.String`s. In comparison, the bit-packed 
version take only ~1.3 times as much memory as the colored `java.lang.String`s.
And as expected, the uncolored `java.lang.String` containing `12000` `Char`s
takes `24kb`, since in Java each `Char` is a 
[UTF-16](https://en.wikipedia.org/wiki/UTF-16) character and takes 2 bytes.

Bit-packing is a technique that is often ignored in "high level" languages like 
Scala, despite having a rich history of usage in C++, C, or Assembly programs. 
Nevertheless, as this example demonstrates, it can lead to huge 
improvements in performance and memory-usage in the cases where it can be used.
Not only does it take up less memory, but bitwise operations on `Int`s or 
`Long`s are going to be much, much, much faster than any methods you could
call on a `Set` or a `Map`.

If you are dealing with a `Set` or `Map` which is the bottle-neck within your 
program, it's worth considering whether you can replace it with a `BitSet` or
even just a plain old `Int` or `Long`. 
 
## Conclusion

So far we've been removing one optimization at a time and seeing what happens. 
But what we haven't done is taken a step back and considered what the aggregate
affect of all the optimizations is! The combined result of these 6 optimizations:

- [arraycopy and copyOfRange](#arraycopy-and-copyofrange)
- [Local Array Caching](#local-array-caching)
- [Speed through While Loops](#speed-through-while-loops)
- [Tries instead of String-Maps](#tries-instead-of-string-maps)
- [Arrays instead of Int-Maps](#arrays-instead-of-int-maps)
- [Bit Packing](#bit-packing)

Can be summarized in one table:

|               |    **Baseline** | **No Bit Packing** | **Ratio** |
|               | **(Optimized)** |  **(Unoptimized)** |           |
|:--------------|----------------:|-------------------:|----------:|
| **Parsing**   |          29,267 |              3,773 |      ~7.6 |
| **Rendering** |          35,001 |              3,766 |      ~9.3 |
| **Concat**    |         249,353 |            253,105 |      ~1.0 |
| **Splitting** |         539,879 |             54,804 |      ~9.9 |
| **Substring** |       2,077,567 |            163,447 |     ~12.7 |
| **Overlay**   |         630,863 |             16,631 |     ~37.9 |
| **Memory**    |         7,200kb |           45,600kb |    ~1/6.3 |


As you can see, the combination of micro-optimizations makes the common 
operations in the Fansi library anywhere from ~7.6x to ~37.9x (!) times faster, 
and have made it take ~6.3x less memory to store its data-structures. The 
only benchmark that hasn't changed is the **Concat** benchmark of the `++`
operation: I guess `Array#++` is already reasonably efficient and
there's no speed-up to be had.

Although here we worked backwards from already-optimized code to demonstrate 
the gains, these are exactly the same gains to be had if you had started from 
un-optimized code and worked forwards, using a profiler to guide you as 
described in [Methodology](#methodology). These are non trivial performance
gains to be had; but are they worth the cost?
 
### Is it Worth It?

This is a question everyone asks: is it worth putting in the effort to 
micro-optimize something and mess up the code a little, in exchange for the 
performance gain?

It's a reasonable question to ask, and the answer depends on what sort of 
code it is:

- [What is "Fast Enough"](#what-is-fast-enough)
- [How often is it modified?](#how-often-is-it-modified)
- [What are the alternatives?](#what-are-the-alternatives)

### What is "Fast Enough"?

For example, if we consider a 10x as the approximate speedup for Fansi's 
operations, that means:

- **A 0.1ms operation becomes 1ms**
- **A 1ms operation becomes 10ms**

In these cases, you probably do not care unless you are doing high-frequency 
trading or rendering 60-frames-per-second video games. 

- **A 10ms operation becomes 100ms**

This starts becoming significant if you are running it over and over, e.g.
as part of a script that's called many times or a webserver that's taking
many requests.

- **A 100ms operation becomes 1s**

To a user, that's something turning from "instant" to "noticeable lag". 

- **A 1s operation becomes 10s**

That's something turning from "noticeable lag" to "annoying delay". 

And so on!

If our code is taking 0.1ms out of a batch process that takes 10 minutes to 
run, it's certainly not worth bothering to optimize. If it's taking 9 minutes 
out of the 10 minutes a process takes to run, it's more likely to be worth it.
 
If it's taking 300ms out of the 600ms that our webserver takes to generate a 
response, is it worth it then? As always it depends: how much does your 
response time matter? If you think speeding it up from 600ms to 300ms will
increase profits, then by all means. If it's some internal webpage that 
someone looks at once every-other week, then maybe not.
 
In general, even when performance is *"fast enough"*, you an often benefit 
from parts of your code having *higher* performance: if you don't need the
speed, you can often trade off speed against convenience. "Fast enough" could 
mean *"fast enough, if you're careful"*, but with extra performance it could
be *"fast enough, no need to care at all"* and save you some headache. 

Do you need to design your application to avoid doing redundant work? Do you
think about re-computing things unnecessarily, or computing things and then
throwing them away? If your library is *"fast enough, if you're careful"* then
you'll need to think about those things. If you're library is *"fast enough, no 
need to care at all"*, perhaps your first-pass of redundant, inefficient code 
with tons of throwaway work is totally acceptable! 

### How often is it modified?

Apart from making the code faster, the micro-optimizations described above have
also made it less idiomatic, more verbose, and also harder to extend. For 
example, storing our `Str.State` in a bit-packed `Int` rather than a 
`Map[Category, Attr]` makes it blazing fast, but it also means that:

- Library-users cannot define their own `Attr`s: they have to be known in 
  advance, and fit nicely into the bit-ranges assigned to them

- Similarly, library-users cannot define their own `Category`s: all `Category`s
  must fit nicely into the single 32-bit integer that is available.
  
That is definitely a loss of flexibility and extensibility. Is that acceptable?
In the case of Fansi, it probably is: the Ansi color codes haven't changed much
for decades and are unlikely to start changing quickly now. On the other hand,
if you are writing a business application and the business rules are changing
constantly, then this loss of flexibility is more painful and may not be worth 
it.

### What are the alternatives?

One consideration is that these sorts of micro-optimizations are often "easy"
to apply. You do not need to re-architect your application, implement a 
persistent caching layer, design a novel algorithm, or make use of multiple 
cores for parallelism. These changes can often be made entirely local to a 
small piece of code, leaving the rest of your codebase untouched. 
 
While we claim above that micro-optimizations result in "less idiomatic", 
"more verbose", "harder to extend" or "less maintainable" code, there is a
flip side to it: if you need to implement persistent caching, design novel 
algorithms, or start multi-threading your code for performance, that could
easily add far more complexity and un-maintainability than a few localized
micro-optimizations. 


If you're finding your code compute-bound and you're considering these sorts of
approaches in order to make it run faster, it's worth considering whether you
can achieve the same speedups using a few `System.arraycopy`s and converting
some Maps/Sets to Tries/Bitsets. Maybe you can't, but if you can, it could be
a quick win and may well be enough!

-------------------------------------------------------------------------------

A typical library or application likely won't see the same kind of speedups
that Fansi did for so little work: often the time spent is spread over much
more code and not concentrated in a few loops in a tiny codebase, like the
Fansi benchmarks were. 

Nevertheless, sometimes you find your code is spending a significant amount of 
time in one section, and you want it to spend less. These tools, and others 
like them, can be used to make it run faster:

- [arraycopy and copyOfRange](#arraycopy-and-copyofrange)
- [Local Array Caching](#local-array-caching)
- [Speed through While Loops](#speed-through-while-loops)
- [Tries instead of String-Maps](#tries-instead-of-string-maps)
- [Arrays instead of Int-Maps](#arrays-instead-of-int-maps)
- [Bit Packing](#bit-packing)

While these techniques are often looked down upon in programming circles - 
with attitudes ranging from "the computer is fast enough" to "the [JIT 
compiler](https://en.wikipedia.org/wiki/Just-in-time_compilation) will take 
care of it" - hopefully this post demonstrates that they still can have a 
powerful effect, and deserve a place in your programmer's toolbox. 

What are your favorite micro-optimization tricks you've used in Scala or other
languages? Let everyone know in the comments below!