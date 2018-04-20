---
layout: post
title:  "A Typed-Macro Writer’s Toolkit"
date:   2018-04-20 08:00:00
---

<i>
This is a transcript of a PL Wonks lightning talk.
</i>

<hr />

<img src="{{ site.url }}/images/wonks-typed-macros-spring-2018/wonks-typed-macros-spring-2018.001.jpeg"/>

Ok. So this talk is going to be about providing a toolkit
to macro writers in Clojure, to be able to communicate to the
type system.

<img src="{{ site.url }}/images/wonks-typed-macros-spring-2018/wonks-typed-macros-spring-2018.002.jpeg"/>

Here's the setting. On the left, we have the type system. It 
finds macros it needs to expand, but it has no idea how these macros
work &mdash; all the type system has is the ability to expand macros, 
and then make a best-effort on type checking the expansion.
On the other side, we have the macro authors, who, sure,
are willing to teach the type system how the macro actually works,
but, currently, there's no way to actually do that.

<img src="{{ site.url }}/images/wonks-typed-macros-spring-2018/wonks-typed-macros-spring-2018.003.jpeg"/>

So in this talk, I'm proposing that we provide an extensible
interface to Typed Clojure's internals to help it be more expressive
and usable.

<img src="{{ site.url }}/images/wonks-typed-macros-spring-2018/wonks-typed-macros-spring-2018.004.jpeg"/>

And how can Typed Clojure be made more expressive and usable?
Well, by provide macros that yield better static type error messages,
macros that require less type annotations, and macros that
are optimized for type checking performance, rather than runtime
performance.

<img src="{{ site.url }}/images/wonks-typed-macros-spring-2018/wonks-typed-macros-spring-2018.005.jpeg"/>

So first, let's talk about how we can write macros that yield
better static type error messages.

<img src="{{ site.url }}/images/wonks-typed-macros-spring-2018/wonks-typed-macros-spring-2018.006.jpeg"/>

Let's look at this simple `when` macro &mdash; it's like an `if`
but the else branch is implicitly `nil`.
So let's imagine we're trying to type check the expanded `if`
form as a `Number`.


<img src="{{ site.url }}/images/wonks-typed-macros-spring-2018/wonks-typed-macros-spring-2018.007.jpeg"/>

And we've found that the else branch is actually `nil`. It should
be a `Number`.

<img src="{{ site.url }}/images/wonks-typed-macros-spring-2018/wonks-typed-macros-spring-2018.008.jpeg"/>

So, what we'd _like_ the type system to do is blame the original
`when` expression.

<img src="{{ site.url }}/images/wonks-typed-macros-spring-2018/wonks-typed-macros-spring-2018.009.jpeg"/>

But what it _actually_ does is blame the original `when` macro &mdash;
basically blame the macro-writer.
The type system needs more information as to who to blame.

<img src="{{ site.url }}/images/wonks-typed-macros-spring-2018/wonks-typed-macros-spring-2018.010.jpeg"/>

The solution here is to allow macro-writers to provide blame labels
to the type system. For example, in the else branch here,
the red box symbolizes an annotation the macro-writer can use.
It says, if the else branch does not conform to the expected type,
then blame the entire `when` expression.

<img src="{{ site.url }}/images/wonks-typed-macros-spring-2018/wonks-typed-macros-spring-2018.011.jpeg"/>

Next, to argue that an extensible interface to Typed Clojure's
internals helps make it more expressive and usable, we're
going to look at how we can make macros require less type annotations.

<img src="{{ site.url }}/images/wonks-typed-macros-spring-2018/wonks-typed-macros-spring-2018.012.jpeg"/>

So here's a `for` comprehension. It increments `'(1 2 3)` to
`'(2 3 4)`. You'll notice there's two annotations &mdash;
one for the input, and one for the output.

<img src="{{ site.url }}/images/wonks-typed-macros-spring-2018/wonks-typed-macros-spring-2018.013.jpeg"/>

How might we eliminate the output annotation, knowing the
output of this `for` comprehension is very complicated, with
nested `loop`s and local mutation.

<img src="{{ site.url }}/images/wonks-typed-macros-spring-2018/wonks-typed-macros-spring-2018.014.jpeg"/>

To set the stage, let's say the expected type for this
`for` comprehension is a sequence of symbols.
That means, the surrounding program expects it to be
sequence of symbols &mdash; clearly it's a sequence
of _numbers_, so some type error is going to happen.
But, who's going to be blamed?

<img src="{{ site.url }}/images/wonks-typed-macros-spring-2018/wonks-typed-macros-spring-2018.015.jpeg"/>

Let's imagine the expansion is a bunch of code, with an
increment form somewhere in it.

<img src="{{ site.url }}/images/wonks-typed-macros-spring-2018/wonks-typed-macros-spring-2018.016.jpeg"/>

What we really want to do is propagate this `Sym` expected
type into this increment form, because this will correctly blame
the responsible user-written code.

<img src="{{ site.url }}/images/wonks-typed-macros-spring-2018/wonks-typed-macros-spring-2018.017.jpeg"/>

And, unsurprisingly, our solution is exactly this:
give the macro writer the ability to take an expected type
from a type checking run, deconstruct it, and then propagate
it to where error messages will be optimized.

<img src="{{ site.url }}/images/wonks-typed-macros-spring-2018/wonks-typed-macros-spring-2018.018.jpeg"/>

And finally, providing an extensible interface to Typed Clojure's
internals helps us write macros that yield simpler checks.

<img src="{{ site.url }}/images/wonks-typed-macros-spring-2018/wonks-typed-macros-spring-2018.019.jpeg"/>

For example, here's our `for` comprehension. It doesn't have
any annotations. How can we get away with this?

<img src="{{ site.url }}/images/wonks-typed-macros-spring-2018/wonks-typed-macros-spring-2018.020.jpeg"/>


Well, we just have a simplified expansion: instead of hundreds of
lines of complex code, we have a single function call.
Now, we basically have two macros: one optimized for runtime performance,
and one optimized for type checking performance.

In an optional type system like Typed Clojure which does not
optimize programs, we can keep the runtime-optimized expansion and
throw away the type-checking optimized expansion (after we're done).
Clojure programs routinely reload code (and therefore reexpand macros),
so this might be a feasible approach for Typed Clojure &mdash;
perhaps less-so for systems like Typed Racket.

<img src="{{ site.url }}/images/wonks-typed-macros-spring-2018/wonks-typed-macros-spring-2018.021.jpeg"/>


So finally, to conclude, we're providing an extensible interface
to Typed Clojure's internals to help it be more expressive 
and more usable.

<img src="{{ site.url }}/images/wonks-typed-macros-spring-2018/wonks-typed-macros-spring-2018.022.jpeg"/>

And how is it more expressive and more usable? Well, we can
write macros that have better errors, macros that require
less annotations, and macros that expand to code 
that is faster to type check.

<img src="{{ site.url }}/images/wonks-typed-macros-spring-2018/wonks-typed-macros-spring-2018.023.jpeg"/>

Thanks!
