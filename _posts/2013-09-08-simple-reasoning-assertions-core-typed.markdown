---
layout: post
title:  "Simple local reasoning with assertions"
date:   2013-09-08 23:00:00
---

Assertions are a popular technique for runtime verification of Clojure code.
These assertions are often a rich source of type information that core.typed
takes advantage of.

## Simple flow reasoning

Using simple reasoning about control flow, core.typed can understand typical
Clojure assertions and preconditions.

Consider the following common macroexpansion for an assertion:


```clojure
(fn [a]
  (if (number? a)
    nil
    (throw (AssertionError. "Assertion violation")))
  (inc a))
```

After we understand branching and sequencing of code in Clojure, it's easy to see that 
`(inc a)` will never throw a type error. 
If we get to `(inc a)`, `a` must be a `Number`, because otherwise an assertion error 
will be thrown.

core.typed follows a similar logic.

First, type information is gathered from the conditional. 

By type checking each branch, core.typed deduces:

- the *then* branch returns normally (it returns `nil`).
- the *else* branch never returns (it always throws an exception), 

The *then* branch is the _only_ branch visited, if execution proceeds normally (ie. without an exception).

Any updated type information that is applied inside the *then* branch can be applied to the
remainder of the current code block.

Concretely, `a` is updated from type `Any` to `Number` in the *then* branch,
so we can assume that `a` is of type `Number` for the rest of the function.

We can use [ann-form](http://clojure.github.io/core.typed/#clojure.core.typed/ann-form),
core.typed's local annotation macro, to confirm this intuition 
(see also: [cf](http://clojure.github.io/core.typed/#clojure.core.typed/cf)).

```clojure
(cf
  (fn [a]
    ; Before the conditional
    (ann-form a Any)
    (if (number? a)
      (do ; In the then branch
          (ann-form a Number)
          nil)
      (throw (AssertionError. "Assertion violation")))
    ; After the conditional
    (ann-form a Number)
    (inc a)))
;=> [Any -> Number]
```

</hr>

## Real world assertions

Assertions and pre/post conditions in Clojure are macros that expand out to code similar to
above.

The good news is that common assertions "just work" with core.typed.

For example, my favourite inline assertion is a sanity check for `nil`.

```clojure
(let [ns (find-ns 'clojure.core)
      _ (assert ns)]
  (ns-resolve ns '+))
```

This models how robust, defensive Clojure code should be written. core.typed updates `ns`
from `(U nil Namespace)` to `Namespace`, precisely what the programmer intended.

Pre/post conditions work similarly. The above code always returns a Var (because `clojure.core/+`
always exists), but we can easily convince core.typed we know what we are doing by using a 
post-condition.

```clojure
(ann plus-var [-> (Var Any)])
(defn plus-var []
  {:post [(var? %)]}
  (let [ns (find-ns 'clojure.core)
        _ (assert ns)]
    (ns-resolve ns '+)))
```

The post-condition updates the return value from `(U nil Class (Var Any))` to `(Var Any)`.

Returning to our original example, we can clean it up by utilising the succinct pre-condition syntax.

```clojure
(cf
  (fn [a]
    {:pre [(number? a)]}
    (inc a)))
;=> [Any -> Number]
```

</hr>

## Read more

Clojure's handling of assertions is a simple and logical extension of 
[occurrence typing](http://www.ccs.neu.edu/racket/pubs/icfp10-thf.pdf) (Tobin-Hochstadt, Felleisen).

Learn about core.typed's [Type Syntax](https://github.com/clojure/core.typed/wiki/Types).
