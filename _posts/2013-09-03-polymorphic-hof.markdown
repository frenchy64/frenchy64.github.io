---
layout: post
title:  "Using polymorphic higher-order functions"
date:   2013-09-03 00:00:00
categories: Typed Clojure, core.typed, Clojure
---

When it comes to annotating functions in [core.typed](https://github.com/clojure/core.typed), there are
some special rules to keep in mind.
Here are two.

1. all function parameters should be annotated
2. polymorphic functions should be instantiated when used
  as a parameters to polymorphic higher-order functions.

</hr>

# Annotating function parameters

There are several ways for function argument annotations to propagate
downwards.

To annotate a full function type, use [`ann-form`](http://clojure.github.io/core.typed/#clojure.core.typed/ann-form).

```clojure
(cf (ann-form (fn [a] (inc a)) [Number -> Number]))
;=> [Number -> Number]
```

[`cf`](http://clojure.github.io/core.typed/#clojure.core.typed/cf) takes a second argument which expands
into `ann-form`.

```clojure
(cf (fn [a] (inc a)) [Number -> Number])
;=> [Number -> Number]
```

[`fn`](http://clojure.github.io/core.typed/#clojure.core.typed/fn) supports partial and full
annotations.

```clojure
(cf (fn [a :- Number] (inc a)))
;=> [Number -> Number]
```

</hr>

# Polymorphic higher-order functions

A keyword can be used as a function.

```clojure
(cf (:foo {:foo 1}))
;=> (Value 1)
```

The first argument to the polymorphic function [map](https://github.com/clojure/core.typed/blob/57da1175037dfd61c96c711165ea318db65f46c0/src/main/clojure/clojure/core/typed/base_env.clj#L1002) is often, itself, polymorphic.
This thwarts the local type inference algorithm.
Passing monomorphic arguments works fine.

```clojure
(cf (map + [1 2 3]))
;=> (clojure.lang.LazySeq AnyInteger)
```

(See [`AnyInteger`](http://clojure.github.io/core.typed/#clojure.core.typed/AnyInteger)).

However if we map over a polymorphic function &mdash; say, a keyword &mdash; we must
provide a more specific type via _instantiation_.

```clojure
(cf (map (inst :foo Number) [{:foo 1} {:foo 2}]))
;=> (clojure.lang.LazySeq Number)
```

[`inst`](http://clojure.github.io/core.typed/#clojure.core.typed/inst) takes a polymorphic
expression and a number of types, and has a return type that replaces the bound type variables
in the polymorphic type with the types provided.
A call to `inst` returns the first argument at runtime.

```clojure
(cf (inst :foo Number))
;=> [(HMap :mandatory {:foo Number}) -> Number]
```

(See [Heterogeneous Maps](https://github.com/clojure/core.typed/wiki/Types#heterogeneous-maps)
for further discussion on heterogeneous map types)

I often introduce a local binding, or even a top-level definition that is just an instantiated type.
This can get your type annotations out of the way and get back some of the readability of just using
a plain keyword.

```clojure
(cf
  (let [get-foo (inst :foo Number)]
    (map get-foo [{:foo 1} {:foo 2}])))
;=> (clojure.lang.LazySeq Number)
```

</hr>

# Why is this necessary?

core.typed and [Typed Racket](http://docs.racket-lang.org/ts-guide/)
use [Local Type Inference (PDF)](http://www.cis.upenn.edu/~bcpierce/papers/lti.pdf) (Pierce and Turner)
to infer applications of polymorphic arguments.

It turns out we can infer direct applications of polymorphic types fairly easily, but
higher-order polymorphic types are more difficult to infer when passed other polymorphic
types. [Hosoya and Pierce](http://citeseerx.ist.psu.edu/viewdoc/summary?doi=10.1.1.39.7265)
describe exactly what &ldquo;plain&rdquo; local type inference is capable of.

The developers of core.typed and Typed Racket are both keen to develop more powerful local inference,
but it probably will require a non-trivial amount of effort. 
Scala&rsquo;s [Colored Local Type Inference](http://lampwww.epfl.ch/~odersky/papers/popl01.html) (Odersky, Zenger, Zenger and Lausanne) 
is the kind of extension to local type inference type we&rsquo;re jealous of.

Furthermore, Typed Clojure and Typed Racket already include extensions of its own to support [Practical Variable-arity
Polymorphism (PDF)](http://www.ccs.neu.edu/racket/pubs/esop09-sthf.pdf) (Strickland, Tobin-Hochstadt and Felleisen),
which helps us type check polymorphic functions with non-trivial variable-parameters like `map`.

</hr>

# See also

- [Sam Tobin-Hochstadt&rsquo;s work](http://www.ccs.neu.edu/home/samth/)
- [Steve Strickland&rsquo;s work](http://www.ccs.neu.edu/home/sstrickl/)
- [Polymorphism in core.typed](https://github.com/clojure/core.typed/wiki/User-Guide#polymorphism)
- My Honours dissertation [_A Practical Optional Type System for Clojure_](https://github.com/downloads/frenchy64/papers/ambrose-honours.pdf)
