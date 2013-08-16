(ns blog.immutable.local
  (:require [clojure.core.typed 
             :refer [Coll NonEmptyColl check-ns]]))

(defn 
  ^{:ann '(All [x]
            [(Coll x) -> (NonEmptyColl x)])}
  always-non-empty [c]
  {:pre [(seq c)]}
  c)

; (check-ns)
; => :ok
