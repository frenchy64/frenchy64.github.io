---
layout: post
title:  "Typed Clojurescript Playground"
date:   2013-10-06 00:00:00
---

Typed Clojurescript is in early development, but it's still fun to play around with.
It's designed to be very similar to Typed Clojure. 
The usual vars like `ann` and `ann-form` are identical to the Clojure implementation,
except the prefix of `clojure.core.typed` is replaced by `cljs.core.typed`. 

One of the major goals of the Typed Clojure [crowdfunding campaign](http://www.indiegogo.com/projects/typed-clojure) 
is to work on Typed Clojurescript.
It will save me a lot of time if enthusiastic Clojurescripters can try type checking Clojurescript code and
report what works. This way, I'm spending time directly fixing bugs, instead of searching for them.

I have created a [Typed Clojurescript playground](https://github.com/frenchy64/typed-clojurescript-play)
for this purpose. Just clone it and run `lein typed check-cljs` to start type checking.

I am building Typed Clojurescript so that it shares much of the implementation of Typed Clojure. We get
advanced features like occurrence typing and variable-arity polymorphism straight up, because it's already
implemented for Clojure.

It should be relatively quick to bring Typed Clojurescript up to a useful state. If you are excited about
Typed Clojurescript, why not give it a go? It will mature much faster.
