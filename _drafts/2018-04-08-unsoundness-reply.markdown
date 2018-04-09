---
layout: post
title:  "Who doesn't want unsound type systems?"
date:   2018-04-09 01:00:00
---

<i>
David Foster published a <a href="http://dafoster.net/articles/2018/04/07/unsound-type-systems-are-still-useful/">thoughtful response</a> to my <a href="/2018/04/06/unsoundness-in-untyped-types.html">previous post</a> on unsoundness, vouching for unsound type systems as useful and worthy of research.
I really like this sentiment--unsound systems can provide usability that is ahead of its time, while motivating entire fields of research! Here's my perspective.
</i>

<hr/>

A “(static) type system” is a discipline that helps design, verify, and document programs. A type system is either “sound” (can correctly predict the possible results of any computation) or “unsound” (sometimes gets it wrong). Perhaps surprisingly, there are plenty of legitimate reasons to introduce “unsoundness”. Even the notion of “soundness” is a moving target, and must be carefully qualified in each language--a type system is both “sound” _relative_ to some subset of its features, and _up-to_ a subset of “unsound” features.

Given how valuable it seems having a _sound_ type system--that is, having an oracle to predict how your program runs--why is unsoundness even in the picture? Historically, “unsound” type system features have been implied to mean “defective”, accidentally introduced due to some oversight or misunderstanding that should be fixed. But, these type systems were still “sound” _up-to_ these “defective” features. Java (pre-generics) has a type system that is sound _up-to_ features like null-pointers and writing to arrays--but these features are enforced at runtime, so they're only _statically_ unsound features. TypeScript's type system is sound _up-to_ first class functions and downcasts from the `any` type, neither being enforced at runtime. The Dialyzer's type system aggressively avoids false-negatives, and could be characterized as sound _up-to_ variable references (at the very least--there must be more to say here). There is an incredibly rich spectrum of soundness, and this spectrum is worth exploring due to the contradictory force of _completeness_--sometimes what is meant by “usability”.

A _complete_ type system never rejects correct programs. When faced with soundness versus completeness tradeoffs, type system designers tend towards preserving soundness--rejecting some correct programs is more satisfying than accepting some incorrect ones. Depending on your goals for the type system, however, this might not always be the right decision. I imagine the designers of Java decided that using mid-1990’s type theory to soundly handle the null-pointer in the type system was not worth the cost to completeness (“usability”): too many correct programs would be rejected and threaten to turn away their core C++ target audience. So, because of the state of type research and its intended audience, Java's type system is “unsound” _relative-to_ null-pointers, but “complete” _relative-to_ null-pointers--no correct program will be rejected by adding null-pointers to its source (so long as the result is still a correct program).

The tension between soundness and completeness is a key to understanding examples of unsoundness found in many type systems. There are two teams in this tug of war--the soundness-sacrificers and the soundness-preservers--and, depending on your perspective, each team is unfairly matched! Both sides want completeness (usability), and “pull” to increase the number of correct programs their type systems can accept. The soundness-sacrificers are willing to forgo some soundness in the name of completeness (usability) _today_; they create forgiving type systems, shedding many technical challenges in creating the type system. Meanwhile, the soundness-preservers play the long game, and see supporting this new set of features _soundly_ as a compelling technical challenge. On the other hand, the soundness-sacrificers often do the work of popularizing new realms of completeness (usability) and had to be inventive enough to recognize the soundness tradeoff in the first place. The soundness-preservers, instead, can borrow these insights as initial sparks for research directions. (This can also go the other way, with soundness-sacrificers removing hard-to-understand, but proven sound features for a less demanding audience.) The soundness-sacrificers are doing pretty well for themselves in 2018, but the soundness-preservers often catch up in time.

Here’s an example to bring all this together: Java’s famous handling of the null-pointer. The soundness-sacrificers designing the Java type system made the first move. Instead of investing decades of research into soundly incorporating null-pointers into a sound type system, they made a type system that is ignorant of null-pointers, compensating by enforcing soundness via runtime checks. They “tugged” the soundness-preservers over the line by being able to accept more correct programs than the soundness-preservers, using current knowledge. Many years later, the soundness-preservers designing Kotlin and Ceylon applied recent advances in control-flow analysis <a href="https://www2.ccs.neu.edu/racket/pubs/icfp10-thf.pdf">(Tobin-Hochstadt & Felleisen, 2010)</a> to “pull back” the number of correct programs they can accept involving null-pointers, while keeping compile-time soundness. This tug-of-war is thrilling for both sides--the initial thrill of pulling away with usable type systems, followed by the thrill of the technically challenging chase--and everyone wins in the end.

<hr/>

<i>
The idea of soundness “up-to” I first heard from
Matthias Felleisen, perhaps in <a href="https://www.youtube.com/watch?v=JBmIQIZPaHY">this video</a> from STOP 2015.
(I can’t check because I have YouTube blocked for my own health.)
To my knowledge, the term doesn’t come up in the literature, so I’ve done my best
reverse engineering what it means. Publishing this blog post is probably
the fastest way for me to be set straight anyway.
</i>
