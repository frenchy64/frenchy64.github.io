---
layout: post
title:  "Automatic Annotations: Inferring Function Types"
date:   2016-08-15 08:00:00
---

<img src="{{ site.url }}/images/automatic-annotations.png"
     alt="Automatic annotations logo"/>

Previously, we covered
<a href="{{ site.url }}/2016/08/07/automatic-annotations.html">why automatic annotations are useful</a>,
and some 
<a href="{{ site.url }}/2016/08/15/runtime-infer-basics.html">basics underlying the infrastructure to perform automatic annotations</a>.

In this post we'll see how function types are inferred,
as well as simple map types.

This work is part of a crowdfunding effort, please
support the campaign 
<a href="https://igg.me/at/typed-clojure-annotations/x/4545030">here</a>!

<hr />

<img src="{{ site.url }}/images/fn-infer/inferring-functions.png"
     alt="Inferring Functions"/>

Automatic annotations for Typed Clojure only work if
you provide tests. Let's suppose we have the following
`point` function with two unit tests.

<img src="{{ site.url }}/images/fn-infer/point-code.png"
     alt="Definition of point with tests"/>

As in the
<a href="{{ site.url }}/2016/08/15/runtime-infer-basics.html">previous post</a>,
to track the value of a `def` you track its initial value.

<img src="{{ site.url }}/images/fn-infer/track-def.png"
     alt="Tracking a function definition"/>

Now we have an interesting problem: how do we track
functions?
We need to learn two things about functions, namely its
inputs and output.
By wrapping the original function, we can track
each of these subcomponents.

<img src="{{ site.url }}/images/fn-infer/track-fn.png"
     alt="Tracking a function invocation"/>

Since this waits for the function to be called,
we need tests to exercise the function.
This is the intuition behind <i>Write Tests, Get Types!</i>.

Let's track `point`.

<img src="{{ site.url }}/images/fn-infer/track-point1.png"
     alt="Further tracking of function"/>

Notice we have added two new path elements corresponding
to the input and output of functions.
We can combine these to  make arbitrarily 
deep paths, and encode statements like <i>the first
parameter's second parameter of var foo-bar is of
type Long</i>.

Let's step through the evaluation of `(point 1 2)`.
First, the wrapped function is called and
we are left with a call to the original function.

<img src="{{ site.url }}/images/fn-infer/track-point2.png"
     alt="Further tracking of function"/>

We then evaluate the first argument to `point`, which
tracks `1`.

<img src="{{ site.url }}/images/fn-infer/track-point3.png"
     alt="Further tracking of function"/>

After this reduction, we infer the first argument to be
of type `Long`.
We then do the same for the second argument.

<img src="{{ site.url }}/images/fn-infer/track-point4.png"
     alt="Further tracking of function"/>

Now we have evaluated the arguments, we invoke the actual
`point` function.

<img src="{{ site.url }}/images/fn-infer/track-point5.png"
     alt="Further tracking of function"/>

It returns a map; to track a map, we need a new kind of
path element, <i>key path elements</i>, to track map entries.
We push the `track` inside the map's values, using key path
elements.

<img src="{{ site.url }}/images/fn-infer/track-point6.png"
     alt="Further tracking of function"/>

Notice the richness of the paths here: `1` is point's range's
`:x` entry.
Reducing the first argument fills in part of the return type.

<img src="{{ site.url }}/images/fn-infer/track-point7.png"
     alt="Further tracking of function"/>

The final reduction completes the type of `point`.

<img src="{{ site.url }}/images/fn-infer/track-point8.png"
     alt="Further tracking of function"/>

Great, we have a type, but there is a mystery step we have
skipped: how we get from inference results like 
`[point (:dom 0)] : Long` and `[point (:dom 1)] : Long`
to final var types like 
`point : [Long Long -> '{:x Long :y :Long}]`.

This is handled by converting inference results
to types, then combining then with `join`.
For example, 
the type for `point` after inferring its arguments is

```clojure
join([Long ? -> ?], [? Long -> ?]) = [Long Long -> ?]
```

Each new inference result is joined in this way,
so inferring the function return type is similar.

# Inferring Functions via Tests

The basic approach of inferring types for functions
is to wrap them and inspect their behaviour
when invoked.
The parameters and return types are incrementally
inferred, and combined with the `join` metafunction.

In the next post, we talk about inferring types for
possibly-infinite sequences and vectors.

<hr />

This work is part of a crowdfunding effort, please
support the campaign 
<a href="https://igg.me/at/typed-clojure-annotations/x/4545030">here</a>!
