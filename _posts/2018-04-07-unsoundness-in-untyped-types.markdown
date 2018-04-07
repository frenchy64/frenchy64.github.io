---
layout: post
title:  "Are unsound type systems wrong?"
date:   2018-04-07 01:00:00
---

<i>
This is an essay I wrote early-2016.
After battling with soundness vs. usability in Typed Clojure for many
years, I was starting to reconsider the "obviously wrong" approach
of baking unsoundness into the type system from the get-go.
I look at some historical examples of intentional
unsoundness in type systems, and try to present both sides of the argument.
</i>

<hr />

Type systems come in all shapes and sizes, varying from simple to complex. They often have one property in common: type soundness. The essential property of type soundness is “type preservation”, which says that a type system’s approximation of the result of a computation (a _type_) is “preserved” at every intermediate computational stage. In other words, the type system’s job is to model a computation, and this model should not lose accuracy as the program evaluates.

Why is soundness important? Historically, soundness has been used as an objective measure of the robustness of a type system. If a type system is proven sound, programmers can use it to prove interesting, sometimes strong, properties about their programs. Programmers often use other verification tools like unit testing or contracts to get strong assurances of similar properties. Using a “sound” type system can sometimes means other verification tools are redundant, so programmers remove other, otherwise helpful, techniques and instead lean on the type system. This is predicated on the type system being sound! If there is a _false_ claim of soundness, programmers accidentally make their programs less verified by transferring verified assurances from other tools to a broken type system.

Why would a type system want to omit soundness, given such a strong historical context? We can split the discussion into two parts. In the first part we discuss _closed-world_ soundness. Closed-world soundness is the traditional kind of soundness that assumes all programs are type checked by the same type system. Recent type systems for untyped languages like [Typed Racket][2] and [Typed Clojure][3] include proofs for closed-world soundness. This allows Typed Clojure, for example, to prove null-pointer exceptions are ruled out in purely type-checked code. Conversely, some type systems intentionally break type soundness, like [TypeScript][1]. You cannot prove any interesting properties about TypeScript code without soundness, so why did the designers intentionally break soundness?

TypeScript challenges the status quo, claiming that lowering the cognitive overhead of using types is more important than type soundness. They argue that JavaScript programmers, their main audience, would find it easier to use TypeScript if they deviated from traditional norms. This was rather distressing to academics, who had already done the hard work of discovering these blunders in published work years ago. For example, TypeScript includes covariant-arrays, which breaks any guarantees about the contents of mutable arrays. An early underestimation of this guarantee brought about the value restriction in Standard ML, and later, covariant-arrays in Java required additional runtime checks to avoid segfaults. Interestingly, fixing these areas in TypeScript has been a fruitful source of new research, probably an unintended consequence of the original TypeScript designers at Microsoft.

The second important component of soundness is _open-world_ soundness. Open-world soundness has recently become important with the advent of _gradual typing_. Languages with open-world soundness not only require closed-world soundness, but also that the guarantees upheld under closed-world assumptions cannot be broken by foreign code. The introduction and consideration of foreign code is the essential difference between open-world and closed-world soundness.

Many more systems are designed around preserving closed-world rather than open-world soundness. [Typed Racket][2] is currently the most sophisticated open-world system. It is designed so that its interactions with foreign code never break closed-world invariants. In practice, this means that error messages are still informative even when Typed Racket code interacts with foreign code. 

Open-world systems like Typed Racket are harder to design, implement, and use. For example, Typed Racket must scrutinize the “language boundary”, the barrier between “foreign” code and “typed” code. In the design of Typed Racket, new technologies were invented to reason about values that crossed this boundary. In the implementation of Typed Racket, the language boundary translated to extra compiler passes to insert necessary checks for values that flow in and out of typed code. In the usage of Typed Racket, programmers must consider the additional performance overhead of checks via the language boundary, especially in tight loops that alternate between both sides of the boundary. All of this cost is under the assumption that the improved error messages are worth it in the end from the perspective of programmers.

Do programmers actually care about soundness in practice? The developers of [TypeScript][1] say no. They reject open-world soundness by ignoring techniques like gradual typing, resulting in poor error messages in the presence of foreign code. They also reject closed-world soundness by including unsound features, effectively providing no guarantees of the type system a TypeScript programmer could rely upon.

The developers of [Typed Clojure][3] have a different story. They seem to reject open-world soundness by default, but have designed their system such that it could be extended with open-world soundness in the future. They fully embrace closed-world soundness, including a full type soundness proof in [Typed Clojure][3], where they advertise interesting properties of the type system that are a result of closed-world soundness (like preventing null-pointer exceptions) as desirable features.

The developers of Typed Racket are much more opinionated with respect to soundness. They fully embrace open-world soundness, and advertise their users enjoy improved error messages even in the presence of foreign code. They also embrace closed-world soundness, a prerequisite of open-world soundness, and design their type system with only intentionally sound features.

Is type soundness a necessity or “just” a tool? In academic contexts, it seems necessary to both build type systems with soundness in mind and prove type soundness for them. Typed Clojure and Typed Racket both come from an academic background, so it is no surprise they both value type soundness as a necessity. Conversely, the Microsoft-designed TypeScript is completely comfortable to cherry pick which sound features they desire, even if the sum of its parts are useless in terms of proving type soundness.

The way TypeScript was designed is in fact more promising than older systems like Java. Those systems inadvertently broke type soundness instead of making it a deliberate action. From this angle, type soundness is becoming more relevant to practical languages, if not for its own sake, but instead as a reference for particular points in the language design space. Type soundness was originally invented to allow language designers to prove properties that their languages supported, and it also functions well in showing exactly how and why properties are lost when type soundness is ignored.

In conclusion, the design space of typed languages allow for many interpretations of type soundness. The recent upsurge of type systems for untyped languages has given language designers reason to experiment with unconventional views of type soundness, from full soundness in Typed Racket to intentional lack of soundness in TypeScript. The justifications for using or ignoring type soundness are interesting and varied, and there are potentially many other interesting points in the design space that are as yet unexplored.

[1]: https://pdfs.semanticscholar.org/1469/b0cbb109c2a788a346dd0480070de8334dea.pdf "TypeScript"
[2]: http://www.ccs.neu.edu/racket/pubs/popl08-thf.pdf "Typed Racket"
[3]: http://frenchy64.github.io/papers/esop16-short.pdf "Typed Clojure"
[4]: http://goto.ucsd.edu/~ravi/research/oopsla12-djs.pdf "Dependent JS"
