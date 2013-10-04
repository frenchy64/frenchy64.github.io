---
layout: post
title:  "Typed Clojure prevents Null Pointer Exceptions"
date:   2013-10-04 08:00:00
---

_I am crowdfunding towards 12 months of full-time Typed Clojure development. If you value
strong guarantees like the static prevention of Null Pointer Exceptions, 
[pledge or share today](http://www.indiegogo.com/projects/typed-clojure)!_

Typed Clojure is opinionated: some type errors are checked
at compile time, and other checks are delayed until runtime. One kind of
type error Typed Clojure is designed to eradicate at compile time is the _Null Pointer
Exception_.

This decision is enforced in regular Clojure code without complicated
runtime constructs or special handling. In fact, the design conforms
so well to the way Clojure programmers reason about problems they will hardly 
even notice the type system's pedantry.

## nil in practice

To understand how Typed Clojure helps Clojure programmers make such strong
claims of their code, we should explore the nature of `nil` in Clojure.

At its most basic level, `nil` is exactly Java's _null_ value.

`nil` has a special property in Clojure: it is a false value. This is particularly
convenient for branching in the presence of `nil`, as tests like `(nil? e)` to test
for `nil` can simply be replaced by `e`.

The places where `nil` is used are very similar to their intended use
in Java: a way to express "nothing", or a lack of a value.
The crucial detail here is the expressivity of the type system: it's not inherently bad
to have a null value, but it's a sin to create a type system
that cannot reason about its usage.

This is exactly the situation in Java: _null_ is implicitly part of every reference type,
at least according to the type system. This leaves the complicated and error-prone task of
correctly handling _null_ to the programmer; infamously, it's all too easy to get it wrong.

## nil is explicit

Typed Clojure's handling of `nil` is easy to understand: `nil` is just another value, being the
only instance of class `nil`. This may seem we are destroying what makes _null_ a null value:
this is the point.
It is a false convenience for _null_ to be implicitly included in all reference types.

Now that `nil` is just another value type, we must be explicit about its usage.
Clojure programmers think in terms of data flow. To capture this insight, types must be rich enough to
describe arbitrary combinations of data. Typed Clojure provides _unions_ for exactly this:
if a function returns a `java.lang.Integer` or `nil`
the type is simply `(U java.lang.Integer nil)`.

Introducing `nil` with unions is all well and good, but we need a mechanism to _eliminate_ `nil`
if a potential misuse of `nil` is to result in a type error.

Typed Clojure already supports a powerful and easy-to-understand way of refining a union:
[occurrence typing](http://frenchy64.github.io/2013/09/08/simple-reasoning-assertions-core-typed.html).
Typed Clojure knows that the local binding `x` in `(when x e)` will never be `nil` in expression `e`,
and actually updates the type of `x` without `nil`.
This also extends to nested structures like heterogeneous maps: the `:foo` key of `x`
in `(when (:foo x) e)` is known to never be `nil` in `e`.

Type checking interactions with Java code is an interesting problem with an explicit `nil` type,
but the solution is [intuitive](http://vimeo.com/55280915), essentially relying on the programmer
to annotate where `nil` is permitted.
The only problem is whether these annotations actually describe the behaviour of the corresponding
Java code; if they differ, there is unsoundness in the type checking.
Currently, global Java annotations are treated as unchecked assumptions, but we could check these annotations
at runtime (and thus fail as early as possible) 
as part of [future work](http://www.indiegogo.com/projects/typed-clojure/x/4545030).

## The result

Using Typed Clojure, Clojure programmers can rest easy that they are never misusing `nil`.
Programming with the flexibility and conciseness that Clojure provides while enjoying this
guarantee is incredibly liberating, and allows you to concentrate on more important things.

You are protected from the sometimes strange world of Java, forcing you to be explicit about
the things a Java type annotation lacks. You can research and annotate the exact semantics of your library
_once_, and know Typed Clojure will always catch type errors that might result in a Null Pointer Exception.

History shows having a null value is extremely error prone, and even a single mishandled null value can
be catastrophic. We as programmers need all the help we can get null right: Typed
Clojure answers this for Clojure programmers.

_Pledge towards further [Typed Clojure](http://www.indiegogo.com/projects/typed-clojure) development_
