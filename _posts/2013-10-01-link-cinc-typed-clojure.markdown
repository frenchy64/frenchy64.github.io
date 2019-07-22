---
layout: post
title:  "The link between CinC and Typed Clojure"
date:   2013-10-01 00:00:00
---

The first stretch goal for the [Typed Clojure campaign](https://www.indiegogo.com/projects/typed-clojure/) 
concentrates on a significant missing piece in JVM tooling: a self-hosted Clojure compiler,
(aka. a Clojure compiler written in Clojure).

This post is intended to briefly demonstrate where core.typed can benefit from [CinC](https://github.com/Bronsa/CinC)
(read Clojure-in-Clojure), an actively developed Clojure compiler.

## Performance

Probably the most frequently asked question about Typed Clojure is whether
it can improve the performance of your code.
("What's with all the annotations?" runs a close second)

The answer is, potentially yes. The most obvious way to speed up Clojure code
is to use Typed Clojure's rich type system to eliminate reflection calls.
Often, Typed Clojure comes across a reflection call and it knows exactly which
type hints should be inserted.

The question is: who do we
give the reflection information to? The Clojure JVM compiler provides no
way to insert hooks or customisations.
This leaves us with our current situation: reflection calls found during type checking
result in a static type error asking the user to provide a type hint.

What would such a hook look like in a Clojure compiler? I don't know.
This is exactly the type of problem a rapidly-iterating compiler like 
CinC is suited to explore.

## Complex macros

[core.match](https://github.com/clojure/core.match) is a pattern matching library
for Clojure. One of my earliest experiences developing Clojure libraries was
writing the initial versions of core.match with David Nolen. 

Since then, David has significantly cleaned up core.match, and recently released 0.2.0.

The centerpiece of core.match is the `match` macro. It 
[rearranges](https://github.com/clojure/core.match/wiki/Understanding-the-algorithm) 
your pattern match into a very efficient representation by minimising branches.

It turns out this is not easy to type check. In particular, the type of
`a` in the `:else` branch in

```clojure
(fn> [a :- (U (Vec Any) (Map Any Any))]
  (cond
    (vector? a) ...
    :else a))
```

could be inferred to be `(Map Any Any)`, because the first branch eliminates the case
of `(Vec Any)`.

It is not so simple following this kind of logic inside aggressively optimised branching
code, like the macroexpansion of `match`. Often branches are rearranged to be much 
"further" away from where you would expect, and futhermore complex exception handling logic
is used to pick branches (this reduces code size).

What might be great is to "pause" macroexpansion at a `match` and be able to plug in a
custom function for type checking. Then we might expand `match` ourselves into a "dumb"
representation; perfect for a type checker.

Again, it's not clear how to achieve this, and we probably need to try many different
approaches. CinC's analyzer is much smaller, clearer and more extensible than the current
Clojure analyzer. As someone who's [hacked around](https://github.com/clojure/jvm.tools.analyzer) 
with current analyzer for 2 years, I'm looking forward to moving onto more flexible technology.

## Funding

I believe CinC in some incarnation will be prominent in some future version of Clojure (CinC
is planned to be offered to Clojure contrib). 
Officially replacing the JVM compiler won't happen any time soon.
It will take years of experimentation with compilers
like Clojurescript and CinC to really nail the "Clojure-in-Clojure" style compilers.

We should start the process today.

After observing his [recent work](https://groups.google.com/d/msg/clojure/cC1yC9zrS1s/W0ducjm0uQYJ) 
on CinC, it's clear that [Nicola](https://twitter.com/Bronsa_) is the real deal.
All funds raised between the $20,000-$25,500 interval will go to Nicola's work on
CinC.

[Pledge today](https://www.indiegogo.com/projects/typed-clojure/)!

## Update

So I tried out my ideas of type checking core.match and I was [very pleased](https://www.youtube.com/watch?v=g2zts1hW19k) with the results!
