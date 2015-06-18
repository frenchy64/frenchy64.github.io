---
layout: post
title:  "Gradual typing for&nbsp;Clojure"
date:   2015-06-11 08:00:00
---

Today&rsquo;s programming landscape is _dynamic_ and _polyglot_. Interlanguage
interoperability is a mainstay &mdash; it&rsquo;s almost unavoidable that a given piece of
code will be used without the knowledge of the original author.

So it&rsquo;s surprising to learn
most static type systems completely ignore the pragmatics of running statically typed
code in the real world.
Who can use this code? Can they break the type invariants?
These questions are answered through the narrow vision of compile-time sources, without
considering what can _actually_ happen at runtime.

Invariants are unreliable if they can be broken.

_Gradual typing_ is designed to keep static invariants reliable 
in the real world &mdash; it
preserves the invariants of well-typed
code _beyond_ the compile-time sandbox type checkers assume.

I am enhancing Typed Clojure with gradual typing.
You can <a href="https://www.indiegogo.com/projects/gradual-typing-for-clojure/x/4545030">support&nbsp;me</a> by funding or sharing
the associated crowdfunding campaign.
To learn more, keep reading.


<div>
<center>
<a href="https://www.indiegogo.com/projects/gradual-typing-for-clojure/x/4545030">
<img src="{{ site.url }}/images/gradual-banner-for-crowdfunding.png"/>
</a>
</center>
</div>


_Gradual typing_ cares deeply about interlanguage invariants. 
While static type systems define and verify invariants _within_ a language,
gradual type systems preserve these invariants _across_ language boundaries.

The role of a gradual type system is to protect a language&rsquo;s static invariants
from foreign code. Often this means protecting a typed language from an untyped
language &rsquo; gradual typing is however widely applicable to many combinations
of languages. 

We now explore the details of gradual typing.

It takes two to tango, so
let Typed Clojure be our &ldquo;typed&rdquo; language &mdash; the language
with finer grained invariants to preserve
&mdash; and Clojure 
be our &ldquo;untyped&rdquo; language.

The main feature of a gradually typed language is the _runtime mediator_.
As the gatekeeper to the typed land, it is responsible for ensuring any
code that crosses it is verified with the appropriate contract.
Depending on the direction, the mediator may choose different contracts
for similar kinds of values.

We represent the mediator as an orange line separating &ldquo;typed land&rdquo;
&mdash; left &mdash; and &ldquo;untyped land&rdquo; &mdash; right.

<img src="{{ site.url }}/images/language-boundary.png"/>

<div class="aside">Gradual typing was independently invented
around 2006 by 
<a href="https://github.com/samth/gradual-typing-bib#the-original-papers">four papers</a>.
Siek & Taha&rsquo;s &ldquo;micro&rdquo; gradual typing applies to single expressions,
while Tobin-Hochstadt & Felleisen&rsquo;s &ldquo;macro&rdquo; approach only applies to entire modules.
We concentrate on the latter, as featured in Typed&nbsp;Racket.
</div>

You can think of each land as a file written in the corresponding language.

When must contracts be added? Anything that cannot be verified at compile time
that could compromise typed code must be checked by a contract.

We represent an untyped value with the
Clojure logo. When the mediator wraps some untyped value in contracts, we 
represent this as a ring of orange.

<img src="{{ site.url }}/images/untyped-example.png"/>

Similarly, the Typed Clojure logo stands for well-typed Typed Clojure value, with
contracts represented by a ring of orange.

<img src="{{ site.url }}/images/typed-example.png"/>

Importing untyped values into typed land requires a contract wrapping the untyped
code.

<img src="{{ site.url }}/images/import-untyped-boundary.png"/>

The goal is to restrict the untyped value to behave exactly as the annotation ascribed to
it by the programmer, otherwise throw a contract violation.
Now the typed language can &ldquo;forget&rdquo; the value is untyped and
treat it like any other typed value.


