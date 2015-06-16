---
layout: post
title:  "Using core.typed at&nbsp;the REPL"
date:   2013-09-03 12:00:00
categories: Typed Clojure, core.typed, Clojure
---

[core.typed](https://github.com/clojure/core.typed) is intended to be used at the
REPL for rapid feedback, however there are a few things to look out for.

# Checking mode

core.typed only has effect when in checking mode. `cf` and `check-ns` are two ways
of starting checking mode.

This is most apparent when type checking a definition at the REPL.

```clojure
clojure.core.typed=> (ann my-inc [Number -> Number])
nil
clojure.core.typed=> (defn my-inc [a] (inc a))
#'clojure.core.typed/my-inc
clojure.core.typed=> (cf (my-inc 1))
Type Error (clojure.core.typed:1:34) Unannotated var clojure.core.typed/my-inc
Hint: Add the annotation for clojure.core.typed/my-inc via check-ns or cf
in: clojure.core.typed/my-inc


ExceptionInfo Type Checker: Found 1 error  clojure.core/ex-info (core.clj:4327)
```

To correctly feed forms to be checked, wrap each form in `cf`.

```clojure
clojure.core.typed=> (cf (ann my-inc [Number -> Number]))
Any
clojure.core.typed=> (cf (defn my-inc [a] (inc a)))
(Var [Number -> Number])
clojure.core.typed=> (cf (my-inc 1))
Number
```

</hr>

# Experimenting at the REPL

core.typed is designed for this workflow:

- check a namespace with `check-ns`
- keep the type environment (if checking is successful) for REPL interaction
- use `cf` for flexible small experiments, possibly producing a "dirty" environment
- another `check-ns` will wipe away previous type state

If you want even more flexibility, `check-ns` accepts a `:collect-only` keyword argument.
This collects type annotations for the target namespace (which must be provided) and
does not perform type checking. 

This is useful if you want to jump straight to REPL interactions for debugging purposes.
Perhaps you need to diagnose a particular type error, but `check-ns` keeps throwing type errors.

```clojure
clojure.core.typed=> (check-ns *ns* :collect-only true)
Start collecting clojure.core.typed
Finished collecting clojure.core.typed
Collected 1 namespaces in 686.003946 msecs
Checked 0 namespaces (approx. 0 lines) in 686.818295 msecs
:ok
```

</hr>

# More resources

For more advice on how to effectively leverage core.typed, 
join the core.typed [mailing list](https://groups.google.com/forum/?fromgroups#!forum/clojure-core-typed)
or #typed-clojure on Freenode and start a conversation.
