---
layout: post
title:  "Macros across boundaries"
date:   2016-05-01 08:00:00
---

Typed Racket cannot export macros.
In typed modules, definitions introduce
statically
verified ("unsafe") variables,
and usages are statically verified.
and export dynamically verified
("safe") variables in untyped namespaces.
Unfortunately,
unsafe variables can ``escape'' via
the macroexpansion of typed macros, thus
macro exports are disallowed.

In contrast, Typed Clojure can export macros.
Safe variables are in scope by default.
Exported macros 
The static type system then rewrites safe occurrences
when their types are verified to avoid runtime overhead.
The drawback: all bindings must be able to generate contracts.


# The Problem

Take the following Typed Racket program that defines
a one argument function `f` that increments a number.

```racket
(define (f [x : Number])
  (add1 x))
```

Typed Racket type checks invocations of `f`
when they occur in typed contexts.

```racket
(f 1)  ;> 1 : Number
(f 'a) ;> Type Checker: type mismatch
       ;  expected: Number
       ;  given: 'a
```

We can write macros that expand to usages of $f$.
Typed Racket only checks the full expansion of
macros.

```racket
(define-syntax (m stx)
  #'(f 'a))

(m) ;> Type Checker: type mismatch
    ;  expected: Number
    ;  given: 'a
```

Exporting such macros are disallowed in Typed Racket.
Since the macroexpansion of `m` contains
an unsafe reference to `f`, using this macro
in untyped contexts can freely violate any
invariants assumed by typed code.

# A Solution: Typed Clojure

Typed Clojure takes a different approach.
Let's try the previous exercise in Typed Clojure.
We define a function `f` that increments its argument.

```clojure
(defn f [x :- Number]
  (inc x))
```

Typed Clojure then splits this definition into
two parts: an unsafe definition `f'`, which simply
renames the original variable,
and a safe definition `f`, which wraps the unsafe
version in a cast based on the static type inferred for `f'`.

```clojure
(defn f' [x :- Number]
  (inc x))
(def f (cast [Number -> Number] f'))
```

Calling `f` in typed code will rewrite it to the unsafe
version.

```clojure
(f 1) ; rewrites to (f' 1)
;=> 2 :- Number

(f 'a) ;rewrites to (f' 'a)
;  Type error, Expected Number found Symbol: 'a
```

Here's the key idea: referring to `f` in a macro defaults 
to the _safe_ binding `f`. 
Expansions in untyped contexts refer to `f`;
expansion typed contexts are _rewritten_, and refer to
`f'`. The type system rewrites post-expansion, since Typed Clojure 
operates on fully expanded code.

```clojure
(defmacro m []
  (let [a 'a]
    `(f '~a)))

(m) ; expands:  (f 'a)
    ; rewrites: (f' 'a)
    ;  Type error, Expected Number found Symbol: 'a
```

Due to the normal expansion containing only
occurrences of safe bindings,
we can freely export this macro and be sure
typed invariants are preserved.

# Drawbacks

The approach we took in Typed Clojure has its drawbacks.
Most pressingly, we must generate contracts for every top level form
in advance. For sufficiently complicated types, this is simply
not possible.
Several solutions are possible. We might selectively disable contract generation
on variables, but this exposes unsafe bindings in the same
way Typed Racket avoids by disabling macro exports.
We could provide users with the ability to assign their own
contracts or types that are "good enough" for their purposes.
We could also use a less strict contract system when we _cast_
values in safe definitions.

# Conclusion: Is this approach general?

Yes, but with the same drawbacks here: all typed
bindings must be capable of generating contracts 
based on their types.
We plan to experiment converting Typed Racket
to use this approach and evaluating whether
programs still type check.