A contract can sometimes be verified
directly by the mediator. A _flat contract_ checks for values like numbers, strings,
and immutable collections &mdash; they can be checked immediately. _Higher-order contracts_
verify functions and mutable references &mdash; they must be permanently wrapped around
the value they check.
For brevity this article doesn&rsquo;t distinguish the two, but the pictorial
representation of a wrapping mediator is a useful metaphor for higher-order contracts.

Exported typed code also needs a contract.

<img src="{{ site.url }}/images/export-typed-boundary.png"/>

Isn&rsquo;t the typed code already type-safe by definition? Yes.
Why do we need a contract? The type system verified all usages in _typed land_
as safe, but exporting the code leaves it vulnerable to unknown code.
To preserve the static invariants for typed code in untyped land, we need a contract.

That&rsquo;s the extent of the runtime mediator&rsquo;s job at the language boundary.
Pretty simple really.

But when functions are involved &mdash; especially in combination with types &mdash; nothing is ever obvious.
What is a function contract?
What do function contracts need to check on typed values? Untyped values?
Let&rsquo;s take a closer look 
at some invocation sites
to appreciate some of runtime mediator&rsquo;s choices. 

The simplest case is an untyped function applied to an untyped value.

<img src="{{ site.url }}/images/untyped-invoke-untyped.png"/>

<div class="aside">Recall the goal of gradual typing &mdash; to ensure the invariants
of <i>typed code</i> are preserved.
</div>

The output is simply an untyped value.
Since there is no interaction with typed code, we don&rsquo;t need to check inputs
or outputs.

Similarly, there&rsquo;s not much to do when typed code is applied to typed code.

<img src="{{ site.url }}/images/typed-invoke-typed.png"/>

By elimination, this situation can _only_ occur in typed land &mdash; if it were in
untyped land, then both the function and the argument would be wrapped in contracts
by the mediator.
Therefore, this invocation has been type checked already and requires no further
validation.

Is it possible for a typed function to be applied to an untyped value
&mdash; without contracts?

<img src="{{ site.url }}/images/bad-typed-invoke-untyped.png"/>

No &mdash; at least one of these values
must have contracts from crossing the language boundary.

In the first situation, 
the typed function has crossed the language boundary into untyped land.

<img src="{{ site.url }}/images/export-typed-boundary.png"/>

Now it&rsquo;s plausible to apply the operator.

<img src="{{ site.url }}/images/gtyped-invoke-untyped.png"/>

For the first time we can appreciate
What kind of contract does the mediator use?

Two interesting things happen based 

The function contract will verify the untyped code conforms to the typed function&rsquo;s 
parameter type &mdash; say `Int`.

A typed function returns a typed value right? Yes.
Why is the output contracted? We need to protect typed values from further interactions
with untyped code.

Say we return a typed function.

```clojure
((fn [m :- Int]
   (fn [n :- Int]
     (+ m n)))
 1)
;=> (fn [n :- Int]
;     (+ 1 n))
```

<div class="aside">The higher-order case is usually the most interesting.
First-order outputs don&rsquo;t need checking (I forgot the former case while drafting this article).
</div>

Wrapping the return value in a contract anticipates bad calls from untyped code.

```clojure
((fn [n :- Int]
   (+ 1 n))
 1.2)
;=> Contract violation
```

The second case in untyped land is applying an uncontracted untyped function to a contracted typed argument.

<img src="{{ site.url }}/images/export-typed-boundary.png"/>

<img src="{{ site.url }}/images/untyped-invoke-gtyped.png"/>

The typed code is already protected, and we don&rsquo;t care about invariants of untyped functions,
so this is just a normal invocation.

The final case occurs only in typed land &mdash; we import an untyped function, now
wrapped in its contract,
and apply it to plain typed code.

<img src="{{ site.url }}/images/guntyped-invoke-typed.png"/>

But isn&rsquo;t the typed code
entering unprotected into untyped code? Yes. How are its invariants preserved?
A function contract 

