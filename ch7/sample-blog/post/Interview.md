Any software engineer who has ever looked for a job has had the interview
experience: being locked in a small room for an hour, asked to write code to
solve some arbitrary programming task, while being grilled by one or two
interviewers whether or why the code they've written is correct.

You can find lots of material online about how to ace the interview as an
*interviewee*, but very little has been written about how to conduct one as the
*interviewer*. This post will lay out a coherent set of principles and cover
many concrete steps you can use to make the most out of the next time you find
yourself interviewing a potential software-engineering hire.

-------------------------------------------------------------------------------

When recruiting software engineers, you typically conduct a whole battery of
interviews to try and decide if a candidate is someone you would want to work
with. While the line-up of interviews may change, there's almost always a
programming interview: writing code on a whiteboard/computer, to solve some
arbitrary problem, to answer the question "can this person actually write
working code?".

In my work, I have conducted over a hundred programming interviews of all sorts,
to candidates of all backgrounds: from new-graduates fresh out of school (or
even those still *in* school!) to experienced engineers with decades more work
experience than I have. I have driven changes in my org's interview process,
such as introducing hands-on coding interviews where the candidate has to
execute and debug their code on a laptop rather than just waving their hands at
a whiteboard.

I have also *been* interviewed by more than 20 different companies over the
years in the course of looking for jobs, for positions ranging from "intern" to
"principal engineer". I have seen questions ranging from *"fix this bug in an
obscure open-source library"* to *"are the words *desert* and *dessert*
synonyms, antonyms, or unrelated?"*

It's easy to be thrown in a room and asked to interview a candidate. It's not so
easy to conduct a *good* interview - getting a good read on a candidate's
abilities and weaknesses - and to be able to do so repeatedly and reliably. This
post will cover some principles and techniques you can use to make the most of
the limited time you have together with a candidate, so you can make a firm
decision whether or not to give the candidate a job offer to work with you in
your company or on your team.

## Goals

While there are a variety of ways you can conduct a programming interview, the
goals typically are the same:

- **Will the candidate be able to write working code if they join the team?**

- **Can the candidate discuss code and problems with the people they'll be
  working with?**

- **Can the candidate reason about arbitrary problems and constraints?**

- **Is the candidate someone we would enjoy working with?**

There are a lot of more tactical goals that you can try to accomplish, along
with specific skills you may want to test (e.g. knowledge of a specific platform
or programming language) but at a high level these are the primary questions you
are trying to answer in a programming interview. There are other interviews
(e.g. resume review, architecture design, etc.) that cover other things you'd
want to know before hiring the fellow.

While you usually give the candidate a programming task to work on during the
interview, it is the *process* through which a candidate finds a solution that
reveals the most about whether they can write code. As an interviewer, you
should end up with a much more holistic view of the candidate than "finished
task X in Y minutes":

- Everyone makes mistakes. Were they stupid mistakes, or were they mistakes that
  any reasonable person would make? Do they indicate sloppiness on the part of
  the candidate, or just unfamiliarity with the problem? How fast were they able
  to recover from them?

- When the candidate makes a decision that turns out to be correct, were they
  just lucky or did they have a solid reason for doing so?

- Was the candidate able to discuss and convey what they were trying to do, in a
  way you could understand?

- Did the candidate have a good grasp of their own code, and be able to smoothly
  make changes when necessary? Or did they get lost in their code, tangled up in
  their own logic totally independent from the task they were trying to solve?

From these observations, you should be able to come up with answers to the four
questions posed above, and then decide whether you'd call this candidate a
"hire" or "no hire"


## Tactical Goals

In order to achieve those high-level goals, there are some intermediate targets
in the design of an interview that help you get there:

### Realism

**We want to test the candidate's ability to perform in something similar to the
environment they'd be working in**. The challenges of the interview should be
challenges that are likely to appear in their day-to-day work environment.

Things like *"refactoring code on a whiteboard, without the ability to insert
text or copy-paste"* are exceedingly challenging, but don't appear in real work.
Neither do things like *"remembering the API of the `heapq` module without
looking up documentation"*.

Being able to refactor code they've already written to twist it in a new
direction, or debugging code that is currently spitting out stack traces, is a
core skill for any software engineer that very often programming interviews fail
to test for.

Not everything that is challenging is realistic, and it is easy for
challenging-but-silly requirements to slip in accidentally. You need to make a
conscious effort to make sure they do not become the focus of the programming
interview, while simultaneously spending some time focusing on the skills that
are most relevant in their day-to-day work should they get hired.

### Set them up for Success

**You want the programming task given to be challenging, but everything else
should be lined up to help the candidate succeed.**

The candidate should not need to deal with vague instructions, unclear
expectations, or the mind-games of trying to guess "what does the interviewer
*actually* want". They shouldn't have to deal with antagonistic or hostile
personalities.


If the real work environment is a good place to work, the interview environment
should be too.

