---
layout: post
title:  "The Road to Typed Clojure 1.0: Part 1"
date:   2018-09-20 02:00:00
---

<i>
What set of features would deserve a 1.0 release for Typed Clojure?
</i>

<i>
We've learnt valuable lessons from real-world developers about the pain
points of using Typed Clojure, and after several years of mulling it over,
we have a much better idea of how we might improve it.
This series of posts will outline our proposed solutions, and give an impression of what we hope
for Typed Clojure 1.0.
</i>

<hr />

# The Problems

CircleCI has a great <a href="https://circleci.com/blog/why-were-no-longer-using-core-typed/">blog post</a>
outlining numerous issues with Typed Clojure in practice.

They boil down to:

1. Type checking is too cumbersome to be interactive
2. Hard to distinguish user-errors from type system shortcomings
3. Not enough community support for library annotations
4. Poor inference of higher-order function usage

I've <a href="https://frenchy64.github.io/typed/clojure,/core.typed,/clojure/2013/09/03/polymorphic-hof.html">already</a> written about the limitations of the local type inference Typed Clojure uses.
In this post, we'll look at some interesting new directions
in local type inference that will help type check more programs,
with less local annotations, and more helpful error messages.

# Improved Local Inference

When we call higher-order functions as programmers,
we're implicitly reasoning about the flow of values
through and between functions.
Can we automatically recover this information to help
the type checker perform similar reasoning?

Let's use `map` as an example. The control flow of 
the following expression

```clojure
(map (fn [x] x) [1 2 3])
```

can be described in two steps.

1. Call `(fn [x] x)`{:.language-clojure .highlight} on elements of `[1 2 3]`{:.language-clojure .highlight},
2. return a sequence of those results.

In a picture:

```clojure
             2.
             /---------------v
(map (fn [x] x) [1 2 3]) ;= [1 2 3]
          ^------/
                 1.
```

If we look at the type of `map`, could we recover the same information?

```clojure
(ann map (All [a b]
           [[a -> b] (Seqable a) -> (Seqable b)]))
```

Yes. We notice the first argument has type `[a -> b]`{:.language-clojure .highlight}, and so
_requires_ a value of type `a` before it produces a value of type `b`.
The second argument is of type `(Seqable a)`{:.language-clojure .highlight}, and is thus our only
source of `a`'s. Let's draw this first step.

```clojure
(ann map (All [a b]
           [[a -> b] (Seqable a) -> (Seqable b)]))
             ^----------------/
                              1.
```

The second step involves collecting the `b`'s the function returns
and returning a sequence of them. Since the only way to get a `b`
is from the function argument, we can draw another arrow.

```clojure
(ann map (All [a b]
                  2.
                  /--------------------------v
           [[a -> b] (Seqable a) -> (Seqable b)]))
             ^----------------/
                              1.
```

There are many potential ways this kind of control-flow information can help more type check Clojure code,
and provide more informative error messages.
Here are some ideas.

## Avoid annotating anonymous fn's

Take the following code, where `f` and `g` are
unannotated anonymous functions like `(fn [x] ...)`{:.language-clojure .highlight}.

```clojure
(let [h (comp f g)]
  (map h c))
;=> r
```

We can delay checking `f` and `g` until
`h` is called, and wait for more type information
from the `map` expression.

```clojure

              v-\ 3.
(let [h (comp f g)]
              | ^
     2./--------/
       |    4.\--v
  (map h c)) ;=> r
       ^-/ 1.
```

There are 4 control flow edges above.

1. Arguments flow from `c` to `h`.
2. The same argument is forwarded to `g`.
3. `g`'s result is passed to `f`.
4. A sequence of `f`'s results returned as `r`.

All this can be justified just from the types of `map`
and `comp`. Here's how to derive the information from `comp`.
The control flow of an expression `((comp f g) v)`{:.language-clojure .highlight} looks like:

```clojure
       v----\ 2.
((comp f    g)    v) ;=> v'
       |    ^-----/ 1.   ^
     3.\-----------------/          
```

Notice that without `v`, there is nothing to call `g` with,
so it's safe to assume `(comp f g)`{:.language-clojure .highlight} by itself can never 
call `g`, and, by transitivity, `f`.

The type of `comp` has this exact information: the output of
`comp` must be called before either `comp` argument is called.

