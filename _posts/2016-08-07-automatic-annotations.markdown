---
layout: post
title:  "Introducing Automatic Annotations"
date:   2016-08-07 08:00:00
---

<img src="{{ site.url }}/images/automatic-annotations.png"
     alt="Automatic annotations logo"/>

Annotations in Typed Clojure are essential, but tedious
to write.
We need a tool to generate type annotations automatically,
based on the specifications we already give via tests.

In this post, I set the stage for why this tool is necessary.
We discuss how it

- provides valuable generated documentation at any point in the development cycle,
- assists in the effort to generate contracts for untyped code to participate in gradual typing, and
- helps static type checking by reducing the annotation effort.

This work is part of a crowdfunding effort, please
support the campaign <a href="https://igg.me/at/typed-clojure-annotations/x/4545030">here</a>!

# The Annotation Burden

Below is a 
function written in Clojure. It creates a `point` 
hash-map that acts as a record.

<img src="{{ site.url }}/images/clojure-point.png"
     alt="Defining a point in Clojure"/>

Here is the same function in Typed Clojure.

<img src="{{ site.url }}/images/example-annotations.png"
     alt="Defining a point in Typed Clojure"/>

The only difference between the typed and untyped
code is *annotations*.
Most effort in porting to Typed Clojure is spent manually
creating these annotations.

# Why Automatic Annotations?

There are many motivations for automatic type
annotations. 
Here are three.

<img src="{{ site.url }}/images/on-demand-docs.png"
     alt="On demand documentation"/>

The first motivation is automatic documentation
for partial programs.

<img src="{{ site.url }}/images/current-shape.png"
     alt="Based on the current tests, what are the inputs/outputs of a function?"/>

The REPL-driven style of Clojure development often
leaves our functions in unfinished states.
Using TDD means we exercise the paths of code
we are interested in, and summaries of these types 
in annotations at any time can help write consistent code.

<img src="{{ site.url }}/images/contract-gen.png"
     alt="Contract generation"/>

The second motivation is runtime checking.

<img src="{{ site.url }}/images/generate-contract.png"
     alt="Assert types as contracts"/>

Using the infrastructure
developed for
<a href="{{ site.url }}/2015/06/19/gradual-typing.html"/>gradual typing</a>,
we can use annotations to assert runtime contracts.

<img src="{{ site.url }}/images/import-untyped-boundary.png"
     alt="Import untyped boundary"/>

Gradual typing allows us to treat untyped code as safe when
passed to typed code, since a runtime contract violation will prevent
ill-typed operations.

<img src="{{ site.url }}/images/static-type-checking.png"
     alt="Static type checking"/>

The third motivation is fuel for type checking. 
Type annotations are needed to help the type system
use local, instead of global, reasoning to type check code.
This is the main spark for this project.

<img src="{{ site.url }}/images/annotations-needed.png"
     alt="Annotations needed for top-level and library bindings"/>

Of the two main kinds of annotations needed, library
annotations are the most annoying.
Each library function used requires an annotation.

Interestingly,
these annotations do not need to be 100% accurate
to be useful.

<img src="{{ site.url }}/images/auto-workflow.png"
     alt="Goal: Mostly correct annotations"/>

We generate 'mostly correct'
annotations, and fixing these types is part of 
the workflow.
Simple functions will successfully type check with
the generated annotations, and more complicated functions
will present you with a mostly complete annotation.

# Introducing Automatic Annotations

<img src="{{ site.url }}/images/this-work-purpose.png"
     alt="This work generates type annotations"/>

Our automatic annotator generates types that
are useful to document and verify
(typed or untyped)
code, using either runtime or compile-time 
techniques.

# Next posts

That wraps up my introduction to automatic annotations
for Typed Clojure.
Upcoming posts will discuss implementation strategies,
generating `clojure.spec` types (Write Unit Tests, Get Generative Tests!)
and unique annotation problems related to how Clojure programmers
use data.

If you enjoyed this post, please consider [donating
to the campaign](https://igg.me/at/typed-clojure-annotations/x/4545030)!