You want the candidate to be able to do their best. If you turn down a
candidate, you want to be 100% sure you turned them down because, despite all
efforts, they weren't able to perform. You don't want to end up hesitating,
unsure whether a candidate did badly only because the interviewer was a jerk or
the task given was vague and unclear.

Lastly, you want every candidate to have a good experience. Even if you don't
want to hire them, they probably have friends you *would* want to hire! Word
spreads, and remember that during an interview, the candidate is interviewing
your company as well.

### Maximize candidate-work time

**You want to maximize the time you can watch the candidate working**: writing
code, tracking down bugs, discussing designs. This is your chance to judge their
abilities and decide if they are capable or not.

A lot of other things often happen in interviews: you explain the task to them
and make sure they understand it. You interrupt the candidate to correct them
if/when they veer off course.

These are things that are necessary, but they are not the point of the
interview, so you should keep them to the minimum necessary. Discussion is
great, but time spent elaborating on vague & poorly delivered instructions
doesn't really count as "discussion", and neither does repeated, annoying
interruptions.

The time saved by minimizing these things can be given to the candidate so they
has a chance to show you their stuff.

## Limitations of the Programming Interview

Now that we've laid out some of the high-level and tactical goals for why we are
conducting these programming interviews, it's worth spending a moment to
consider the constraints of the interviews you will be conducting, and some
explicit non-goals: things that we are *not* trying to achieve.


### Constraints

#### Random Chance

**There will always be randomness in the programming interview, but it is
possible to control and mitigate it.**

Let's imagine you give the candidate a problem to solve, and they finish it
relatively quickly and without difficulty. That could mean any of the following
things:

- Candidate is a strong performer who was able to put together their existing
  programming knowledge to solve the task at hand

- Candidate is brilliant but solved the problem from first principles,
  re-deriving decades worth of academic work in the minutes they spent working
  on it

- Candidate has seen this exact problem before, and regurgitated the solution
  they already knew

- Candidate hasn't seen this exact problem, but *has* seen a problem similar to
  it, and managed to piece together a similar solution that is correct

- Candidate hasn't seen any related interview problems, but happens to have an
  interest in the field related to the problem domain (Dynamic programming?
  Tries? Database implementation details?)

- Candidate hasn't seen the problem or field at all, but of the 3 different
  approaches that seemed plausible up-front they happened to pick the correct
  one entirely arbitrarily

It is impossible to pick a set of problems that every candidate is equally
unfamiliar with. And if a candidate finds a solution quickly, it is very
difficult to distinguish the various cases above.

However, there *are* things you can do that make it less random:

- Rather than rigidly sticking to a script hoping that it is entirely
  objective, put in the effort to get a deep and nuanced subjective view of the
  candidate.

- Design tasks with multiple possible approaches, so it matters less which
  approach the candidate picks up-front.

