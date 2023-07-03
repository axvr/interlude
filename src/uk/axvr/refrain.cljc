;;;; All rights reserved.
;;;;
;;;; Copyright © 2022 Alex Vear.
;;;; Copyright © 2009, 2016 Rich Hickey.
;;;;
;;;;   The use and distribution terms for this software are covered by the
;;;;   Eclipse Public License 1.0 which can be found in the LICENCE file at the
;;;;   root of this distribution.  By using this software in any fashion, you are
;;;;   agreeing to be bound by the terms of this license.  You must not remove
;;;;   this notice, or any other, from this software.

(ns uk.axvr.refrain
  "Collection of useful Clojure utilities."
  (:require [clojure.string :as str]
            #?@(:clj [[clojure.edn     :as edn]
                      [clojure.java.io :as io]]))
  #?(:cljs (:refer-clojure :exclude [regexp?])))


;;; Core

(defn assoc*
  "Like `clojure.core/assoc`, but won't assoc if key or val is nil."
  [m & kvs]
  (into (or m {})
        (comp (partition-all 2)
              (remove (partial some nil?)))
        kvs))

;; Copyright © 2009 Rich Hickey.
;; https://github.com/clojure/core.incubator/blob/4f31a7e176fcf4cc2be65589be113fc082243f5b/src/main/clojure/clojure/core/incubator.clj#L63-L75
(defn dissoc-in
  "Dissociates an entry from a nested associative structure returning a new
  nested structure.  ks is a sequence of keys.  Any empty maps that result
  will not be present in the new structure."
  [m [k & ks]]
  (if ks
    (if-let [nextmap (get m k)]
      (let [newmap (dissoc-in nextmap ks)]
        (if (seq newmap)
          (assoc m k newmap)
          (dissoc m k)))
      m)
    (dissoc m k)))

(defn derefable?
  "Returns true if `clojure.core/deref` can be called on ref."
  [ref]
  #?(:cljs    (or (satisfies? IDeref ref)
                  (satisfies? IDerefWithTimeout ref))
     :default (or (instance? clojure.lang.IDeref ref)
                  (instance? clojure.lang.IBlockingDeref ref))))

#?(:clj
   (defn regexp?
     "Returns true if x is a Java regular expression pattern."
     [x]
     (instance? java.util.regex.Pattern x))
   :cljr
   (defn regexp?
     "Returns true if x is a .NET regular expression pattern."
     [x]
     (instance? System.Text.RegularExpressions.Regex x))
   :cljs
   (def regexp?
     "Alias to `cljs.core/regexp?`.  This exists purely to make writing *.cljc
     files that need `regexp?` easier."
     cljs.core/regexp?))

(defn contrast
  "Contrast multiple comparable objects with each other with op.  (Wrapper
  around `clojure.core/compare`.)  Example ops: < > <= >= = not="
  ([op]
   (partial contrast op))
  ([op obj & objs]
   (->> (cons obj objs)
        (partition 2 1)
        (map (comp #(op % 0) (partial apply compare)))
        (every? true?))))


;;; Collections

(defn in?
  "Returns true if coll contains elm."
  [coll elm]
  (cond
    (set? coll)     (contains? coll elm)
    (seqable? coll) (boolean (some #(= elm %) coll))
    :else           false))

;; Copyright © 2016 Rich Hickey.
;; https://github.com/clojure/spec-alpha2/blob/74ada9d5111aa17c27fdef9c626ac6b4b1551a3e/src/test/clojure/clojure/test_clojure/spec.clj#L18,L25
(defn submap?
  "Returns true if map1 is a subset of map2."
  [map1 map2]
  (if (and (map? map1) (map? map2))
    (every? (fn [[k v]]
              (and (contains? map2 k)
                   (submap? v (get map2 k))))
            map1)
    (= map1 map2)))

(defn- deep-merge-with'
  "Internal deep-merge-with algorithm."
  [f c1 c2]
  (if (coll? c1)
    (if (and (coll? c2) (map? c1))
      (merge-with (partial deep-merge-with' f) c1 c2)
      (f c1 c2))
    c2))

(defn deep-merge-with
  "Like `clojure.core/merge-with`, but recursively merges maps."
  ([_ coll] coll)
  ([f c1 c2]
   (when (or c1 c2)
     (deep-merge-with' f c1 (or c2 {}))))
  ([f c1 c2 & cs]
   (reduce (partial deep-merge-with f)
           (deep-merge-with f c1 c2)
           cs)))

(defn deep-merge
  "Like `clojure.core/merge`, but recursively merges maps."
  [& colls]
  (when-let [[coll & colls] (filter some? colls)]
    (apply deep-merge-with (fn [_ x] x) coll colls)))


;;; Strings

(defn trim-start
  "Trim substr from the start of s."
  [s substr]
  {:pre [(string? s)
         (string? substr)]}
  (if (str/starts-with? s substr)
    (subs s (count substr))
    s))

(defn trim-end
  "Trim substr from the end of s."
  [s substr]
  {:pre [(string? s)
         (string? substr)]}
  (if (str/ends-with? s substr)
    (subs s 0 (- (count s) (count substr)))
    s))


;;; Other

(defmacro when-let*
  "Short circuiting version of `clojure.core/when-let` on multiple binding
  forms."
  [bindings & body]
  (let [[form tst & rst] bindings]
    `(when-let [~form ~tst]
       ~(if (seq rst)
          `(when-let* ~rst ~@body)
          `(do ~@body)))))

(defn macro-body-opts
  "Helper for creating macros that accept an optional map of options in their
  body.  Call this function in your macro on the rest-body param and it will
  return a vector containing the option map and the rest of the body.

  An option map will be found if there were more than one form in the body and
  the first form is a map."
  [[opts & body :as params]]
  (if (and (seq body) (map? opts))
    [opts body]
    [{} params]))

#?(:clj
   (defn read-edn-resource
     "Read an EDN file at path from JVM resources."
     ([path]
      (read-edn-resource path {}))
     ([path opts]
      (when-let [rsc (some-> path io/resource)]
        (with-open [rdr (io/reader rsc)]
          (edn/read opts (java.io.PushbackReader. rdr)))))))
