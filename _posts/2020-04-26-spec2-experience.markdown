---
layout: post
title:  "Spec2 experience"
date:   2020-04-26 08:00:00
---

Over the last few months I've been building [typed.clj/spec](https://github.com/typedclojure/typedclojure/blob/master/typed/clj.spec/README.md)
on top of [spec-alpha2](https://github.com/clojure/spec-alpha2), the new and
improved version of Clojure spec that Alex, Rich and others are building.

Of course, I haven't been using spec-alpha2 "as intended",
so I don't have opinions here about open vs closed maps, or the new
`select` operations that are front-and-center in spec2's marketting.

My pithiest description of typed.clj/spec is it's a spec _metalanguage_.
In more words, it provides specs that reason about other specs.

Thus, as usual I have a bunch of experience with corners of spec2 that
perhaps are less interesting to the layperson, and might even be so
obscure that even the maintainers of spec2 don't care :)

With that context, here's some feedback about my experience with spec2,
and how I solved some challenges I faced.

<hr />

Right off the bat, spec2 is much more pleasant to build on top of than [spec1](https://github.com/clojure/spec.alpha).
A library like typed.clj/spec is probably possible with spec1, but
the emphasis on specs-as-data with spec2 is very nice.

The big difference between spec2 and spec1 in terms of spec representation
is this concept of a "symbolic spec", which is an extensible notion
of a spec that can be created both programatically from data and
conventionally using macros.
The key new operator is `resolve-spec`, which creates a spec from its
data representation, with extension points `create-spec` and `expand-spec`.

# Binders

The first challenge with typed.clj/spec was to create a "binder" for
specs, such that you could write specs like 

```
for all specs x,
  is a function from x to x
```

This is a bit of a puzzler with spec2, since all symbolic specs "explicate"
(fully expand) their arguments. 
Effectively, if we use symbols for local variables (like `fn` and `let`),
explicate might turn our example into:

```
for all specs user/x,
  is a function from user/x to user/x
```

To get around this, I simply decided to use unqualified keywords as the
syntactical representation of type variables.

```
for all specs :x,
  is a function from :x to :x
```

Still, this is unsatisfactory when it comes to variable-substitution.
What if that body of the spec has `:x` meaning something other than
a type variable?

```
for all specs :x,
  is a function from (s/cat :x :x) to :x
```

To get around this, I introduced a macro `typed.clj.spec/tv` that represents a type-variable occurence.

```
for all specs :x,
  is a function from (s/cat :x (tv :x)) to (tv :x)
```

There is a related problem, where we want our polymorphic binder to be able to shadow
variables, such as in:

```
for all specs :x,
  for all specs :x,
    is a function from (s/cat :x :x) to :x
```

Representing specs as data enabled a neat and extensible solution to shadowing,
which I won't detail here. The end result is 
a new macro `typed.clj.spec/all` that represents the "for all" statement above,
and a "binder" macro `typed.clj.spec/binder` that is somewhat related to `s/cat`.

```clojure
(s/def ::identity
  (t/all :binder (t/binder :x (t/bind-tv))
         :body (s/fspec :args (s/cat :x (t/tv :x))
                        :ret (t/tv :x))))
```

To instantiate this polymorphic spec, the implementation
replaces `(t/tv :x)` with the chosen spec.

```clojure
user=> (-> (t/inst ::identity {:x int?})
           s/form)
(s/fspec :args (s/cat :x int?)
         :ret int?)
```

# Function specs and Explicate

Unfortunately, `explicate` is a little too eager to resolve symbols
when it comes to function specs, which leads to some very obscure
error messages.

It is impossible to shadow a global variable using a function spec.
For example, if you create a spec for `count` on strings, you might
write the following spec:

```clojure
(s/def ::string-count
  (s/fspec :args (s/cat :str string?)
           :fn (fn [{ {:keys [str]} :args
                     :keys [ret]}]
                 (= (count str) ret))
           :ret int?))
```

Unfortunately, `explicate` resolves both occurrences of `str`
to `clojure.core/str`, as we can see from the spec's (prettyfied) expansion:

```clojure
user=> (s/form (s/resolve-spec ::string-count))
(fspec ...
  :fn (fn [{ {:keys [clojure.core/str]} :args,
           :keys [ret]}]
        (= (count clojure.core/str)
           ret)))
```

Clearly, not what the programmer intended. Effectively, any binding operator
has unpredictable results if used in a function spec, such as `let` or `fn`.

```clojure
user=> (s/explain (s/resolve-spec ::string-count) count)
;Execution error (UnsupportedOperationException)
;  at spec2-fn/eval11966$fn (REPL:7).
;count not supported on this type: core$str
```

I did some digging in spec1+2 and there seems to be hard-coded support
for `#(.. % ..)` which works well if no macros/special-forms are used in
its body.

As a workaround, you can always pull out the implementation of your function spec
using a regular Clojure def, and hook it up with the `#()` special form,
like so:

```clojure
(def string-count-fn
  (fn [{ {:keys [str]} :args
       :keys [ret]}]
    (= (count str) ret)))

(s/def ::string-count
  (s/fspec :args (s/cat :str string?)
           :fn #(string-count-fn %)
           :ret int?))
```

This is resilient even if you `(def % ...)` in the current namespace
(making a resolvable `%`
breaks even `(fn [%] (string-count-fn %))` as the `:fn`).

# fspec iterations

The particular approach I took in conforming `t/all` specs
had unfortunate interactions with `fspec`.

The intuition behind my approach says, to
test if `clojure.core/identity` conforms to `::identity`,
instantiate `:x` to several singleton specs and conform
`clojure.core/identity` to each resulting spec.

For example, the conform

```clojure
(s/conform ::identity identity)
```

might internally reduce to the following conforms:

```clojure
(s/conform (s/fspec :args (s/cat :x #{1})
                    :ret #{1})
           identity)
(s/conform (s/fspec :args (s/cat :x #{nil})
                    :ret #{nil})
           identity)
(s/conform (s/fspec :args (s/cat :x #{\a})
                    :ret #{\a})
           identity)
```

This almost works--the only wrinkle is that each sub-conform
on `fspec` will call `identity` on exactly the same input
around 20 times due to `s/*fspec-iterations*`.

I manually rebound `s/*fspec-iterations*` when this became a performance problem,
but I found myself wanting finer-grained control of fspec iterations with
nested fspec's. It might look like the following:

```clojure
(s/def ::nested-fspecs
  (t/all :binder (t/binder :x (t/bind-tv))
         :body (s/fspec
                 :iterations 1
                 :args (s/cat :f
                         (s/fspec
                           :iterations 10
                           :args (s/cat :x integer?)
                           :ret (t/tv :x)))
                 :ret (t/tv :x))))
```

This example doesn't make much sense since the `:f` arg is generated
and not conformed, but note the `:iterations` syntax. Unsure
if this would actually be useful.

# Instrumentation

I've long wondered what instrumentation would look like for polymorphic
specs/contracts. Racket using runtime-sealing, which has crippling
consequences that is simply not compatible with spec's raison d'etre.

I have a few ideas, but if I figure it out it would nice if
`s/instrument` was extensible to support more than just `fspec`.
For example, instrumenting `::identity` on `clojure.core/identity`.