```clojure
(defn inc [a]
  (foo a))
```

<iframe src="https://www.indiegogo.com/project/typed-clojure-from-optional-to-gradual-typing/embedded/4545030" width="222px" height="445px" frameborder="0" scrolling="no"></iframe>

<hr/>

With gradual typing, the Typed Clojure compilation pipeline will look like this.

Migrating code to Typed Clojure often involves removing dynamic checks.

In a perfect world, all code is written in Typed Clojure and we've simply made
our programs faster!
At some point, however, we need to interact with the rest of the world
and this is where things get scary.

Take our hero @aphyr. He has diligently decided to convert an old Clojure function to
Typed Clojure and was rudely greeted by a null-pointer exception originating from 
<b>typed code</b>!

<!--
<blockquote class="twitter-tweet" lang="en"><p lang="en" dir="ltr">(said bug was introduced by a refactor I made to satisfy the typechecker which, when called by unchecked code, allowed a null through)</p>&mdash; Upbeat Med. Hologram (@aphyr) <a href="https://twitter.com/aphyr/status/575906637531062272">March 12, 2015</a></blockquote>
<script async src="//platform.twitter.com/widgets.js" charset="utf-8"></script>
-->

But Typed Clojure guaranteed to <b>prevent</b> NPE's in typed code!

This situation is easy to replicate.
Simply call a typed function from untyped land with ill-typed arguments.

```clojure
(ns ^:core.typed safe
  (:require [clojure.core.typed :refer [ann All]]))

(ann app (All [a b] [[a -> b] a -> b]))
(defn app 
  "Takes a function of one argument and an argument and 
  returns the result of applying that function 
  to the argument."
  [f x]
  (f x))
```

```clojure
(ns unsafe
  (:require [safe]))

(safe/app inc 1)
;=> 2

(safe/app nil 2)
; NullPointerException thrown
```

Even the fancy polymorphic type assigned to `safe/app` is powerless to stop the untyped 
`unsafe` from passing bad arguments.
Statically typing `unsafe` is one solution, but this just pushes the problem further out:
there will always be a boundary between languages where static enforcement is impossible.

My bigger point is an untyped version of `app` has better runtime checks that <b>should</b>
be redundant with static type checking.

```clojure
(defn app 
  "Takes a function of one argument and an argument and 
  returns the result of applying that function 
  to the argument."
  [f x]
  (assert (ifn? f) "Must provide a function to app")
  (f x))
```

Unfortunately these runtime checks are still needed to be fully robust.

</p>

<hr/>


# Trash

Gradual typing is the discipline of strictly preserving language invariants across boundaries.

This usually involves inserting runtime checks either to ensure the "less" typed language
adheres to its annotation, or to protect the "more" typed language from foreign
code.


After all this, we deserve an actionable quote.

```
Gradual typing makes statically typed programs robust to foreign code.
```

Typed Clojure has been built with gradual typing in mind from the beginning, and it's finally time to
implement it.
If this is up your alley, please fund the project!

<center>
<iframe src="https://www.indiegogo.com/project/gradual-typing-for-clojure/embedded" width="222px" height="445px" frameborder="0" scrolling="no"></iframe>
</center>

A gradually typed _language_ has a type system that performs static type checking,
as well as help the compiler preserve the static invariants of checked code.
The way it does this is with runtime contracts.


[Static program analysis](http://matt.might.net/articles/intro-static-analysis/) 
have started to address the problem of polyglot projects.
Galois&rsquo; [SAW](http://saw.galois.com/)
can verify [program equivalence](http://saw.galois.com/tutorial.html#cross-language-proofs)
across polyglot programs.
Facebook&rsquo;s [Infer](https://code.facebook.com/posts/1648953042007882/open-sourcing-facebook-infer-identify-bugs-before-you-ship/)
bundles multiple monoglot linters into one.

But unknown code is still a problem.
