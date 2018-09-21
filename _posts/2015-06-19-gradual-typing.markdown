---
layout: post
title:  "Gradual typing for&nbsp;Clojure"
date:   2015-06-19 08:00:00
---

Today&rsquo;s programming landscape is _dynamic_ and _polyglot_. 
Interlanguage
interoperability is a mainstay &mdash; it&rsquo;s almost unavoidable that a given piece of
code will be used without the knowledge of the original author.

So it&rsquo;s surprising to learn
most static type systems completely ignore the pragmatics of running statically typed
code in the real world.
Who can use this code? Can they break the type invariants?
These questions are answered through the narrow vision of compile-time sources, without
considering what can _actually_ happen at runtime.

_Gradual typing_ is designed to keep static invariants reliable 
in the real world &mdash; it
preserves the invariants of well-typed
code _beyond_ the compile-time sandbox type checkers assume.


<div>
<center>
<a href="https://www.indiegogo.com/projects/gradual-typing-for-clojure/x/4545030">
<img src="{{ site.url }}/images/gradual-banner-for-crowdfunding.png"/>
</a>
</center>
</div>

I am enhancing Typed Clojure with gradual typing.
You can <a href="https://www.indiegogo.com/projects/gradual-typing-for-clojure/x/4545030">support&nbsp;me</a> by funding or sharing
the associated crowdfunding campaign.
To learn more, keep reading.

_Gradual typing_ cares deeply about interlanguage invariants. 
While static type systems define and verify invariants _within_ a language,
gradual type systems preserve these invariants _across_ language boundaries.

<div class="aside">
Gradual typing is widely applicable to many combinations
of languages. There is usually a &ldquo;more typed&rdquo; and respectively
&ldquo;less typed&rdquo; language &mdash; Idris and Haskell are a
plausible combination.
</div>

The role of a gradual type system is to protect a language&rsquo;s static invariants
from foreign code. Often this means protecting a typed language from an untyped
language.

We now explore the details of gradual typing.

It takes two to tango, so
let Typed Clojure be our &ldquo;typed&rdquo; language &mdash; the language
with finer grained static invariants
&mdash; and Clojure 
our &ldquo;untyped&rdquo; language.

The main feature of a gradually typed language is the _runtime mediator_.
As the gatekeeper to the typed land, it uses _runtime contracts_
to ensure typed and untyped values play nicely.


<div class="aside">
You can think of each land as a file written in the corresponding language.
</div>

We represent the mediator as an orange line separating &ldquo;typed land&rdquo;
(left) and &ldquo;untyped land&rdquo; (right).

<img src="{{ site.url }}/images/language-boundary.png"/>

<div class="aside">Gradual typing was independently invented
around 2006 by 
<a href="https://github.com/samth/gradual-typing-bib#the-original-papers">four papers</a>.
Siek & Taha&rsquo;s &ldquo;micro&rdquo; gradual typing applies to single expressions,
while Tobin-Hochstadt & Felleisen&rsquo;s &ldquo;macro&rdquo; approach only applies to entire modules.
We concentrate on the latter, as featured in Typed&nbsp;Racket.
</div>

Whenever a value &mdash; typed or untyped &mdash; crosses the language boundary, it is the mediator&rsquo;s job to
apply an appropriate contract.

We represent an untyped value with the
Clojure logo. When the mediator wraps some untyped value in contracts, we 
represent this as a ring of orange.

<img src="{{ site.url }}/images/untyped-example.png"/>

Similarly, the Typed Clojure logo stands for a well-typed Typed Clojure value, with
contracts represented by a ring of orange.

<img src="{{ site.url }}/images/typed-example.png"/>

Importing untyped values into typed land requires a contract wrapping the untyped
code.

<img src="{{ site.url }}/images/import-untyped-boundary.png"/>

The goal is to restrict the untyped value to behave exactly as the annotation ascribed to
it by the programmer, otherwise throw a contract violation.
Now the typed language can &ldquo;forget&rdquo; the value is untyped and
treat it like any other typed value.


An exported typed value also needs a contract.

<img src="{{ site.url }}/images/export-typed-boundary.png"/>

Isn&rsquo;t the typed value already type-safe by definition? Yes.
Why do we need a contract? All usages in _typed land_ are type checked,
but exporting a typed value leaves it vulnerable to unknown code.
To preserve the static invariants for typed value in untyped land, we need a contract.

