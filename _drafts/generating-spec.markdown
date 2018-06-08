---
layout: post
title:  "Automatic clojure.specs s/keys"
date:   2018-06-08 09:00:00
categories: specs automatic annotations
---

<i>
Clojure.spec has an interesting design feature that
enforces heterogeneous map specifications to have each
entry associated with a global alias.
Implicit information is then used to locate the spec,
and which key it should checked against in a map.
</i>

<i>
In this post, we'll go over the essential innovation in
heterogeneous map specs, and how they can be an
extra challenge to automatically generate over
Typed Clojure annotations.
</i>

<hr />

If you were tasked in creating syntax for specifying a heterogeneous keyword map
in Clojure
and I had to predict what it might look like, I would guess it would look
something like Typed Clojure's `HMap`:

```clojure
(defalias MyMap
  (HMap :mandatory {:a Int
                    :my-ns/a String}
        :optional {:b Boolean
                   :my-ns/b Symbol}))
```

You'd have some way of associating keys with their spec, and
whether they are required or optional. 
Whether a keyword is unqualified (like `:a`) or qualified
under a namespace (like `:my-ns/a`), they are grouped together.
All these values would conform to this specification.

```clojure
{:a 1, :my-ns/a "foo"}
{:a 1, :my-ns/a "foo", :b true}
{:a 1, :my-ns/a "foo", :my-ns/b 'abc}
{:a 1, :my-ns/a "foo", :my-ns/b 'abc :b false}
```

clojure.spec instead mixes things up in two ways. First, it distinguishes unqualified
keys (`:req-un` and `:opt-un`) from qualified keys (`:req` and `:opt`). 

```clojure
(require '[clojure.spec.alpha :as s])
(s/def MyMap
  (s/keys :req    [:my-ns/a]
          :req-un [:unq/a]
          :opt    [:my-ns/b]
          :opt-un [:unq/b]))
```

Second, instead of key-spec pairs, entries are a specified
as a vector of registered spec aliases.

```clojure
(s/def :unq/a int?)
(s/def :my-ns/a string?)
(s/def :unq/b boolean?)
(s/def :my-ns/b symbol?)
```

Spec keyword aliases must be fully qualified, so we need to add a
namespace for `:req-un` and `:opt-un` entries (here I used `unq`).

We can use `s/exercise` to generate 10 values that conform to this spec.

```clojure
(pp/pprint (map first (s/exercise MyMap)))
;=> ({:my-ns/a "", :a 0}
;    {:my-ns/a "D", :a -1, :my-ns/b k/Aj, :b true}
;    {:my-ns/a "gB", :a 0, :b false}
;    {:my-ns/a "", :a -1, :b false, :my-ns/b V.}
;    {:my-ns/a "EDtk", :a 0}
;    {:my-ns/a "j", :a 0, :my-ns/b f*.u.T6R/!.9O7, :b false}
;    {:my-ns/a "3Dn342", :a 1, :b true, :my-ns/b ?-!KA+4.f!.DkJ2*/b-+}
;    {:my-ns/a "ZWz7A3r",
;     :a 0,
;     :my-ns/b xR+.+.t0C.V9.z!p*jy._9oob+R-/Z!T4?}
;    {:my-ns/a "5", :a -1, :my-ns/b _Y_G?!!._?6_oUE.-SWVJ/n!-1_n+}
;    {:my-ns/a "U",
;     :a -45,
;     :my-ns/b i?n.+xh_?*.oAN?qnp5.TeYSFiU*+Y.O.+rI.?M5._w.__*!8k-!-8/!-Dd.8!,
;     :b true})
```

The intention in clojure.spec's design here is to 
make map specifications more uniform and less redundant by encouraging alias reuse.
For example, these three map specs all depend on the global
definition of the `:my-ns/a` key.

```clojure
(require '[clojure.spec.alpha :as s])
(s/def MyMap1 (s/keys :req [:my-ns/a :my-ns/b]))
(s/def MyMap2 (s/keys :req [:my-ns/b]))
(s/def MyMap3 (s/keys :req [:my-ns/a]))
```

# Implications: Unqualified keys

There are several interesting things to consider when automatically
generating these specs.

First, how do we interpret the global meaning of an unqualified entry?

For example, how do we convert these types to specs?

```clojure
(defalias AMap1 (HMap :mandatory {:a Int}))
(defalias AMap2 (HMap :mandatory {:a Boolean}))
```

We could combine all similarly-named entries into one alias
and reuse it.

```clojure
(s/def :unq/a (s/or :int int?
                    :bool boolean?))
(s/def AMap1 (s/keys :req-un [:unq/a]))
(s/def AMap2 (s/keys :req-un [:unq/a]))
```

