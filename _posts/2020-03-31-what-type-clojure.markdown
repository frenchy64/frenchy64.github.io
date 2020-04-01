---
layout: post
title:  "What are types in Clojure?"
date:   2020-03-30 08:00:00
---

Recently, Jim Newton reached out to me to clarify a
[post](https://clojureverse.org/t/determining-subtype-at-runtime/5614)
he submitted to ClojureVerse.

Jim is doing [interesting work](https://www.lrde.epita.fr/dload/papers/newton.18.phd.pdf) on defining heterogeneous types in
Common Lisp, with a set-theoretic flavor. In ongoing followup work, he is generalizing his approach to
to other languages and type systems, and he wants to learn more about Clojure
and how it relates to Common Lisp, and how [Typed Clojure](https://github.com/typedclojure/typedclojure) and
[clojure.spec](https://github.com/clojure/spec.alpha) fit into the story.

I'm glad Jim reached out, since I had no idea Common Lisp included a formal definition of
of a [type](http://www.lispworks.com/documentation/lw51/CLHS/Body/04_bc.htm) with a
[subtyping algorithm](http://clhs.lisp.se/Body/f_subtpp.htm) that can return 3 answers
("yes", "no", "maybe").

<hr />

Jim asks:

```
1. What is a 'type' in Clojure?
2. Does the isa? function implement subtyping?
3. Is isa? decidable?
```

My response:

There are a few perspectives to answer this from, since:

1. Clojure has no official standard (unlike Common Lisp),
2. Typed Clojure is merely an unofficial optional type checker, and
3. spec is just a library.

# Clojure's perspective

Clojure has nothing as ambitious as [SUBTYPEP](http://clhs.lisp.se/Body/f_subtpp.htm), nor a rigorous definition of a "type".
There's not much to see here, Clojure has no formal semantics or specification.

[isa?](https://github.com/clojure/clojure/blob/38bafca9e76cd6625d8dce5fb6d16b87845c8b9d/src/clj/clojure/core.clj#L5564-L5583)
is very simple and decidable. Here is the complete definition.
The most "interesting" part is that it uses
[isAssignableFrom](https://docs.oracle.com/javase/8/docs/api/java/lang/Class.html#isAssignableFrom-java.lang.Class-),
which is also decidable.

```clojure
(defn isa?
  "Returns true if (= child parent), or child is directly or indirectly derived from
  parent, either via a Java type inheritance relationship or a
  relationship established via derive. h must be a hierarchy obtained
  from make-hierarchy, if not supplied defaults to the global
  hierarchy"
  {:added "1.0"}
  ([child parent] (isa? global-hierarchy child parent))
  ([h child parent]
   (or (= child parent)
       (and (class? parent) (class? child)
            (. ^Class parent isAssignableFrom child))
       (contains? ((:ancestors h) child) parent)
       (and (class? child) (some #(contains? ((:ancestors h) %) parent) (supers child)))
       (and (vector? parent) (vector? child)
            (= (count parent) (count child))
            (loop [ret true i 0]
              (if (or (not ret) (= i (count parent)))
                ret
                (recur (isa? h (child i) (parent i)) (inc i))))))))
```

It turns out isa? is central to how Clojure multimethods work, so my [ESOP16](https://frenchy64.github.io/papers/esop16-short.pdf)
paper goes into depth about how to model it in a type checker. But isa? is pretty much unrelated to subtyping a la SUBTYPEP.

# clojure.spec's perspective

[clojure.spec](https://github.com/clojure/spec.alpha) implements runtime predicates and destructuring similar
to the runtime portion of your [ESL17](https://www.lrde.epita.fr/wiki/Publications/newton.17.els) paper. It has the same regex operators
for sequences, for example.

However I don't know of any rigorous work of defining a subtyping algorithm between
specs. Actually, I'm [in the middle](https://github.com/typedclojure/typedclojure/blob/7ba8eb9956d016b5878c9dfab770c885226bd1be/typed/clj.spec/src/typed/clj/spec/subtype.clj)
of thinking about how to do this, so it's very convenient
that you brought your [ESL17](https://www.lrde.epita.fr/wiki/Publications/newton.17.els)
paper to my attention, since I was going to start looking
at [XDuce](https://en.wikipedia.org/wiki/XML_transformation_language#Existing_languages)/[CDuce](https://en.wikipedia.org/wiki/CDuce) for inspiration on how to deal with semantic subtyping with regex ops.

In summary, spec has no formal semantics, and is somewhat self-describing in some
ways. eg., integer? is a spec/type that represents all values that passes the integer? predicate.
If you're looking for a more rigorous definition of spec, you might enjoy
looking at [my quals](https://ambrosebs.com/talks/quals-answers.pdf) where I talk more about spec in Q2 and define a formal model
of spec in Q3.

# Typed Clojure's perspective

Types in Typed Clojure are not set-theoretic (though that's a future direction I'm working towards).

[Subtyping](https://github.com/typedclojure/typedclojure/blob/7ba8eb9956d016b5878c9dfab770c885226bd1be/typed/clj.checker/src/clojure/core/typed/checker/jvm/subtype.clj#L141) is straight out of [TAPL](https://www.cis.upenn.edu/~bcpierce/tapl/) with equi-recursive types.

Local type variable inference is straight from [LTI](https://www.cis.upenn.edu/~bcpierce/papers/lti-toplas.pdf)
(Pierce & Turner) with extensions
from Strickland et al.'s [ESOP09](https://www2.ccs.neu.edu/racket/pubs/esop09-sthf.pdf).

I've played with a few extensions to interleave symbolic execution, but definitely not set-theoretic.

Thanks,
Ambrose
