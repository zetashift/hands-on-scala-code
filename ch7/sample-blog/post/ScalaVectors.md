One oft-repeated fact is that in the Scala language, immutable Vectors are 
implemented using a 32-way trees. This makes many operations take `O(log32(n))` 
time, and given a maximum size of `2^31` elements, it never takes more than `6` 
steps to perform a given operation. They call this "effectively constant" time. 
This fact has found its way into [books](http://tinyurl.com/yc73sutz), 
[blog posts](https://www.47deg.com/blog/adventures-with-scala-collections/#vector), 
[StackOverflow answers](https://stackoverflow.com/a/4442155/871202),
and even the [official Scala documentation].

While this logic sounds good on the surface, it is totally incorrect, and 
taking the logic even one or two steps further illustrates why. This post will 
walk through why such logic is incorrect, explore some of the absurd 
conclusions we can reach if the above logic is taken to be true, and 
demonstrate why Scala's Vector operations are not "effectively constant" time.

[official Scala documentation]: http://docs.scala-lang.org/overviews/collections/concrete-immutable-collection-classes.html#vectors

-------------------------------------------------------------------------------

## How does a Scala Vector work?

A Scala Vector is implemented as a balanced tree with values only at the 
leaves. Except instead of being *binary*, as is commonly seen, the trees used
to implement a Vector are  *32-ary*: each node in the tree contains 32 children 
instead of just 2. Otherwise, operations on a Vector's 32-ary tree work similar 
to how those on a binary tree work. 

Looking up an element at index `i` within the Vector, for example, involves 
starting at the root of the tree, and walking down node by node until you find 
the element you are interested in (There is also some book-keeping you have to
do, but that doesn't change the gist of the approach). A balanced binary tree 
with `n` items is `log2(n)` nodes deep, and so there would be `log2(n)` nodes 
you need to walk to get from the root to the element you are interested in. In 
a Scala Vector, which is a 32-ary tree, there would be `log32(n)` items to 
walk. Hence this "lookup at index" operation is `O(log2(n))` on a balanced 
binary tree, and `O(log32(n))` on a Scala Vector.

Scala's Vectors are immutable, perhaps unlike many binary tree implementations.
This does not affect the lookups described above, since lookups do not mutate
the tree at all. It turns out many other operations on immutable binary trees
are also `O(log2(n))` on the size of tree. Operations like 

- "create a new tree with one element inserted" 
- "create a new tree with one element removed"
- "create a new tree with one element replaced"

All take `O(log2(n))` on an immutable binary tree, e.g. the immutable binary 
AVL-tree described [here](https://justinmchase.com/2011/12/13/immutable-avl-tree-in-c/).
I won't go into detail of why this is the case, but you'll have to take my word
on this. There are lots of resources online going into more detail, such as 
[this blog post](http://mikefroh.blogspot.sg/2011/03/immutable-binary-trees.html)
explaining why inserting into a (simplified) immutable binary tree takes
`O(log2(n))` time.

Since Scala's Vectors are immutable balanced 32-ary trees rather than binary 
trees, all these operations (lookup, insert, remove, replace) take 
`O(log32(n))` time instead of `O(log2(n))` time. Given the max size of a 
machine integer on the JVM (`2^31`, or `2147483648`) that means it never takes
more than 6 steps to perform an operation on the Vector. 

So far, all this is true and uncontroversial.

According to the [official Scala documentation], this makes those operations 
take "effectively constant" time. It is *that* widely repeated claim that is
entirely incorrect, and the rest of this blog post illustrates why.

## O(log32(n)) is O(log2(n)/5) is O(log2(n))

The most straightforward way of seeing that Scala's `O(log32(n))` Vector 
operations are really "logarithmic", `O(log2(n))` by asymptotic Big-O analysis, 
is to reduce one to the other. It turns out that this does not require any 
tricks, just high-school math and the very-basics of Big-O notation.

- `log32(n)` is the same as `logK(n)/logK(32)`, for any arbitrary constant `K`.
  This is simple math, and unrelated to Big-O notation, data-structures, or the
  Scala language.

- Given `K = 2`, `log32(n)` becomes `log2(n)/log2(32)`, which resolves to
  `log2(n)/5`, or `0.2 * log2(n)`.
  
- Now we know Scala's `O(log32(n))` Vector operations are `O(0.2 * log2(n))` 
  Vector operations, we can apply the 
  [Multiplication by a Constant](https://en.wikipedia.org/wiki/Big_O_notation#Multiplication_by_a_constant)
  rule `O(kg)=O(g) if k is non-zero`, with `k = 0.2, g = log2(n)`, and find 
  that Scala's Vector operations are `O(log2(n))`
  
That is all there is to it. None of the rules here are particularly novel,
complex, or exotic. This is something a first-year college student should be
able to figure out as part of their first homework on big-O notation.

And so it turns out, that Scala's `O(log32(n))` Vector operations are simply
logarithmic: they take `O(log(n))` time to perform on a Vector with `n` items.

## Arguments for "Effectively Constant"

Unfortunately, this isn't the end of the discussion: there are many arguments 
that I have seen justifying why Scala Vector operations are "effectively
constant", despite the "obvious" analysis above. I will go into various
arguments I have seen and explain why they're wrong.

### But the real-world performance is good due to cache effects! 

[actual benchmarks]: http://www.lihaoyi.com/post/BenchmarkingScalaCollections.html#vectors-are-ok

One point that often muddles the discussion of the Scala `Vector`'s performance 
is the fact that Scala's Vectors are actually reasonably fast. Now, they are 
not *very* fast, as can be seen in [actual benchmarks],
but overall they're not terrible. Part of this is due to things like 
cache-locality, where the 32-way branching of the tree means that elements at
the leaves are stored in relatively-compact 32-element arrays. This makes
traversing or manipulating them substantially faster than working with binary
trees, where any traversal has to follow a considerable number of pointers 
up-and-down in order to advance a single element.

This improvement in cache locality is a real effect, that can be measured.

However, cache effects and similar things are irrelevant when analyzing the
"asymptotic" [Big-O](https://en.wikipedia.org/wiki/Big_O_notation) performance
of operations on a data structure. In such a scenario, you *only* want to 
analyze the behavior of a data structure in an idealized setting. Similarly,
in [actual benchmarks], the big-O analysis is irrelevant when compared to the
hard numbers of "how fast is my code actually running here?". Neither analysis
is all encompassing, and both are useful in their own way.

The question then is: what are we discussing? Idealized, asymptotic, Big-O 
performance, or the performance of [actual benchmarks]? In the case of the
[official Scala documentation], there is not a single concrete number to be
found, but lots of terms like "Constant", "Log" and "Linear". This is clearly
describing asymptotic Big-O performance, so any cache-effects and similar 
things that would turn up on [actual benchmarks] can-and-should be disregarded.

Similarly, when programmers discuss and compare various data-structures in the
same programming language, without any concrete application benchmark in mind, 
most often it's in terms of the asymptotic Big-O performance of their various 
operations.
 
Now, we *could* be using some heretofore unheard of analysis technique that's
not one of the two described above, but until someone rigorously describes that
technique to me, I have to assume we're using one of the well-known approaches
mentioned here.

We know we're talking about the idealized, asymptotic, Big-O performance 
characteristics of Scala Vectors, both in the official documentation and 
colloquially. While Big-O analysis is no where near the complete picture in a
world with cache-effects and significant constant factors, if we choose it to 
analyze the performance of our data-structures, we must follow the rules for
our analysis to make any sense.

And thus, by the rules of Big-O notation, Scala's `O(log32(n))` Vector 
operations are just `O(log(n))`, not "effectively constant".

### But log32(Int.MaxValue) cannot go above 6!

Given a Scala Vector operation that takes `log32(n)` steps to complete, given 
the max size of a Vector is `2^31` (Scala 32-bit signed integers have `2^32` 
values, from `-2^31-1` to `+2^31`), it will never take more than `6.2` steps to 
complete the operation. 

This is true and uncontroversial.

What is invalid is the claim "6 is a constant, therefore the runtime of the 
operation is effectively constant".

This reasoning falls apart when you consider the 
[formal definition](https://en.wikipedia.org/wiki/Big_O_notation#Formal_definition)
of Big-O notation:

> Let f and g be two functions defined on some subset of the real numbers. One 
> writes 
> 
> `f(x) = O(g(x)) as x -> infinity` 
>
> if and only if there is a positive constant `M` such that for all 
> sufficiently large values of `x`, the absolute value of `f(x)` is at most `M` 
> multiplied by the absolute value of `g(x)`

Note that it is defined for `x -> infinity`. Not `x -> 2^31`, or `x -> 31337`, 
but `x -> infinity`. Thus, the fact that a Scala Vector running on the JVM uses
machine-integers that can't go above `2^31`, is entirely irrelevant to Big-O 
notation. And as mentioned above, until someone formally-defines a rigorous new
analysis approach to replace Big-O notation or actual-benchmarks, those two are 
the only tools we have to analyze these things.

## Reductio Ad Absurdum

While one way of disproving an argument is to find a flaw in the logic, another 
common approach is to use the same logic to prove something absurd that cannot
possibly be correct. Since we know the end-result is false, and we reached that
end-result using our argument, we therefore know that there must be a flaw in
our argument even if we do not know exactly what the flaw is.

It turns out, using the same arguments that "show" `log32(n)` is "effectively
constant", we can prove all sorts of absurd results. This section will explore
a selection of them.

### What if the max size of input mattered? 

[Earlier on](#but-the-real-world-performance-is-good-due-to-cache-effects), I 
had shown that we have to be discussing aymptotic, Big-O
complexity when considering Vector operation performance, and showed that the
"`log32(n)` cannot go above `6.2`" logic used to shown they were "effectively 
constant" was 
[invalid given the definition of asymptotic Big-O complexity](#but-log32intmaxvalue-cannot-go-above-6). 
But let's say we ran a thought experiment: imagine if we *could*
perform asymptotic, Big-O analyses with a maximum size of input, e.g. 
`n < 2^31`? What then? 

While it is true that `log32(n)` will not go above a certain constant given the
size limits on a JVM machine integer, there are many other rates of growth 
which also do not grow above a certain constant given the same size limits!

It turns out that given a limit of `n < 2^31`:
 
- Any `O(log2(n))` operation will complete in no more than `32` steps
- Any `O(n)` operation will complete in no more than `2147483648` steps. 

`32` and `2147483648` are just as constant as `6.2` is. So why can't all my 
`O(n)` and `O(log2(n))` operations running on JVM machine-integers be called 
"effectively constant" too?
 
Even if you try and limit the reasoning to "`6` is a small constant", what 
defines "small"? Perhaps `2147483648` doesn't count as "small", but I think
`31` is pretty small.

For this argument to be valid, there needs to be *some* reason why `f(x) < 6.2`
is enough to call `f` "effectively constant", but `f(x) < 31` isn't. Or, we 
need to accept that `O(log2(x))` is *also* "effectively constant", a claim I
think many would agree is absurd. If we aren't able to provide the former
and unwilling to accept the latter, we have to accept that this "`f(x)` cannot 
go above `6.2` for `n` limited to JVM machine-integers" logic is bunk.

### What if constant factors mattered?

[Earlier on](#olog32n-is-olog2n5-is-olog2n), I had shown that given the 
definition of Big-O complexity, constant factors in performance *are entirely
irrelevant*. Given that `O(log32(n))` is equivalent to `O(0.2 * log2(n))`, that
is then equivalent to `O(log2(n))` by the rules of Big-O analysis.

However, we could run a thought experiment: what if constant factors *did* 
matter in Big-O analysis, and could shift the balance between "logarithmic"
and "effectively constant"?

It turns out that given:

- `O(log2(n))` algorithms are "logarithmic"
- `O(log32(n))`, or `O(0.2 * log2(n))`, algorithms are "effectively constant"

You can then "prove" all sorts of crazy things!

Let's consider a single `O(0.2 * log2(n))` Vector lookup to be
"effectively constant". However what if we then perform *five* vector lookups?

Given that five Vector lookups takes 5 times as long as a single Vector lookup,
the combined Big-O complexity of the five lookups is `O(5 * 0.2 * log2(n))` 
which is `O(log2(n))`, which is "logarithmic". 

Now, we've shown that a single Vector lookup is "effectively constant", but 
five consecutive Vector lookups is "logarithmic"! This is clearly absurd.

### What if constant factors mattered? Part deux

Taking this logic further, let us imagine we have an `O(log2(n))` algorithm, 
say a binary search, implemented in say [Python](https://www.python.org/). At a 
first approximation, CPU-bound Python code runs about ~40x slower than the 
equivalent Java or Scala code. If we've decided that constant factors matter, 
we could say that the same algorithm implemented in Java would be 
`O(0.025 * log2(n))`.

Given that we've already said that `O(log2(n))` is "logarithmic" and 
`O(0.2 * log2(n))` is "effectively constant", where does that leave 
`O(0.025 * log2(n))`? At the very least, we'd have to say that it's *better*
that "effectively constant". 

You can count the "time" taken to run an algorithm any number of ways: 
wall-clock, CPU-time, assembly instructions, etc.. Whatever X you use, you'll 
still find that the algorithm in Java uses ~40x less Xs than the same algorithm 
in Python.

And thus, we have shown that a "logarithmic" algorithm implemented in Python,
becomes "effectively constant" when implemented in Java. Clearly an absurd 
conclusion, which illustrates precisely why constant factors are not considered
in Big-O analysis.

## Conclusion

Scala's immutable Vector operations (as well as the related Map and Set 
operations) are logarithmic: the time they take to execute grows 
logarithmically with the size of the Vector. We have seen that this can be 
proved by high-school maths and the basics of Big-O notation. What's more, if 
we accept the logic arguing that Scala Vector operations *are* "effectively 
constant" time, we could then prove that: 

- ["Logarithmic" operations are effectively constant 
  too!](#what-if-the-max-size-of-input-mattered)
- [Running 5 "effectively constant" operations in a row becomes 
  "logarithmic"](#what-if-constant-factors-mattered)
- [Translating a "logarithmic" algorithm from Python to Java makes it 
  "effectively constant"](#what-if-constant-factors-mattered-part-deux)

None of which make any sense.

The basic problem here is that "effectively constant" is not a *thing* with a
rigorous definition and broadly-accepted meaning.

"Big-O" notation is a thing, as is Big-O constant time `O(1)`. Small-O, 
Big-Omega, Small-Omega, Big-Theta notations, while less common, are well-known 
and have rigorously defined meanings. "Amortized `O(1)`", "`O(1)` assuming 
integer operations are all `O(1)`", or "`O(1)` expected but `O(n)` worst-case"
are all well-defined properties that people throughout the 
computer-science/software-engineering communities can understand.

Coming from an entirely different angle, "100ns per operation with a 1,000,000 
item collection" or "Operation takes 10 CPU cycles per input element" are also 
*things*: useful, rigorous, and broadly understood.

"effectively constant given a maximum size of input" does not have a rigorous, 
useful definition. The definitions I have seen ascribed to it are full of 
holes, and lead to the absurd conclusions shown above. This is not surprising,
given the first line of the formal definition states `as x -> infinity`.

It feels good to come up with novel data-structures that in practice fare 
better that those that came before. It also feels good to optimize them to work
well with the underlying hardware. However, we should not abuse 
well-established notation just because we think "we know better". 

If we think there's a better approach for analyzing algorithm performance, we 
should rigorously define it so it can be properly discussed, and ensure we have
satisfactory answers to all the holes described above. Until then, we should
toe the line and accept that Scala's Vector operations are all `O(log(n))`:
their execution time is logarithmic in the number of items involved.