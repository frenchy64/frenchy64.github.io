---
layout: post
title:  "Typed DOM interactions with Typed Clojurescript: Part 1"
date:   2013-10-11 00:00:00
---

An interesting aspect of type checking a Clojure dialect is dealing with host interop.

In Typed Clojure, Java interop is handled by utilising Java type information. 
For the collected Java types to be sound in Typed Clojure, we then transform them to
deal with corner cases in the Java type system like covariant arrays and 
[null](https://frenchy64.github.io/2013/10/04/null-pointer.html).

In checking Clojurescript, we need to type check interactions with Javascript. Clearly Javascript
does not provide its own type annotations like Java.

## Checking DOM interactions

The nature of gradual typing means we only need to annotate the _interface_ between typed and untyped
code. In practice, this means all Javascript calls found via type checking with Typed Clojurescript must
be annotated.

Take this helper function `by-id`, a thin wrapper around a Javascript method call.

```clojure
(ann by-id [string -> (U nil js/HTMLElement)])
(defn by-id [id]
  (.getElementById js/document id))
```

Typed Clojurescript can statically verify the expected annotation of `by-id` if it knows 

- `js/document` is of type `js/Document`
- an instance of `js/Document` has a method of type `[string -> (U nil js/HTMLElement)]`.

This information needs to be provided explicitly, and the current implementation of Typed Clojurescript
[provides a small base environment](https://github.com/clojure/core.typed/blob/main/src/main/clojure/clojure/core/typed/base_env_cljs.clj#L40).

## Future work

Clearly the amount of work to _manually_ annotate the entirety of the DOM is not worth the effort.
However, there are several sources of inspiration for automating this effort.

- [TypeScript](https://www.typescriptlang.org/) annotates the DOM and provides type checking on its usage.
- The [DOM4 spec](https://www.w3.org/TR/domcore/) includes detailed type signatures for the DOM.

This work is an important deliverable for the Typed Clojure crowdfunding campaign. The longer I can
work on Typed Clojure, the more viable Typed Clojurescript will become.

_In Part 2, we look at why type checking Clojurescript is more satisfying than type checking Javascript_

<div>
  <a href='https://www.indiegogo.com/projects/typed-clojure/'>
    <img src='{{ site.url }}/images/typed-clojure-2013-campaign-60pc.png'
         alt="Crowdfunding campaign"/>
  </a>
</div>
