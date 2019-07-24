---
layout: post
title:  "Basics of automatic annotations"
date:   2016-08-13 08:00:00
---

<img src="{{ site.url }}/images/automatic-annotations.png"
     alt="Automatic annotations logo"/>

We have covered some of the reasons
why automatic annotations are useful
in my 
<a href="{{ site.url }}/2016/08/07/automatic-annotations.html">previous post</a>,
now let's build up the infrastructure necessary to perform
it from scratch.

This work is part of a crowdfunding effort, please
support the campaign 
<a href="https://igg.me/at/typed-clojure-annotations/x/4545030">here</a>!

<hr />

<img src="{{ site.url }}/images/auto-basics/instrumentation-header.png"
     alt="Instrumenation"/>

At the center of our approach is the `track` function.
It wraps values and remembers a 'path'
to report the source of the value.

<img src="{{ site.url }}/images/auto-basics/track.png"
     alt="Specification of track"/>

How is `track` defined? 
Let's add support for flat values.

# Inferring flat values

Here's how you might define `track` to remember the type
of integers. 

```clojure
(defn track [v path]
  (cond
    (integer? v) (do (type-at-path (-class (class v)) path)
                     v)
    :else v))
```

<i>
`-class` creates a core.typed type of a given class;
`type-at-path` associates the given type with the current
path.
</i>

A 'path' is a sequence of 'path elements'. Our first path
element is the 'var path element'.
To track the value of a `def`, we rewrite it with
 the following transformation.

<img src="{{ site.url }}/images/auto-basics/track-def.png"
     alt="Track top-level binding"/>

The we track a `def` by tracking its right-hand side as
a singleton path containing just the var name.

Recall from the previous post, we are interested in two kind of
annotations: user-level vars and libraries.

<img src="{{ site.url }}/images/annotations-needed.png"
     alt="Annotations needed for top-level and library bindings"/>

We can track user-level vars by directly transforming
the `def` expressions before they are compiled.
To intercept library functions, we need to wrap each
library var dereference in a `track`.

<img src="{{ site.url }}/images/auto-basics/track-library-imports.png"
     alt="Track library imports"/>

Here's how the evaluator step through an evaluation of tracking a `def`.

<img src="{{ site.url }}/images/auto-basics/42-step.png"
     alt="Example tracking of binding"/>

1. The initial `def` is what the programmer actually writes.
2. We rewrite the plain `def` into a `track` that remembers
   the value as coming from `forty-two`.
3. We remember that `forty-two` is of type `Long`.
4. Using this type environment, we can then output the following
   correct annotation:

```clojure
(ann forty-two Long)
```

# Extensions

From here, we have many options to extend our algorithm.
In the following posts, we'll talk about:

- inferring function types,
- inferring sequence types,
- different merging strategies for types,

The basics, however, remain the same, so
this help should help as a reference
when we discuss these extensions.

<hr />

<i>
This work is part of a crowdfunding effort, please
support the campaign by clicking the banner the below
(or <a href="https://igg.me/at/typed-clojure-annotations/x/4545030">here</a>)!
</i>

<iframe src="https://www.indiegogo.com/project/typed-clojure-automatic-annotations--2/embedded/4545030" width="222px" height="445px" frameborder="0" scrolling="no"></iframe>
