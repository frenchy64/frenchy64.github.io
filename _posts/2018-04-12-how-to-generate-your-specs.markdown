---
layout: post
title:  "How to generate specs for your Clojure project"
date:   2018-04-12 01:00:00
---

<i>
This post describes how to generate specs for any Leiningen project.
After 8 months of internships, teaching, and quals, I needed a refresher.
You might also find it helpful.
</i>

<hr />

First, add a dependency to [`lein-typed`](https://github.com/typedclojure/lein-typed)
in your `~/.lein/profiles`. Here’s what mine looks like.

<div class="aside highlight">
<i>
~/.lein/profiles.clj
</i>
</div>

```clojure
{:user {:plugins [[lein-typed "0.4.2"]]}}
```


Let’s generate specs for [`clj-time`](https://github.com/clj-time/clj-time).

<div class="aside">
<i>
Shell
</i>
</div>

```
$ git clone git@github.com:clj-time/clj-time.git
$ cd clj-time
```

Next, make `clj-time` depend on [`core.typed`](https://github.com/clojure/core.typed).

<div class="aside highlight">
<i>
clj-time/project.clj
</i>

<div>
(Having issues?
See <a href="https://github.com/clojure/core.typed#releases-and-dependency-information">this section</a> about recent releases.)
</div>
</div>
```clojure
:dependencies [...
               [org.clojure/core.typed "0.5.0"]
               ...]
```

Now, we generate specs for the `clj-time.core` namespace.

<div class="aside">
<i>
Shell
</i>
</div>

```
$ lein typed infer-spec clj-time.core
```

Our annotations have been inserted automatically in `src/clj_time/core.clj`.
To end, here’s one of the generated annotations.

<div class="aside">
<i>
Result
</i>
</div>

```clojure
(s/fdef
  days
  :args
  (s/alt :0-args (s/cat) :1-arg (s/cat :n int?))
  :ret
  (s/or
    :org.joda.time.Days
    (partial instance? org.joda.time.Days)
    :org.joda.time.PeriodType
    (partial instance? org.joda.time.PeriodType)))
```