We could separate each unqualified key into
its own entry.

```clojure
(s/def :unq1/a int?)
(s/def :unq2/a boolean?)
(s/def AMap1 (s/keys :req-un [:unq1/a]))
(s/def AMap2 (s/keys :req-un [:unq2/a]))
```

Or, some combination of the two by taking into
account other information.
For example, if the unqualified specs are identical
in two maps, reuse a spec alias, or use ad-hoc "tags"
to group unqualified keys.

# Implications: Qualified keys

There is exactly one spec alias for each qualified key (itself).
This means that if we generate a spec alias for a qualified key, there's a chance
it can get clobbered by another alias definition of the same name.
This is unfortunate, but I don't have a good solution yet.

Probably the easiest way to avoid this problem is by simply not including qualified
keys in our map specifications.

# Automatic conversion from Typed Clojure to spec annotations

Typed Clojure's automatic annotator has been extended to 
also generate clojure.spec annotations. Several clojure.spec features 
make it a weird fit, especially `s/keys`.

# Just-in-time alias generation

```clojure
(defalias AMap1 (HMap :mandatory {:a Int}))
(defalias AMap2 (HMap :mandatory {:a Boolean}))
```

```clojure
;; Step 1
; Just-in-time aliases:
; (s/def :unq/a int?)

(s/def AMap1 (s/keys :req-un [:unq/a]))
```

```clojure
;; Step 2
; Just-in-time aliases:
; (s/def :unq/a (s/or :int int?
;                     :bool boolean?))

(s/def AMap1 (s/keys :req-un [:unq/a]))
(s/def AMap2 (s/keys :req-un [:unq/a]))
```

```clojure
;; Final output:
(s/def :unq/a (s/or :int int?
                    :bool boolean?))

(s/def AMap1 (s/keys :req-un [:unq/a]))
(s/def AMap2 (s/keys :req-un [:unq/a]))
```

# Nested maps

```clojure
(defalias AMap1 (HMap :mandatory {:a (HMap :mandatory {:a Boolean})}))
```

```clojure
;; Step 1
; Just-in-time aliases:
; (s/def :unq/a boolean?)

(s/def AMap1 (HMap :mandatory {:a (s/keys :req-un [:unq/a])}))
```

```clojure
;; Step 2
; Just-in-time aliases:
; (s/def :unq/a (s/or :amap (s/keys :req-un [:unq/a])
;                     :bool boolean?))

(s/def AMap1 (s/keys :req-un [:unq/a])})
```

# Previously used aliases get clobbered

```clojure
(defalias unq/a Int)
(defalias AMap1 (HMap :mandatory {:a unq/a}))
(defalias AMap2 (HMap :mandatory {:a Boolean}))
```

```clojure
;; Step 1
; Just-in-time aliases:

(s/def :unq/a int?)
```

```clojure
;; Step 2
; Just-in-time aliases:

(s/def :unq/a int?)
(s/def AMap1 (s/keys :req-un [:unq/a])})
```

```clojure
;; Step 3
; Just-in-time aliases:
; (s/def :unq/a boolean?)

(s/def :unq/a int?)
(s/def AMap1 (s/keys :req-un [:unq/a])})
(s/def AMap2 (s/keys :req-un [:unq/a])})
```

```clojure
;; Final output:

(s/def :unq/a boolean?) ;clobbered!
(s/def :unq/a int?)
(s/def AMap1 (s/keys :req-un [:unq/a])})
(s/def AMap2 (s/keys :req-un [:unq/a])})
```

# Unreachable aliases

```clojure
(defalias AMap1 (HMap :mandatory {:a (HMap :mandatory {:b Int})}))
(defalias AMap2 (HMap :mandatory {:a (Map Any Any)})})
```

```clojure
;; Step 1
; Just-in-time aliases:
; (s/def :unq/b int?)

(s/def AMap1 (HMap :mandatory {:a (s/keys :req-un [:unq/b])}))
```

```clojure
;; Step 2
; Just-in-time aliases:
; (s/def :unq/b int?)
; (s/def :unq/a (s/keys :req-un [:unq/b]))

(s/def AMap1 (s/keys :req-un [:unq/a]))
```

```clojure
;; Step 2
; Just-in-time aliases:
; (s/def :unq/b int?)
; (s/def :unq/a (s/map-of any? any?))

(s/def AMap1 (s/keys :req-un [:unq/a]))
(s/def AMap2 (s/keys :req-un [:unq/a]))
```

```clojure
; Final output:

(s/def :unq/b int?) ;unused!
(s/def :unq/a (s/map-of any? any?))
(s/def AMap1 (s/keys :req-un [:unq/a]))
(s/def AMap2 (s/keys :req-un [:unq/a]))
```
