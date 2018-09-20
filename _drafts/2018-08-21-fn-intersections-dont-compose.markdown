---
layout: post
title:  "Function Intersection Types have poor reuse"
date:   2018-08-21 08:00:00
---

Typed Racket [popularised][1] ordered function intersection types,
later being used in Typed Clojure and [TypeScript "overloaded functions"][2].
They are a handy way of capturing the overloaded behaviour of a function.
However, they can quickly become unweildy.

Let's use an ordered intersection type to assign a type to Clojure's `+`.

```clojure
(ann clojure.core/+ (IFn [Long * -> Long]
                         [(U Long Double) * -> Double]
                         [AnyInteger * -> AnyInteger]
                         [Number * -> Number]))
```

This type encodes four straightforward properties of `+` that will
be used in order to infer a result type from provided arguments:
`Long` arguments yield a `Long`, 
any further `Double` argument yields a `Double`, 
adding integers gives an integer,
and a "catch-all" base case that allows any `Number` argument.

For example, `(+ 1 2) : Long` and `(+ 1 2.2) : Double`.

This is great! Expressive documentation with checked special cases.
But here's the catch. The types of `-`, `inc`,
and `dec` must duplicate this information.

```clojure
(ann clojure.core/- (IFn [Long * -> Long]
                         [(U Long Double) * -> Double]
                         [AnyInteger * -> AnyInteger]
                         [Number * -> Number]))
(ann clojure.core/inc (IFn [Long -> Long]
                           [Double -> Double]
                           [AnyInteger -> AnyInteger]
                           [Number -> Number]))
(ann clojure.core/dec (IFn [Long -> Long]
                           [Double -> Double]
                           [AnyInteger -> AnyInteger]
                           [Number -> Number]))
```

In fact, any helper function you might write with these functions
must duplicate these arities in some form to take advantage of the
improved inference.

```
(ann inc-inc (IFn [Long -> Long]
                  [Double -> Double]
                  [AnyInteger -> AnyInteger]
                  [Number -> Number]))
(defn inc-inc [n] (+ 2 n))
```

This doesn't seem so bad at first---even if we must endlessly tailor this
information for each helper function we write, at least it's only 4 lines.
However, for systems like Typed Racket's numeric tower, the situation is much
worse. 

My close friend and fellow IU graduate student [Andrew M Kent][3]
is working on simplifying the numeric tower


[1] http://www.ccs.neu.edu/home/stamourv/papers/numeric-tower.pdf
[2] https://www.typescriptlang.org/docs/handbook/functions.html
[3] https://github.com/racket/typed-racket/blob/445a65705a25697967c41e12d7770a2f360858c4/typed-racket-lib/typed-racket/base-env/base-env-numeric.rkt
[4] https://pnwamk.github.io/