- Testing broad things like "can they write,
  discuss, analyze and refactor code" rather than narrow things like "can they
  remember the Dynamic Programming solution to the
  [Longest Common Subsequence problem](https://en.wikipedia.org/wiki/Longest_common_subsequence_problem)".

Through things like this, you can actively work to mitigate the effect of random
chance in your interviews, even if you cannot entirely eliminate it.

#### Time constraints

**Programming interviews tend to be anywhere from 40 minutes to an hour long.
This is unlikely to change.**

I have heard everyone wish they had more time in the interview process:
interviewees sometimes wish they had a longer/more-realistic time frame to
showcase their skills, while interviewers often lament the short and unrealistic
programming tasks they are forced to give candidates to fit within the allotted
time slots.

Perhaps in an "ideal interview process", the candidate would be able to spend
more time with engineers from the company they are interviewing with. In
practice, this quickly bumps up against reality:

- The candidate often *already has a job*, and asking for 1 day off to go
  interview somewhere is already hard, nevermind asking for more

- The candidates are often interviewing at *multiple* companies, not just one

- The poor engineers *conducting* the interviews *also* have full time jobs!
  Asking for more time-per-interview or more interviews-per-engineer means less
  time for your engineers to do the actual engineering work you hired them for

There are other things you can do to try and get signal:

- You can often try and get extra signal through homework problems, in the end
  these have the same heavy time-cost for a candidate to submit, and for an
  engineer on your team to evaluate

- You could hire people you already know from previous jobs, but that's a pretty
  limited pool to hire from.

- You could try and look at a candidates open-source code, but not every
  candidate is willing or able to spend time on open-source contributions (There
  are well-known silicon-valley tech companies that prohibit employees from
  doing open-source work!)

In the end, I think there's no silver bullet to getting a good read on a
candidate. Time is limited, and we have to make the best of it to learn as much
about the candidate as we can.

### Non-goals

#### Successfully implementing a specific algorithm

While most often you give the candidate some kind of algorithmic task to work
through, **the goal for you-the-interviewer is *not* to try to get the candidate
to finish and correctly solve the given problem!**

**The programming interview is like a treadmill: the point isn't to go anywhere
in particular. The point is just to keep the candidate running** so you can watch
and analyze their technique. Due to [random chance](#random-chance), how long a
candidate takes to complete an implementation often has no bearing on how good
they are.

Giving the candidate a task to solve is purely so you have a chance to watch the
candidate work: it is watching them work that you can then make a decision
whether or not this is a person who can write code and effectively reason about
and discuss problems and constraints.

While stronger candidates *do* tend to on-average be able to accomplish more of
the tasks that you set before them, and do so quicker, I nevertheless do not
think trying to get a candidate to complete a task correctly is a goal that is
worth striving towards.

#### Making life difficult for the candidate

**It is common, not just in programming interviews, to make life difficult for
the candidate. I think this idea is rubbish.**

Things like being vague in what the candidate is meant to do,
interrupting them while they are working, and generally being confrontational. I
have seen this justified under the idea that a candidate "needs to be able to
work under pressure", and thus any additional pressures you can give during an
interview setting are, if not *good* at least *acceptable*.

The programming interview isn't a military boot-camp where you stress people to
make the weak-willed candidates drop out; your goal is to try and get a glimpse
of how the candidate thinks and works through programming problems in a
realistic setting.

Hopefully in your realistic setting, the challenge comes from the difficult
tasks you are given to solve, not some jerk that's sniping at you from the
sidelines. You want to find candidates who would do well in the setting you'll
be working with them in, not candidates who do well under hazing.

#### Making life difficult for the candidate, part two

On a related note, **it is *not* the sole purpose of the interview to pose
extremely difficult or challenging problems**, even in the absence of unrelated
difficulties. While this is something you often end up doing, remember that your
[goal](#goals) is to get a good read of the candidate's abilities, and the
difficulty of the given task is only useful so far as it helps achieve that
goal.

There are multiple ways difficult problems can go against getting a good read on
a candidate's abilities:

- The problem is so difficult that most candidates make no progress. If all
  weak, mediocre, and strong candidates all get equally stuck, you do not get a
  chance to see how the candidate works, nor can you differentiate them based on
  how much progress they have made (since they all made none)

- The problem has a very high degree of [random chance](#random-chance). Thus
  whether a candidate makes progress or not is simply random and does not reflect
  whether or not they're any good.

One good example I have found of the second problem is interviews which make a
candidate dive into an existing large codebase and make some changes.

These interviews are extremely [realistic](#realism), since diving into large
codebases is what you do day-in and day-out as a software engineer, and they are
also very challenging. But I have personally found that with such tasks, it is
often essentially random how long it takes a candidate to find what they need.
That does not give you a good signal on whether the candidate is any good.

#### Nitpicking

**A programming interview is not the place to nit-pick over minor details**.
This is simply due to the format: with less than 60 minutes to work on a task
you have never seen before, often in an environment without editors or tools the
candidate is comfortable with.

In a real work setting, they would have days or weeks to get comfortable with
the programming environment, coding and style conventions. They will have lint
rules, auto-formatters, code-review, a compiler, and many other things to make
sure they doesn't forget the small stuff.

During a programming interview, none of those things are available.

Whether or they can keep track of minutiae in an interview reflects not-at-all
on whether they can keep things neat in day-to-day work. Thus you should let
small things slide:

- Forgetting semi-colons
- Mixing up `camelCase` vs `snake_case`
- Un-descriptive variable names
- Forgetting if `java.util.Map#get` returns `null` or throws an exception if the
  item is not found

During day-to-day work, you should be as strict about these things as possible:
standardizing small details like this is core to the long-term health of a
codebase. But during an interview, in an environment without proper tools,
trying to be strict accomplishes nothing.


## Before the interview

The work needed to conduct a good programming interview starts long before the
candidate steps into the room. Here are some things to keep in mind when you are
picking and preparing a programming task which you can then use to stretch and
challenge interview candidates for months or years to come.

### Pick messy, open-ended tasks

It is very common for interviewers to give programming challenges which are
based on neat, elegant little algorithms, data-structures or techniques.

- Longest increasing subsequence ([dynamic programming](https://en.wikipedia.org/wiki/Dynamic_programming))
- Shortest path on a weighted graph ([djikstra's algorithm](https://en.wikipedia.org/wiki/Dijkstra%27s_algorithm))
- Sort an array in worst-case `O(n log n)` time ([mergesort](https://en.wikipedia.org/wiki/Merge_sort))
- Filtering words based on a prefix ([trie](https://en.wikipedia.org/wiki/Trie))
- LRU cache ([mutable doubly-linked list](https://en.wikipedia.org/wiki/Doubly_linked_list))

**These elegant problems make for poor interview questions.**

I have found that some of the best problems are messy and without a single,
provably-optimal solution.

The problem with many tasks such as that have a single, specific solution is
that it is often [random](#random-chance) how easy it is for a candidate to find
that one solution. A candidate who is able to regurgitate a flawless Merge-sort
implementation from memory doesn't tell you anything about if they are any good!

Another shortcoming of these problems is that there isn't much freedom designing
a solution: one "correct" solution looks the same as any other: after all, how
many ways can you implement a [Trie](https://en.wikipedia.org/wiki/Trie)? You
thus miss out on seeing how the candidate writes code when they have the freedom
to design things and make choices.

Specific algorithms make great *practice problems* because you can write
code and validate it for correctness yourself without a real interviewer sitting
there, but that doesn't matter at all for someone *conducting* an interview.

Open-ended questions, on the other hand, often have multiple solutions, and a
candidate doesn't need to have studied any specific topic to put together a good
solution. For example:

- If the task is to write a string templating engine which processes
  `{{interpolated}}` snippets within a template string, there are multiple ways
  you could do it: using `String.split`, `String.indexOf`, `String.replace`,
  parsing character-by-character in a `while`-loop, using regular-expressions.
  You can build an abstract syntax-tree, or you could render directly to an
  output string.

- If the task is to implement a parallel web-scraper, you could write a working
  solution with any of the following tools: multithreading, an asynchronous
  event-loop, using Futures/Promises, or using Actors. The parallelism could be
  fork-join parallelism, or entirely asynchronous.

- If the task is to make a tiny interactive website using Javascript/HTML/CSS
  (e.g. a task for front-end engineering candidates), there typically isn't any
  single algorithmic challenge to solve: instead there are tons of small
  decisions to make in order to make the website work, and countless ways in to
  implement it which could all work (and countless ways to screw it up)

In these cases, it matters a lot less whether you are familiar with any specific
algorithm or data-structure. The techniques you need to solve these problems are
a lot more ubiquitous, and you have a lot more *different* ways of approaching
the problem. This means:

- The candidate doesn't have to "get lucky" to choose/study-before-hand the
  single approach that yields an optimal solution

- The interviewer can get a much deeper understanding of how the candidate
  writes code when they have the freedom to pick a solution and run with it

Note that this doesn't make the problem any less difficult! There are still
plenty of opportunities for a candidate to screw up, to get tangled up in their
own code, or to demonstrate their fluency and competence. Even if the task
proves to be too simple, such problems can easily be extended into
[multi-stage tasks](#prepare-multi-stage-tasks) to keep things challenging.

What's important is that open-ended sorts of tasks try and minimize the
advantage/disadvantage a candidate will get from mostly
[random chance](#random-chance) decisions: knowing a specific algorithm or
arbitrarily picking a particular approach. Rather than having a single solution
that the candidate either gets or doesn't, such questions should allow any
candidate to start working and iterating towards a good solution, giving the
interviewer lots of time to see them work.

### Prepare multi-stage tasks


**You should design your tasks such that they are broken down into multiple
related stages.**

Earlier, I mentioned that it is a non-goal to get the candidate to
[successfully implement a specific algorithm](#successfully-implementing-a-specific-algorithm).
Taken to the extreme, ideally you should be able to keep a candidate working
hard for the full duration of the interview, *no matter how good they are*.
Obviously, if you have a single task some candidates will finish it earlier and
some later. The solution, then, is to have a multi-stage task, comprised of many
steps each dependent on another.

For example, a simple task might be:

> Implement an arithmetic expression parser that can evaluate strings like
> `(1+2)*(3+(4*5))` to `69`. Assume every binary operation `a+b` must be wrapped
> in a set of parens `(a+b)`

That may take someone anywhere from 10 minutes to 30 minutes to complete. Maybe
someone who has seen similar problems before will be able to directly spit out a
working solution in 5 minutes. What then?

If someone finishes it early, you should have follow-on task after follow-on
task to keep them working hard, so you can watch them continue working.

Perhaps:

> Allow multiple operations within a single set of parens, or multiple sets of
> wrapping parens, allowing for operator precedence (`*` and `/` are higher
> precedence than `+` and `-`) e.g. evaluating `((1+2))*(3+4*5)` into `69`

Then:

> Extend it to allow variables in the expression, e.g. evaluating
> `(y+2)*(3+x*5)` into a object `f`, and later call `f(1,4)` to return `69`

And then:

> Limiting the only operations to `+` and `*`, extend your code to expand the
> symbolic expression, e.g. converting `(y+2)*(3+x*5)` into `5yx + 3y + 10x + 6`

**Remember, the programming interview is like a treadmill: the point isn't to go
anywhere in particular. The point is just to keep the candidate running**.

Having a sequence of multiple related tasks like this helps you mitigate the
effect of [random chance](#random-chance): even if they've seen your initial
task before, they're unlikely to have seen *all* the follow-ups.

Compared to having multiple unrelated tasks, or a single task with all
requirements provided up-front, having multiple related tasks with additional
requirements *intentionally not provided up front* forces the candidate to
repeatedly refactor their old code to make it do a new thing. This is usually a
fascinating process, and teaches you more about how a candidate works than how
fast they can spit out the code to reverse a binary tree.

Lastly, having multi-stage tasks results in having lots of natural stopping
points: one after each stage. Those are natural points for you to interject for
discussion, or to wrap up the interview seamlessly if time is running short.


### Test your programming task

**Whatever task you end up giving to a candidate, should first be tested on one,
or preferably *more than one*, of your colleagues.**

By "tested", I mean that you should sit down with your colleague(s) in a room,
get them to do the interview task under interview time-constraints, interacting
with them as you would in an interview setting. Whatever solution they came up
with, how it long it took for them to complete various stages of the problem,
and any other notable facts should all be written down and tabulated in a
document somewhere that any interviewer administering this question will have
access to.

What does testing the interview task before-hand give you?

- It calibrates you to how difficult the task is. You doing a task you came up
  with yourself will *always* be easier than someone doing an unfamiliar task
  under interview pressure. Testing the task on colleagues helps ensure you have
  reasonable and realistic expectations of how much can get done in 60 minutes.

- It flushes out any bugs in the problem statement: is some part of the problem
  vaguely defined? Are there typos in the example input/output data you provide?
  Is the answer you thought was optimal, incorrect? Is there some unstated
  assumption that's not in the problem statement, but necessary to putting
  together a solution?

- It reveals different approaches and solutions to the problem. If you
  [pick open-ended tasks](#pick-messy-open-ended-tasks), there will be more than
  one solution, and the fresh pairs of eyes provided by your colleagues are more
  likely to discover them. When a candidate comes up with an unusual solution,
  you will be better able to judge at-a-glance where that solution fits in to
  the set of solutions you already know, and whether or not it's correct.

- It exposes your colleagues to the task; this has the dual benefit of expanding
  the repertoire of interview questions that *they* can ask, as well as
  calibrating *your colleagues* so that when you discuss a candidate's
  performance, they are already familiar with what the difficulties and
  challenges of a particular problem is.

Testing an interview task is somewhat tedious and annoying: rather than simply
asking the question when a candidate comes in, you now have to ahead-of-time
budget 1-2 hours with your colleagues to administer the question to them and
discuss/tabulate the results after.

Nevertheless, you will probably conduct many interviews over the course of your
career, and will no-doubt re-use the same interview questions many times. Thus
this relatively-small up-front investment is definitely worth it for the many
ways it helps you make the most out of the your limited time with a candidate in
the interview room.

## Starting the Interview

You've done all your prep work. You're now waiting in the interview room, and
someone has noticed the candidate hanging around the front door and brought them
in.

Even now, as the interview is just starting, there are things you can screw up.
Here are some things to keep in mind during the first few minutes of your
hour-long slot:

### Map out the structure of the interview before starting

**After introductions, the first thing that you should do is go over the
structure of the interview.**

One common layout I've seen is for 60-minute interview slots is:

- 5 minutes buffer: in case the candidate is late, previous interview runs over
  time, bathroom breaks, or simply letting the candidate relax and decompress
- 5 minutes of resume discussion
- 45 minutes of coding
- 5 minutes to ask the interview(s) questions


This can obviously vary with the overall length of the interview and personal
preference. Regardless of what layout you choose, you should state it
explicitly. This helps the candidate:

- Manage expectations: I have gone into interviews not knowing how long the
  interview is meant to be, and having it take longer or shorter than I
  expected. Nothing throws you off balance like being told *"you thought it was
  time for lunch, but actually we have another 30 minutes of follow-up tasks for
  you"*

- Budget time: a 15-minute resume discussion looks a lot different from a
  5-minute resume discussion, and a 45-minute programming solution looks a lot
  different from a 20-minute programming solution.

Overall, it helps [set them up for success](#set-them-up-for-success), since the
candidate can feel secure and produce their best work, and is definitely worth
the 15 seconds or so it takes to deliver to them.


### Clearly separate what is expected from what is provided

**You need to make sure you are clear what you are meant to provide the
candidate, and what the candidate is expected to produce in return.**

In many problems you'll need to provide back-story, constraints, requirements,
or stub functions and APIs for the candidate to make use of. While *sometimes*
you can get away with asking the candidate e.g. "what functions do you think
you'll need" or "what do you think the requirements would be", taken too far
this just results in a confused/frustrated candidate and a confused/frustrated
interviewer unable to make progress.

Conversations like:

> **Candidate**: This solution takes `O(log n)` time per operation
>
> **Interviewer**: Do you think that's fast enough?
>
> **Candidate**: Yeah it seems fast enough for me
>
> **Interviewer**: Would you be happy deploying this to production?
>
> **Candidate**: Since you haven't told me how important production is, sure why
> not

In real work, there will be external context and limitations on the system,
timelines and budgets, and stakeholders who will have requirements that need to
be met. In an interview settings, none of that exists, so it is up to the
interviewer to provide it.

A candidate trying to propose a solution without being provided the necessary
constraints and requirements is like an engineer trying to build a system
without knowledge of the system's requirements or access to stakeholders: a
frustrating, useless waste of time that's sure to end in disaster.

*If you ask the candidate the define the requirements for the task you're going
to give them, you can't be surprised if the candidate imagines a use case with
entirely different requirements from what you expected!*

While it's good to have discussions with the candidate to learn how they think,
you as-the-interviewer need to have a crystal-clear understanding of what it is
you want the candidate to deliver and what it is you are willing to provide. No
matter how capable a candidate is, they cannot deliver something if you don't
provide the context/constraints/requirements necessary.

Once that is clear, you will always be able to drive the discussion towards a
clear destination: either you providing something to the candidate, or the
candidate delivering something to you. This clarity, for both the interviewer
and the candidate, goes a long way to keeping the interview flowing smoothly and
allowing focus on the task at hand.

### Provide the task description printed out on a piece of paper

**Before an interview, you should just type out the problem statement, and hand
them the piece of paper**. This should contain everything they need: any
background info, the problem description, example inputs and outputs. Something
like:

> Your task, should you choose to accept it, is to implement a LRU cache.
>
> LRU caches are often used to implement caches which you do not want to grow
> indefinitely. The cache has a max size, and when a new key is inserted that
> would make it grow larger than the max size, the key which has not been
> accessed for the longest time is removed to make space.
>
> It should support these operations:
>
> - `get(key)`: get the value of the key if the key exists in the cache
> - `put(key, value)`: update or insert the value if the key is not already
>   present. When the cache reached its capacity, it should invalidate the least
>   recently used item before inserting a new item.
>
> What happens if you call `get(key)` on a key which doesn't exist in the cache
> is up to you to define.
>
> Example usage:
>
> ```js
> cache = new LruCache(2) // capacity = 2
>
> cache.put(1, "1")
> cache.put(2, "2")
> cache.get(1)          // returns "1"
> cache.put(3, "3")     // evicts key 2, because the key 1 was retrieved
> cache.get(2)          // returns null, because 2 was just evicted
> cache.put(4, "4")     // evicts key 1,
> cache.get(1)          // returns null, because it was evicted earlier
> cache.get(3)          // returns "3"
> cache.get(4)          // returns "4"
> ```


If there are any questions, then the interviewer can answer them.

This accomplishes a lot of things:

- [Maximize candidate-work time](#maximize-candidate-work-time): cutting short
  the initial delivery and clarifications and letting them go straight to work

- [Clearly separate what is expected from what is provided](#clearly-separate-what-is-expected-from-what-is-provided):
  the paper problem statement should be self-contained and detail exactly what
  the candidate is provided, and what they are meant to do

- [Sets them up for success](#set-them-up-for-success): the candidate can refer
  to it as the interview progresses to keep themselves on track.

- Avoids mistakes *by the interviewer*, who could easily mis-state the problem,
  provide buggy examples, or forget some detail

The explanation of the problem statement at the start of the interview is
basically boilerplate: tedious, repetitive, but *very important*, and easy to
get wrong and cause frustration or confusion. "How well a candidate clarifies
poorly stated requirements" is certainly something you could test for, but isn't
generally one of of the main [goals](#goals) of the programming interview.

A standard printout ensures a basic level of consistency between candidates
(they all got the exact same problem), and allows the candidate to quickly
understand the basic problem they need to solve so they can move on to
higher-level discussion and implementation. It is that discussion and
implementation that is the real meat of the programming interview.

## During the Interview

The interview is now well under way: you have 45 minutes to try and figure out
if this candidate is someone you are willing to hire and work with on your team.

Here are some tips to optimize the main body of the interview, to try and
maximize the signal you get out of your limited time slot so you can be clear
and confident in your final yes/no judgement.

### Write code on a computer, not a whiteboard

**As an interviewer you should default to getting the candidate to type out
their solution, whether they want to or not**. Many programming interviews still
take place on a whiteboard. As a candidate, if you ask, they'll often let you
type it out on a computer, but this is inadequate: the computer should be the
default.

Writing code on a whiteboard always ends in a mess that confuses everybody,
benefits nobody and is an pain in the neck to read. There are many strange
things that happen when you start writing code on a whiteboard:

- You start using really short variable names, since writing on a whiteboard is
  far slower than typing on a keyboard.

- You become loathe to refactor your code. While I stated earlier that
  programming interviews are not the place for [nitpicking](#nitpicking), it
  turns out that people typing out code in a text editor are perfectly happy to
  tidy-up and re-organize things to maintain a minimal level of neatness. On a
  whiteboard, not so much.

- You start drawing arrows all over the place to indicate the edits you wish you
  could do, but are reluctant to erase-and-rewrite large blobs of code to make
  it happen. Even something as simple as "insert some code here" often involves
  arrows being drawn, because trying to make space to insert it properly could
  require erasing-and-rewriting a dozen or more lines.

While these things take up time, none of them are relevant at all to what the
candidate will be expected to do day-to-day if they get hired. Nobody spends
time writing and refactoring large blobs of code on a whiteboard in their day to
day work.

To maximize [realism](#realism) and
[maximize candidate-work time](#maximize-candidate-work-time), you should make
the candidate write code on a computer with all the modern conveniences of
"insert text at line" and "copy-paste", or even things like syntax highlighting.
While it's obviously ideal if the candidate can write code on their own machine
in an environment they're familiar with, *any* computer with a text editor is
already a huge step up in efficiency, clarity and realism.

You can keep a whiteboard around, for drawing diagrams and such that text
editors do not do well. They just shouldn't be the default place to write, edit
and discuss chunks of code.

### Make the candidate run and debug their code

**You should, if possible, make the candidate execute and debug the code they
write on a computer.**

Of the places I've interviewed with, only a small minority of them by-default
expected you to execute-and-debug the code you wrote; the vast majority of
interviewers just wanted to inspect the code themselves for correctness. I think
this is the wrong default.

Most people who have done a programming interview (either as interviewer or
interviewee) would have an experience like:

> **Interviewer**: I see a bug in your code
>
> **Candidate**: It looks correct to me
>
> **Interviewer**: Are you sure?
>
> **Candidate**: Yes
>
> **Interviewer**: What if you put in this input?
>
> **Candidate**: It looks like it does the right thing
>
> **Interviewer**: The bug I see is in *this* function `f`. Do you see it?
>
> **Candidate**: No, it looks correct

This back-and-forth can take forever. Sometimes the interviewer is right,
sometimes the candidate is right. Always though, this kind of exchange is a
waste of time, and only occurs because you're not executing the code being
written.

If you actually run the code, the exchange could instead go:

> **Interviewer**: I see a bug in your code
>
> **Candidate**: It looks correct to me
>
> **Interviewer**: Are you sure?
>
> **Candidate**: Yes
>
> **Interviewer**: What if you put in this input?
>
> **Computer**: `IndexError: index out of range: 1, File "main.py", line 1, in
> <module>`

And thus the candidate is given irrefutable proof that something is broken,
without needing to explain to them what the problem is. They can then use their
standard debugging techniques to figure out what is wrong, and fix it.

While not every programming exercise can be conveniently packaged up in an
executable form (e.g. some involve calling mock functions or APIs that don't
actually exist), many can. When you're asking someone to serialize a binary tree
to a linked list, there's no reason why the candidate should not be able to take
their code, run it, and either verify its correctness or begin the debugging
process.

Making the candidate execute-and-debug their code is a very different interview
experience than just having the interviewer inspect it. I think it has a number
of benefits, some of them quite surprising:

- It forces the candidate to be less sloppy. When executing the code, every
  syntax error, undefined name or mismatched type becomes highly visible, but
  the candidate also has the tools needed to track them down and fix them. This
  simply creates an environment in which the candidate can write better code,
  despite my earlier warning against [nitpicking](#nitpicking)

- It has much greater [realism](#realism). When someone starts work, they're
  going to be writing code that is run. They'll need to track-down bugs, sort
  through stack traces, and use a range of tools and techniques to fix the code
  and make it run.

- It [sets them up for success](#set-them-up-for-success): rather than squinting
  at lines of code trying to spot what's wrong, the candidate has the full array
  of normal tools and techniques they can use to write the code and verify its
  correctness.

- It reduces the need for
  [making life difficulty for the candidate](#making-life-difficult-for-the-candidate):
  with the computer showing the candidate that their code is buggy, the interviewer no
  longer needs to be aggressively poking holes in their solution. Rather than
  *candidate v.s. interviewer* it then becomes *candidate v.s. code*, and the
  interviewer can take on a neutral or even supporting role.

Overall, writing-and-executing code is a mindset change compared to writing code
and having the interviewer squint at it. My experience is that those interviews go
much smoother, are much less stressful for both parties, candidates write better
code and nevertheless the interviewer ends up with much better signal on how
fluent the candidate is at writing code to solve problems.

### Let the candidate make mistakes

**It is ok to let a candidate explore approaches or solutions you know will end
up not working.**

Very often, I've seen a candidate start ever-so-slightly down the "wrong" path
to the solution, only to be immediately and vigorous stopped by the interviewer.
They are then forced, via constant questioning, back onto the "correct" path,
until they later diverge and again have to be stopped.

This kind of interviewer behavior isn't entirely unexpected: as engineers, we
loathe to see someone doing things the "wrong way", and want to help them get to
the correct answer as quickly as possible. This is something that is of value in
our day-to-day work.

But interviews are not like day-to-day work.

One of the main ways interviews are not like day-to-day work is that it is a
non-goal to get the candidate to
[successfully implement a specific algorithm](#successfully-implementing-a-specific-algorithm).

The point of the interview task is to make the candidate work. It is almost
irrelevant whether or not the task ends up being completed.

Thus, you should feel free to let a candidate make the "wrong" decisions.

- Sometimes it just doesn't matter: what they're doing isn't wrong, just
  different

- Sometimes, the "wrong" approach and the "right" approach look just as
  plausible up-front, and there's really nothing "wrong" in the *decision* to
  pick the "wrong" one: whether you pick the right approach up-front is
  basically random and says nothing about the candidate's abilities.

- Sometimes it does matter, and the candidate will figure it out later, or their
  computer will give them an error when they try to run their code. Then you'll
  get to see how the candidate recovers from mistakes.

Refactoring buggy code to turn it into correct code is one of the things that
software engineers do day-in and day-out. By letting the candidate run with a
mistake for a while, you then get a chance to see how well they do at this very
crucial skill. Watching how someone rescues a broken piece of code *they wrote*
tells you much more than watching them regurgitate a straightforward
breadth-first-search for the n-th time.

Remember that getting the candidate to the "correct" solution is *not* what your
job as the interviewer is. Your job is to see the candidate work in a variety of
realistic scenarios, and from the way they work judge how fluent they are at the
work they'll have to do if you end up hiring them. Working
with-broken-code/under-broken-assumptions for a while and having to
debug/refactor/fix/recover after is definitely an all-too-realistic scenario for
people working in this industry.

### Minimize Interruptions

**You should consciously minimize the number of times you interrupt a candidate
while they are working.**

Remember, the goal is not to get the candidate to
[successfully implement a specific algorithm](#successfully-implementing-a-specific-algorithm),
and that it is ok to
[let the candidate make mistakes](#let-the-candidate-make-mistakes). If a
candidate spending some time on an (eventually) dead-end approach gives you a
chance to watch them think and work, that is no loss at all given the
[goals](#goals) of the programming interview.

Minimizing interrupts helps avoid
[making life difficulty for the candidate](#making-life-difficult-for-the-candidate),
since interrupts are inherently an uncomfortable, confrontational affair.
Furthermore, by interrupting you are reducing the amount of time you can watch
the candidate work, which you want to [maximize](#maximize-candidate-work-time).
While you want to know if the candidate can discuss code, being interrupted
repeatedly in the middle of work is something most software engineers would
agree is *not* a welcome form of "discussion".

It's OK to interrupt occasionally, but you can often be strategic so your
interrupts are as un-disruptive as possible:

- Interrupt when the candidate has a pause in their work. This is less
  disruptive than interrupting while they're in the middle of writing out a line
  of code. If you [prepared multi-stage tasks](#prepare-multi-stage-tasks),
  there are lots of natural stopping points after each stage

- Batch questions/concerns/pointers/comments together, and interrupt a single
  time to start a discussion to cover multiple points. This lets people properly
  switch from "coding mode" to "discussion mode", and yields richer discussions
  with less surprise and frustration

- Of course, if the candidate is totally stuck and you-as-the-interviewer are
  getting no signal from watching them work, feel free to interrupt and start a
  conversation.

Interrupting the candidate in the middle of work, while sometimes unavoidable,
is something that you can and should take care to minimize.


## After the interview

After you're 60-minutes with the candidate are over, hopefully you have a pretty
good sense of how they are as an engineer, and whether you'd like to work with
them.

It's then time to write up your experience, both objective, e.g.

- 3 min: candidate did X
- 12 min: candidate got stuck at Y
- 24 min: candidate completed first task, modulo some bugs A, B, C which I
  pointed out
- 30 min: candidate fixed all obvious bugs, test cases all passed
- ...


And subjective, e.g.

- Candidate completed initial implementation very quickly, had a good intuition
  for what approach would work out
- Candidate was sloppy and left lots of logical errors/bugs throughout their
  code, despite knowing at a high level what they wanted to do
- Candidate very fluent at tracking down and fixing issues once they started
  debugging their code
- ...

Ideally, each subjective point would have one-or-more objective points
substantiating it. This is much harder than just writing down subjective
feelings, but forces you to be slightly more rigorous about why you feel a
certain way about the candidate. It also makes it much easier when you have
multiple people's write-ups that need to be integrated into a coherent, holistic
picture.

Depending on your process, the candidate may have multiple interviews to
complete. Perhaps the interviewers will get together to exchange notes about the
candidate and discuss their experience, or perhaps someone will consolidate the
feedback. Eventually, someone will make the call whether or not to give the
candidate a job offer. And that's the end of the interview process!

## Conclusion

It's not hard to be thrown in a room and asked to give a programming interview.
However, it is also not hard to conduct the interview poorly: antagonizing the
interviewee, testing irrelevant skills, or wasting precious minutes on things
which don't matter. Remember that these [goals](#goals) are the things that
actually matter:

- **Will the candidate be able to write working code if they join the team?**

- **Can the candidate discuss code and problems with the people they'll be
  working with?**

- **Can the candidate reason about arbitrary problems and constraints?**

- **Is the candidate someone we would enjoy working with?**

There are countless materials available on how to do well in a programming
interview *as the interviewee*: books, blog posts, skills-training websites, and
countless other sources. But there is almost nothing written about how
to conduct a good interview *as the interviewer*. This post tries to fill that
void.

This post has hopefully described a coherent philosophy behind the programming
interview, as well as several concrete steps that you can take to try and make
your interviews both smooth and insightful.

While in no way comprehensive, hopefully the guidelines presented here can help
you optimize the programming interviews you do conduct, so you can maximize the
amount you can learn about a software engineering candidate in the limited time
you have.
