(ns uk.axvr.refrain
  "Collection of useful Clojure utilities."
  (:require [clojure.string :as str]
            #?@(:clj [[clojure.edn     :as edn]
                      [clojure.java.io :as io]])))


;;; Strings

(defn trim-end
  "Trim sub from the end of s."
  [s sub]
  (if (and (string? s)
           (string? sub)
           (str/ends-with? s sub))
    (subs s 0 (- (count s)
                 (count sub)))
    s))

(defn trim-start
  "Trim sub from the start of s."
  [s sub]
  (if (and (string? s)
           (string? sub)
           (str/starts-with? s sub))
    (subs s (count sub))
    s))


;;; Collections

(defn in?
  "Returns true if coll contains elm."
  [coll elm]
  (some #(= elm %) coll))

(defn deep-merge-with
  "Like `clojure.core/merge-with`, but recursively merges."
  ([f coll] coll)
  ([f c1 c2]
   (if (coll? c1)
     (if (and (coll? c2) (map? c1))
       (merge-with (partial deep-merge-with f) c1 c2)
       (f c1 c2))
     c2))
  ([f c1 c2 & cs]
   (reduce (partial deep-merge-with f)
           (deep-merge-with f c1 c2)
           cs)))

(def deep-merge
  "Like `clojure.core/merge`, but recursively merges."
  (partial deep-merge-with (fn [_ x] x)))

;;; https://github.com/clojure/spec-alpha2/blob/74ada9d5111aa17c27fdef9c626ac6b4b1551a3e/src/test/clojure/clojure/test_clojure/spec.clj#L18,L25
(defn submap?
  "Returns true if map1 is a subset of map2."
  [map1 map2]
  (if (and (map? map1) (map? map2))
    (every? (fn [[k v]]
              (and (contains? map2 k)
                   (submap? v (get map2 k))))
            map1)
    (= map1 map2)))


;;; Other

(defn assoc*
  "Like `clojure.core/assoc`, but won't assoc if key or val is nil."
  [m & kvs]
  (into m
        (comp (partition-all 2)
              (filter (partial not-any? nil?)))
        kvs))

(defn dissoc-in
  "Like `clojure.core/assoc-in`, but for dissoc."
  [m ks]
  (case (count ks)
    0 m
    1 (dissoc m (first ks))
    (update-in m (butlast ks) dissoc (last ks))))

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
  "For macros, extract a map of options from the body.  If there is more than
  one parameter, the first item will be treated as an options map if it is
  a map."
  [[opts & body :as params]]
  (if (and (seq body) (map? opts))
    [opts body]
    [{} params]))


;;; Java

#?(:clj
   (defn derefable?
     "Returns true if `clojure.core/deref` can be called on ref."
     [ref]
     (or (instance? clojure.lang.IDeref ref)
         (instance? clojure.lang.IBlockingDeref ref))))

#?(:clj
   (defn date-compare
     "Compare date1 to date2 using op.  Example ops: < > <= >= ="
     ([op date1 date2]
      (op (.compareTo date1 date2) 0))
     ([op date1 date2 & dates]
      (reduce (partial date-compare op)
              (date-compare op date1 date2)
              dates))))

#?(:clj
   (defn read-edn-resource
     "Read an EDN file at path from JVM resources."
     [path]
     (when-let [rsc (some-> path io/resource)]
       (with-open [rdr (io/reader rsc)]
         (edn/read (java.io.PushbackReader. rdr))))))


;;; Debugging

(defn |>
  "Perform a side-effect operation on an intermediate value within
  a thread-first macro."
  [x f]
  (f x)
  x)

(defn |>>
  "Perform a side-effect operation on an intermediate value within
  a thread-last macro."
  [f x]
  (f x)
  x)