That&rsquo;s the extent of the runtime mediator&rsquo;s job at the language boundary.
Pretty straightforward really.

Of course, the devil&rsquo;s in the details &mdash; especially (as usual) with functions.
What is a function contract?
How do function contracts protect typed functions? Or verify untyped functions?
Let&rsquo;s take a closer look at some invocation sites
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
by the mediator as they crossed the language boundary.
Therefore, this invocation has been type checked already and requires no further
validation.

Is it possible for a typed function to be applied to an untyped value
&mdash; without contracts?

<img src="{{ site.url }}/images/bad-typed-invoke-untyped.png"/>

No &mdash; at least one of these values
must have contracts from crossing the language boundary.

In the first situation, 
the _typed function_ has crossed the language boundary into untyped land.

<img src="{{ site.url }}/images/export-typed-boundary.png"/>

Now it&rsquo;s plausible to apply the operator.

<img src="{{ site.url }}/images/gtyped-invoke-untyped.png"/>

What kind of contract is attached to the typed function?
Checking the input is very similar to the mediator accepting an
untyped value &mdash; it is wrapped with a contract.

<div class="aside">
This picture represents an untyped value flowing into a (contracted)
typed function. While similar to the mediator, don&rsquo;t confuse
the two.
</div>

<img src="{{ site.url }}/images/import-untyped-boundary.png"/>

Now notice the return value of the invocation.
A typed function returns a typed value, right? Yes.
Why is the output contracted? We need to protect typed values from further interactions
with untyped code.

Say we return a typed function.

```clojure
(fn [n :- Int] :- Int
  (+ 1 n))
```

The following can happen if the function is left bare in untyped land.

```clojure
((fn [n :- Int] :- Int
   (+ 1 n))
 1.2)
;=> 2.2
```

Clearly this is bad. `1.2`{:.language-clojure .highlight} is not an `Int`. Neither is `2.2`{:.language-clojure .highlight}. Adding contracts preserves the static invariants.

```clojure
((fn [n :- Int] :- Int
   {:pre [(integer? n)]}
   (+ 1 n))
 1.2)
;=> Contract violation ...
```

It turns out we can elide the contract on the return value of typed functions
if they are _flat_ values, that is a value that can be checked immediately 
&mdash; like integers, strings, or immutable data structures.
If the return type is a function like `[Int -> Int]`{:.language-clojure .highlight} or anything that _might_ require a higher-order
contract like `Any`, we must be proactive and add a contract.

The second case where a typed function can be applied to an untyped value is in typed land
&mdash;

<img src="{{ site.url }}/images/typed-invoke-guntyped.png"/>

&mdash;
where the _untyped value_ was imported into typed land via the mediator.

<img src="{{ site.url }}/images/import-untyped-boundary.png"/>

Remember, typed code can consider an untyped value as _typed_
if the mediator assigns it a contract.
So, if the argument is now typed, we require no more runtime checking &mdash;
the invocation is statically typed.

The final two cases are similar.

Firstly, exporting a typed value can be used by an untyped function.

<img src="{{ site.url }}/images/untyped-invoke-gtyped.png"/>

Secondly, importing an untyped function wraps it in a function contract,
which ensures the plain typed parameter is protected 
before it enters the untyped function.

<img src="{{ site.url }}/images/guntyped-invoke-typed.png"/>

The return value also needs checking on each invocation &mdash; we can&rsquo;t
trust an untyped function to do the right thing.

Finally for comparison, here are the interesting combinations.

<img src="{{ site.url }}/images/gtyped-invoke-untyped.png"/>

<img src="{{ site.url }}/images/typed-invoke-guntyped.png"/>

<img src="{{ site.url }}/images/guntyped-invoke-typed.png"/>

<img src="{{ site.url }}/images/untyped-invoke-gtyped.png"/>

<hr/>

In practice, a gradual typing system must consider much more
than what we&rsquo;ve outlined here.

If you want to support the effort to bring gradual typing to Clojure,
please fund the crowdfunding campaign.

<iframe src="https://www.indiegogo.com/project/typed-clojure-from-optional-to-gradual-typing/embedded/4545030" width="222px" height="445px" frameborder="0" scrolling="no"></iframe>

