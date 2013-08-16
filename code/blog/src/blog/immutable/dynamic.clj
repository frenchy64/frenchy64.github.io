(ns blog.immutable.dynamic
  (:require [clojure.core.typed 
                          :refer [Atom1 Int check-ns]]))

(def ^{:ann '(U nil (Atom1 Int))}
  ^:dynamic *atom-or-nil* nil)

;(fn [] 
;  (when *atom-or-nil*
;    (swap! *atom-or-nil* inc)))

(defn ^{:ann '[-> Int]}
  inc-dynamic []
  (if-let [a *atom-or-nil*]
    (swap! a inc)
    0))