```clojure
(ann comp (All [a b c]
              v-------------\ 2.
            [[b -> c] [a -> b] -> [a -> c]]))
              |        ^-----------/ 1. ^
            3.\-------------------------/
```

In words, once `a` is provided:
1. `a` flows to the second argument.
2. `b` flows to the first argument.
3. `c` flows to the return.

 We can justify a delayed check of `comp`'s arguments
 because providing `a` "kicks it all off".

## Error messages detailing inference strategy

```clojure
;   CircleCI's blog post rightly complained that error messages
;   often don't provide enough guidance for a user to identify
;   the cause(s) of an error.

;   By using the same control flow information as above, we can
;   do a better job of explaining why some applications fail.
;   For example, take

(comp inc boolean)

;   Typed Clojure currently reports an error which includes the types of
;   `comp`, `inc`, and `boolean`, and leaves the user to diagnose
;   the issue.

;   Instead, we can incorporate the control flow information into the
;   error message like so:

  Type Error:
  Input to first argument of comp must accept the output of
  the second argument of comp.
  
  In the following diagram, `b` was inferred as Boolean from
  the result of `boolean`, but it flowed to the input of `inc`,
  which only accepts Number.
  
  
   [[b -> c] [a -> b] -> [a -> c]]
     ^             |
     \-------------/
  in: (comp inc boolean)
```

## Type check transducers

```clojure
;   Transducers in Clojure are a frequent source of 
;   anonymous functions.
;   Transducers "compose left-to-right", so the following
;   code first increments elements of `c`, then decrements.

(into []
      (comp (map (fn [x] (inc x)))
            (map (fn [y] (dec y))))
      c)
;=> r

;   We could build a similar control flow graph from
;   the types of `into`, `comp` and `map` that allow
;   us to propagate the type of `c` to the formal
;   parameter `x`.

(into []
    /-----------------v
    | (comp (map (fn [x] (inc x)))
    \       (map (fn [y] (dec y))))
     \
      c)
;=> r


;   Then, having a type for `x`, would trigger the checking
;   of the subsequent composed transducers.
```

## Supporting hard-to-check map operations

```clojure
;   Clojure has a host of handy map operations like `get-in`,
;   `assoc-in`, and `select-keys`. We can "reify them at the
;   type level" and then use control flow analysis to
;   type check operations "in order" (of evaluation).

;   For example, `update` updates a map at a given entry
;   with a function.

(update {:a 1} :a inc) ;=> {:a 2}

;   By reifying `get` and `assoc` at the type level, we
;   could annotate `update` like so:

(ann update (All [m k v]
              [m k [(Get m k) -> v] -> (Assoc m k v)]))

;   Here's a useful "order" to check the type variables,
;   so we avoid any hard questions about running `get`
;   or `assoc` "backwards.

(ann update (All [m k v]
                               2./----------------v
              [m k [(Get m k) -> v] -> (Assoc m k v)]))
            1. \ |       ^ ^                  ^ ^
                \--------/ /------------------/ /

;   In words, first we infer the value of the map and key
;   from the first two arguments to `update`. Then we have
;   enough information to "call" the third argument, which
;   gets propagated to `Assoc`, which itself now has
;   "ground" types so is easy to calculate.

;   This is enough information to help check anonymous fn's
;   passed to `update`, like:

(update {:a 1} :a #(+ 42 %)) ;=> {:a 2}

;   Notice that the control flow happens to go left-to-right
;   in `update`, but as we've seen in other examples like
;   `comp`, it could just as well be reversed and we could
;   derive the same information.
```

##  Better "not-supported" Type Errors

```clojure
;   If for some reason we can't infer the type of a local
;   fn, instead of making a last-ditch effort to check
;   the fn with argument types Any, we can simply ask the
;   user to annotate the fn.

;   For example, checking the following function at type
;   [Any -> ?]

(fn [x] (inc x))

;   Gives a confusing error message that `inc` does not
;   accept type `Any`. Now, since we could lean on control
;   improved inference to check most fn's, we can reasonably
;   be more conservative here.
```

# Conclusion

I'm very excited at the possibilities here. It's still early
days for this research, but surely, if you can intuitively
reconstruct the information for a given application based
on local information, there should be an algorithm to 
recover it, right? :)

# Next post

In the next few posts, we'll describe our approach to improving
feedback time from the type checker, a framework for providing
custom type rules for macros, and explain how
automatic library annotations can help mitigate the lack of community-provided
annotations.
