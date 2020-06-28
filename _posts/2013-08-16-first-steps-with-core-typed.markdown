---
layout: post
title:  "Invariants via immutability"
date:   2013-08-16 16:22:50
categories: Typed Clojure, core.typed, Clojure
---

Clojure&rsquo;s emphasis on immutable bindings and data structures lead
us to write simpler, more obvious code. 
We don&rsquo;t need to worry about immutable things changing over time.
This reduces the cognitive load of both writing and reading code.

It is common to assume invariants for mutable structures, however,
even in Clojure.
Sometimes refactoring such logic in terms of _immutable_ things can be
clearer, and also help verification tools like Typed Clojure infer and check interesting invariants.

# Local bindings

Local bindings in Clojure are immutable. Furthermore, local bindings often point to
data structures which are _themselves_ immutable.

This combination enables Typed Clojure to infer invariants and are 
both uncomplicated and inexpensive to compute.

For example, if a collection implements `clojure.lang.IPersistentCollection`
(ie. it is immutable), Typed Clojure can track its `count`.

In this snippet, a local `a` of type `(Coll Number)` is in scope.
`seq` returns a true value if its argument is non-empty. Typed Clojure
remembers this down the &ldquo;then&rdquo; branch, because `a` is _immutable_.

```clojure
...
(if (seq a)
  ; `a` is (NonEmptyColl Number)
  ; `a` is (Coll Number)
  )
...
```

Note: `Coll` and `NonEmptyColl` are type aliases defined in `clojure.core.typed`.

This also works sequentially with assertions.

```clojure
...
(do
  (assert (seq a))
  ; `a` is (NonEmptyColl Number)
  )
...
```

The program will fail at runtime if the expected invariant is violated. 
Because of this, Typed Clojure is safe to assume the invariant on our immutable
binding for the rest of its scope.

# Dynamic vars

Vars in Clojure are mutable. Typed Clojure does not attempt to track any interesting
invariants about Vars as programs progress, but, as we will see, we can fall back on 
the obvious safety of _immutable_ local bindings.

In this example, `*atom-or-nil*` has type `(U nil (Atom1 Number))`.

This code results in a type error &mdash; Typed Clojure does not refine the type of mutable
bindings, so it cannot rule out a null pointer exception. (Typed Clojure guarantees typed
code cannot throw null pointer exceptions).

```clojure
(when *atom-or-nil*
  (swap! *atom-or-nil* inc))
```

We can convince Typed Clojure that a null pointer exception is impossible by
using an intermediate local variable. See the definition of `inc-dynamic` below.

```clojure
(ns blog.immutable.dynamic
  (:require [clojure.core.typed 
             :refer [Atom1 Int check-ns ann]]))

(ann *atom-or-nil* (U nil (Atom1 Int)))
(def ^:dynamic *atom-or-nil* nil)

(ann inc-dynamic [-> Int])
(defn inc-dynamic []
  (if-let [a *atom-or-nil*]
    (swap! a inc)
    0))

; (check-ns)
; => :ok
```

`inc-dynamic` enjoys the strong invariants provided by immutable bindings, at the cost of
a local binding. It is also obviously clear to the reader of such code that it is safe
from null pointer exceptions. There is no question about mutating `a`.

_Note_: For presentational purposes, we return `0` if `*atom-or-nil*` is `nil`.
See [`when-let-fail`](https://clojure.github.io/core.typed/#clojure.core.typed/when-let-fail), which throws an exception if the binding is a false value.

# Conclusion

Immutability reduces the cognitive load of programming. It also helps analysis tools
verify invariants about your code without particularly sophisticated techniques.

Refactoring code to take advantage of immutability
often results in clearer, more obviously correct code.

Your readers will thank you for it.

# Read more

[Occurrence Typing](https://www.ccs.neu.edu/racket/pubs/icfp10-thf.pdf) by [@samth](https://twitter.com/samth) and Matthias Felleisen.
This inference technique helps Typed Clojure infer better types at branches and assertions.

# Code

See the [code](https://github.com/frenchy64/frenchy64.github.io/tree/main/code/blog) for this blog.

Particularly:

- [locals inference](https://github.com/frenchy64/frenchy64.github.io/blob/main/code/blog/src/blog/immutable/local.clj)
- [dynamic vars](https://github.com/frenchy64/frenchy64.github.io/blob/main/code/blog/src/blog/immutable/dynamic.clj)
