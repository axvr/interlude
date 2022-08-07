;;;; All rights reserved.
;;;;
;;;; Copyright © 2022 Alex Vear.
;;;;
;;;;   The use and distribution terms for this software are covered by the
;;;;   Eclipse Public License 1.0 which can be found in the LICENCE file at the
;;;;   root of this distribution.  By using this software in any fashion, you are
;;;;   agreeing to be bound by the terms of this license.  You must not remove
;;;;   this notice, or any other, from this software.

(ns uk.axvr.refrain-test
  "Unit tests for the `uk.axvr.refrain` namespace/library."
  (:require [clojure.test :refer [deftest testing is]]
            [uk.axvr.refrain :as r])
  #?@(:clj  [(:import [java.time Instant])]
      :cljs [(:require-macros [uk.axvr.refrain :as r])
             (:refer-clojure :exclude [regexp?])]))

(defrecord TestRecord [foo])


;;; Core

(deftest assoc*
  (testing "Works like assoc."
    (is (= {:foo 1} (r/assoc* nil :foo 1)))
    (is (= {:foo 1 :bar 2} (r/assoc* {} :foo 1 :bar 2)))
    (is (= {:foo 1 :bar 2} (r/assoc* nil :foo 1 :bar 2)))
    (is (= {:foo 2 :bar 3} (r/assoc* {:foo 1 :bar 3} :foo 2)))
    (is (= {:foo 1 :bar 2 :biz 3} (r/assoc* {:foo 4 :biz 1} :foo 1 :bar 2 :biz 3))))
  (testing "No key values given, return input or empty hash-map."
    (is (= {} (r/assoc* nil)))
    (is (= {} (r/assoc* {})))
    (is (= {:foo 1 :bar 2} (r/assoc* {:foo 1 :bar 2}))))
  (testing "Ignores nil values."
    (is (= {} (r/assoc* nil :foo nil)))
    (is (= {:bar 2} (r/assoc* {} :foo nil :bar 2)))
    (is (= {:foo 1} (r/assoc* nil :foo 1 :bar nil)))
    (is (= {:foo 2 :bar nil} (r/assoc* {:foo 1 :bar nil} :foo 2)))
    (is (= {:foo 4 :biz 3} (r/assoc* {:foo 4 :biz 1} :foo nil :bar nil :biz 3))))
  (testing "Ignores nil keys."
    (is (= {} (r/assoc* nil nil 1)))
    (is (= {:bar 2} (r/assoc* {} nil 1 :bar 2)))
    (is (= {:foo 1} (r/assoc* nil :foo 1 :bar nil)))
    (is (= {:foo 2 nil 3} (r/assoc* {:foo 1 nil 3} :foo 2)))
    (is (= {:foo 4 :biz 3} (r/assoc* {:foo 4 :biz 1} nil 1 nil 2 :biz 3))))
  (testing "Works on records."
    (is (= (map->TestRecord {:foo 12 :bar 42})
           (r/assoc* (map->TestRecord {}) :foo 12 :bar 42 :biz nil)))
    (is (instance? TestRecord
                   (r/assoc* (map->TestRecord {}) :foo 12 :bar 42 :biz nil))))
  #?(:clj
     (testing "Throws exception on non-even number of key value pairs."
       (is (thrown? IllegalArgumentException (r/assoc* {} :foo)))
       (is (thrown? IllegalArgumentException (r/assoc* {} :foo 1 :bar))))))

(deftest dissoc-in
  (testing "No route, return input."
    (is (nil? (r/dissoc-in nil [])))
    (is (= {} (r/dissoc-in {} [])))
    (is (= {:foo 1} (r/dissoc-in {:foo 1} [])))
    (is (= {:foo 1 :bar [1 2 3]} (r/dissoc-in {:foo 1 :bar [1 2 3]} []))))
  (testing "Single key in route, behave like `clojure.core/dissoc`."
    (is (nil? (r/dissoc-in nil [:foo])))
    (is (= {} (r/dissoc-in {} [:foo])))
    (is (= {} (r/dissoc-in {:foo 1} [:foo])))
    (is (= {:bar [1 2 3]} (r/dissoc-in {:foo 1 :bar [1 2 3]} [:foo])))
    (is (= {:foo 1} (r/dissoc-in {:foo 1 :bar [1 2 3]} [:bar])))
    (is (= {:foo 1 :bar [1 2 3]} (r/dissoc-in {:foo 1 :bar [1 2 3]} [:baz]))))
  (testing "Deep route."
    (is (nil? (r/dissoc-in nil [:foo :baz])))
    (is (= {} (r/dissoc-in {} [:foo :baz])))
    (is (= {} (r/dissoc-in {:foo {:baz 42}} [:foo :baz])))
    (is (= {:foo {:baz 42}} (r/dissoc-in {:foo {:baz 42}} [:foo :woz])))
    (is (= {:foo {:woz 9}} (r/dissoc-in {:foo {:baz 42 :woz 9}} [:foo :baz])))
    (is (= {:foo {:woz 9}} (r/dissoc-in {:foo {:baz {} :woz 9}} [:foo :baz])))
    (is (= {:foo {:woz 9}} (r/dissoc-in {:foo {:baz {} :woz 9}} [:foo :baz :bar])))
    (is (= {:foo {:woz 9 :baz {:hi 12}}}
           (r/dissoc-in
             {:foo {:baz {:bar [1 2 3] :hi 12} :woz 9}}
             [:foo :baz :bar])))
    (is (= {:foo {:baz {:bar [1 2 3] :hi 12}}}
           (r/dissoc-in
             {:foo {:baz {:bar [1 2 3] :hi 12} :woz 9}}
             [:foo :woz])))
    (is (= {:foo {:bar 42}}
           (r/dissoc-in {:foo {:foo 12 :bar 42}} [:foo :foo])))
    (is (= {:foo {:bar 42 :foo 12}}
           (r/dissoc-in {:foo {:foo 12 :bar 42}} [:bar :foo])))
    (is (= {:bar [1 2 3]} (r/dissoc-in {:foo {:baz 42} :bar [1 2 3]} [:foo :baz]))))
  (testing "Works on records."
    (is (= (map->TestRecord {:foo {:bar 42} :biz 4})
           (r/dissoc-in (map->TestRecord {:foo {:bar 42 :hi {:hello "world"}} :biz 4}) [:foo :hi :hello])))
    #?(:clj
       (is (instance? TestRecord
                      (r/dissoc-in (map->TestRecord {:foo {:bar 42 :hi {:hello "world"}} :biz 4}) [:foo :hi :hello]))))))

(deftest derefable?
  (testing "True on result of delay."
    (is (true? (r/derefable? (delay 1))))
    (is (true? (r/derefable? (delay (+ 1 2 3))))))
  #?(:cljs (comment "ClojureScript does not have the \"future\" function.")
     :default
     (testing "True on result of future/promise."
       (is (true? (r/derefable? (future 1))))
       (is (true? (r/derefable? (future (+ 1 2 3)))))
       (is (true? (r/derefable? (promise))))))
  (testing "False on already derefed delay object."
    (is (false? (r/derefable? @(delay 1))))
    (is (false? (r/derefable? @(delay (+ 1 2 3)))))
    (is (false? (r/derefable? (deref (delay 1)))))
    (is (false? (r/derefable? (deref (delay (+ 1 2 3)))))))
  #?(:cljs (comment "ClojureScript does not have the \"future\" function.")
     :default
     (testing "False on already derefed promise object."
       (is (false? (r/derefable? @(future 1))))
       (is (false? (r/derefable? @(future (+ 1 2 3)))))
       (is (false? (r/derefable? (deref (future 1)))))
       (is (false? (r/derefable? (deref (future (+ 1 2 3))))))
       (is (false? (r/derefable? (deref (future 1) 100 :timeout))))
       (is (false? (r/derefable? (deref (future (+ 1 2 3)) 100 :timeout))))
       (is (false? (r/derefable? (deref (promise) 10 :timeout))))
       (is (false? (r/derefable? (deref (promise) 10 :timeout))))))
  (testing "False on nil."
    (is (false? (r/derefable? nil))))
  (testing "False on non-derefable object."
    (is (false? (r/derefable? "Hello")))
    (is (false? (r/derefable? 42)))
    (is (false? (r/derefable? (+ 1 2 3))))))

(deftest regexp?
  (testing "True for regexp literal."
    (is (true? (r/regexp? #"")))
    (is (true? (r/regexp? #"\w+"))))
  (testing "True for result of `clojure.core/re-pattern` call."
    (is (true? (r/regexp? (re-pattern ""))))
    (is (true? (r/regexp? (re-pattern "\\w+")))))
  (testing "False on nil."
    (is (false? (r/regexp? nil))))
  (testing "False on string."
    (is (false? (r/regexp? "")))
    (is (false? (r/regexp? "\\w+")))))

(deftest contrast
  (testing "Contrast strings."
    (is (true? (r/contrast < "1" "2" "3")))
    (is (true? (r/contrast > "3" "2" "1")))
    (is (true? (r/contrast not= "1" "2")))
    (is (false? (r/contrast = "1" "2")))
    (is (false? (r/contrast < "a" "b" "c" "c")))
    (is (true? (r/contrast <= "a" "b" "c" "c")))
    (is (true? (r/contrast < "a")))
    (is (true? (r/contrast < "hello" "hey" "hi")))
    (is (true? (r/contrast < "799c29c2-1688-40e7-80d3-05cef2b8b7d3"
                           "cd201847-a9d1-4aa9-87d5-e2a9d37d8c8a")))
    (is (= ["c" "c" "b" "b" "a"]
           (sort (r/contrast >=) ["a" "b" "c" "b" "c"]))))
  (testing "Contrast numbers."
    (is (true? (r/contrast < 1 2 3 4)))
    (is (false? (r/contrast > 1 2 3 4)))
    (is (false? (r/contrast < 1 1 2 3 4)))
    (is (true? (r/contrast <= 1 1 2 3 4)))
    (is (= [1 1 2 3 5 5 6 8 9]
           (sort (r/contrast <=) [1 5 2 6 8 9 3 1 5]))))
  #?(:clj
     (testing "Contrast dates."
       (let [inst1 (Instant/now)
             inst2 (.minusSeconds inst1 2)
             inst3 (.plusSeconds inst1 10)]
         (is (true? (r/contrast = inst1 inst1)))
         (is (true? (r/contrast < inst2 inst1 inst3)))
         (is (false? (r/contrast < inst1 inst2)))
         (is (= [inst2 inst1 inst3]
                (sort (r/contrast <) [inst1 inst3 inst2])))))))


;;; Collections

(deftest in?
  (testing "Returns true if coll contains element."
    (is (true? (r/in? [1 2 3 4] 1)))
    (is (true? (r/in? [1 2 3 4] 2)))
    (is (true? (r/in? [1 2 3 4] 3)))
    (is (true? (r/in? [1 2 3 4] 4))))
  (testing "Returns false if coll does not contain element."
    (is (false? (r/in? [1 2 3 4] 0)))
    (is (false? (r/in? [1 2 3 4] 5))))
  (testing "Returns false on nil input collection."
    (is (false? (r/in? nil nil)))
    (is (false? (r/in? nil 1))))
  (testing "Behaves like `clojure.core/contains?` on hash-sets."
    (is (true? (r/in? #{:foo :bar} :foo)))
    (is (true? (r/in? #{nil} nil)))
    (is (false? (r/in? #{:foo} :bar)))
    (is (false? (r/in? #{} :foo))))
  (testing "Matches key-value pairs in hash-maps."
    (is (true? (r/in? {:foo 1} [:foo 1])))
    (is (false? (r/in? {:bar 1} [:bar 2]))))
  (testing "Tests if strings contains specific character."
    (is (true? (r/in? "foo:bar" \:)))
    (is (false? (r/in? "foo:bar" \c)))))

(deftest submap?
  (testing "Returns true on identical inputs."
    (is (true? (r/submap? nil nil)))
    (is (true? (r/submap? {} {})))
    (is (true? (r/submap? {:foo 1} {:foo 1})))
    (is (true? (r/submap? 1 1)))
    (is (true? (r/submap? {:foo [1 2 3]} {:foo [1 2 3]}))))
  (testing "Returns false on different inputs."
    (is (false? (r/submap? nil :foo)))
    (is (false? (r/submap? {} #{})))
    (is (false? (r/submap? {:foo 2} {:foo 1})))
    (is (false? (r/submap? 1 2)))
    (is (false? (r/submap? {:foo [1 3 3]} {:foo [1 2 3]}))))
  (testing "Returns true on matching submap."
    (is (true? (r/submap? {:foo {:bar 1}} {:foo {:bar 1} :bar 2})))
    (is (true? (r/submap? {:foo {:bar 1} :woz {:1234 [1 2 3 4] :hi :there}}
                          {:foo {:bar 1 :hello 4} :bar 2 :woz {:1234 [1 2 3 4] :hi :there :foo nil}}))))
  (testing "Returns false on no matching submap."
    (is (false? (r/submap? {:foo {:bar 1}} {:foo {:fail 1} :bar 2})))
    (is (false? (r/submap? {:foo {:bar 1} :woz {:1234 [1 2 3 4] :hi :there}}
                           {:foo {:bar 1 :hello 4} :bar 2 :woz {:1234 [1 3 3 4] :hi :there :foo nil}}))))
  (testing "Works with records."
    (is (true? (r/submap? (map->TestRecord {:foo {:bar 1}}) {:foo {:bar 1 :biz 12} :woz 42})))
    (is (true? (r/submap? {:foo {:bar 1}} (map->TestRecord {:foo {:bar 1 :biz 12} :woz 42}))))
    (is (false? (r/submap? (map->TestRecord {:foo {:bar 2}}) {:foo {:bar 1 :biz 12} :woz 42})))
    (is (false? (r/submap? {:foo {:bar 2}} (map->TestRecord {:foo {:bar 1 :biz 12} :woz 42}))))))

(deftest deep-merge-with
  (testing "Returns nil on nil input."
    (is (= nil (r/deep-merge-with conj nil)))
    (is (= nil (r/deep-merge-with conj nil nil)))
    (is (= nil (r/deep-merge-with conj nil nil nil))))
  (testing "If a map was passed in, return a map."
    (is (= {} (r/deep-merge-with conj {})))
    (is (= {} (r/deep-merge-with conj {} {})))
    (is (= {} (r/deep-merge-with conj {} {} {})))
    (is (= {} (r/deep-merge-with conj {} nil {})))
    (is (= {} (r/deep-merge-with conj nil {})))
    (is (= {} (r/deep-merge-with conj nil {} nil)))
    (is (= {} (r/deep-merge-with conj nil nil {})))
    (is (= {} (r/deep-merge-with (fn [_ x] x) {} nil)))
    (is (= {} (r/deep-merge-with (fn [_ x] x) nil {})))
    (is (= {} (r/deep-merge-with (fn [_ x] x) nil {} nil)))
    (is (= {} (r/deep-merge-with (fn [_ x] x) nil nil {})))
    (is (= {:foo 1} (r/deep-merge-with conj nil {:foo 1})))
    (is (= {:foo 1} (r/deep-merge-with conj {:foo 1} nil)))
    (is (= {:foo 1} (r/deep-merge-with conj {:foo 1} {}))))
  (testing "Shallow merge with `clojure.core/conj` merge strategy."
    (is (= [1 2 3] (r/deep-merge-with conj [1] 2 3)))
    (is (= {:foo 1} (r/deep-merge-with conj {:foo 2} {} {:foo 1})))
    (is (= {:foo 1 :bar true} (r/deep-merge-with conj {:bar true} {:foo 1})))
    (is (= {:bar [1 2 3] :foo 1} (r/deep-merge-with conj {:foo 2} {:bar [1 2 3]} {:foo 1})))
    (is (= {:bar [1 2 3] :foo 1} (r/deep-merge-with conj {:foo 2 :bar [1 2 3]} {:foo 1})))
    (is (= {:bar [1 2 3] :foo 1} (r/deep-merge-with conj {:foo 2} {:foo 1 :bar [1 2 3]})))
    (is (= {:bar [1 2 3] :foo [2 1]} (r/deep-merge-with conj {:foo [2]} {:foo 1 :bar [1 2 3]}))))
  (testing "Shallow merge with \"replace\" merge strategy."
    (let [replace (fn [_ x] x)]
      (is (= [3] (r/deep-merge-with replace [1] [2] [3])))
      (is (= {:foo 1} (r/deep-merge-with replace {:foo 2} {} {:foo 1})))
      (is (= {:foo 1 :bar true} (r/deep-merge-with replace {:bar true} {:foo 1})))
      (is (= {:bar [1 2 3] :foo 1} (r/deep-merge-with replace {:foo 2} {:bar [1 2 3]} {:foo 1})))
      (is (= {:bar [1 2 3] :foo 1} (r/deep-merge-with replace {:foo 2 :bar [1 2 3]} {:foo 1})))
      (is (= {:bar [1 2 3] :foo 1} (r/deep-merge-with replace {:foo 2} {:foo 1 :bar [1 2 3]})))
      (is (= {:bar [1 2 3] :foo 1} (r/deep-merge-with replace {:foo [2]} {:foo 1 :bar [1 2 3]})))
      (is (= {:bar [1 2 3] :foo nil} (r/deep-merge-with replace {:foo [2]} {:foo nil :bar [1 2 3]})))))
  (testing "Deep merge with `clojure.core/conj` merge strategy."
    (is (= {:foo {:bar {:biz 42} :woz 12 :world [1 2 3]} :hi "there"}
           (r/deep-merge-with conj
                              {:foo {:bar {} :woz 12}}
                              {:foo {:world [1 2]} :hi "there"}
                              {:foo {:bar {:biz 42}}}
                              {:foo {:world 3}})))
    (is (= {:foo {:bar {:biz 42} :woz 12 :world [1 2 3]} :hi "there"}
           (r/deep-merge-with conj
                              {:foo {:bar {} :woz 12} :hi "there?"}
                              {:foo {:world [1 2]} :hi "there"}
                              {:foo {:bar {:biz 42}}}
                              {:foo {:world 3}}))))
  (testing "Deep merge with \"replace\" merge strategy."
    (let [replace (fn [_ x] x)]
      (is (= {:foo {:bar {:biz 42} :woz 12 :world [1 2 3]} :hi "there"}
             (r/deep-merge-with replace
                                {:foo {:bar {} :woz 12}}
                                {:foo {:world [1 2]} :hi "there"}
                                {:foo {:bar {:biz 42}}}
                                {:foo {:world [1 2 3]}})))
      (is (= {:foo {:bar {:biz 42} :woz 12 :world [1 2 3]} :hi "there"}
             (r/deep-merge-with replace
                                {:foo {:bar {} :woz 12} :hi "there?"}
                                {:foo {:world [1 2]} :hi "there"}
                                {:foo {:bar {:biz 42}}}
                                {:foo {:world [1 2 3]}})))))
  (testing "Works on records."
    (is (= (map->TestRecord {:foo 1 :bar [1 2 3]})
           (r/deep-merge-with conj (map->TestRecord {:foo 1 :bar [1 2]}) {:bar 3})))
    (is (instance? TestRecord
                   (r/deep-merge-with conj (map->TestRecord {:foo 1 :bar [1 2]}) {:bar 3})))
    (is (not (instance? TestRecord
                        (r/deep-merge-with conj {:foo 1 :bar [1 2]} (map->TestRecord {:bar 3})))))))

(deftest deep-merge
  (testing "Returns nil on nil input."
    (is (= nil (r/deep-merge nil)))
    (is (= nil (r/deep-merge nil nil)))
    (is (= nil (r/deep-merge nil nil nil))))
  (testing "If a map was passed in, return a map."
    (is (= {} (r/deep-merge {})))
    (is (= {} (r/deep-merge {} {})))
    (is (= {} (r/deep-merge {} {} {})))
    (is (= {} (r/deep-merge {} nil {})))
    (is (= {} (r/deep-merge nil {})))
    (is (= {} (r/deep-merge nil {} nil)))
    (is (= {} (r/deep-merge nil nil {})))
    (is (= {:foo 1} (r/deep-merge nil {:foo 1})))
    (is (= {:foo 1} (r/deep-merge {:foo 1} nil)))
    (is (= {:foo 1} (r/deep-merge {:foo 1} {}))))
  (testing "Shallow merge behaves like `deep-merge-with (fn [_ x] x) ...`."
    (is (= [3] (r/deep-merge [1] [2] [3])))
    (is (= {:foo 1} (r/deep-merge {:foo 2} {} {:foo 1})))
    (is (= {:foo 1 :bar true} (r/deep-merge {:bar true} {:foo 1})))
    (is (= {:bar [1 2 3] :foo 1} (r/deep-merge {:foo 2} {:bar [1 2 3]} {:foo 1})))
    (is (= {:bar [1 2 3] :foo 1} (r/deep-merge {:foo 2 :bar [1 2 3]} {:foo 1})))
    (is (= {:bar [1 2 3] :foo 1} (r/deep-merge {:foo 2} {:foo 1 :bar [1 2 3]})))
    (is (= {:bar [1 2 3] :foo 1} (r/deep-merge {:foo [2]} {:foo 1 :bar [1 2 3]})))
    (is (= {:bar [1 2 3] :foo nil} (r/deep-merge {:foo [2]} {:foo nil :bar [1 2 3]}))))
  (testing "Deep merge behaves like `deep-merge-with (fn [_ x] x) ...`."
    (is (= {:foo {:bar {:biz 42} :woz 12 :world [1 2 3]} :hi "there"}
           (r/deep-merge {:foo {:bar {} :woz 12}}
                         {:foo {:world [1 2]} :hi "there"}
                         {:foo {:bar {:biz 42}}}
                         {:foo {:world [1 2 3]}})))
    (is (= {:foo {:bar {:biz 42} :woz 12 :world [1 2 3]} :hi "there"}
           (r/deep-merge {:foo {:bar {} :woz 12} :hi "there?"}
                         {:foo {:world [1 2]} :hi "there"}
                         {:foo {:bar {:biz 42}}}
                         {:foo {:world [1 2 3]}}))))
  (testing "Works on records."
    (is (= (map->TestRecord {:foo 1 :bar 3})
           (r/deep-merge (map->TestRecord {:foo 1 :bar [1 2]}) {:bar 3})))
    (is (instance? TestRecord
                   (r/deep-merge (map->TestRecord {:foo 1 :bar [1 2]}) {:bar 3})))
    (is (not (instance? TestRecord
                        (r/deep-merge {:foo 1 :bar [1 2]} (map->TestRecord {:bar 3})))))))


;;; Strings

(deftest trim-start
  (testing "Trims the start of string."
    (is (= " world" (r/trim-start "Hello world" "Hello")))
    (is (= "world" (r/trim-start "Hello world" "Hello "))))
  (testing "Return original string if input does not start in substring."
    (is (= "Hello world" (r/trim-start "Hello world" "world!"))))
  (testing "Return original string on empty substring."
    (is (= "Hello world" (r/trim-start "Hello world" ""))))
  (testing "Does nothing on empty string."
    (is (= "" (r/trim-start "" "world"))))
  #?@(:clj
      [(testing "Throws exception on non-string input string."
         (is (thrown? AssertionError (r/trim-start 123 ""))))
       (testing "Throw exception on non-string substring."
         (is (thrown? AssertionError (r/trim-start "" nil))))]))

(deftest trim-end
  (testing "Trims the end of string."
    (is (= "Hello " (r/trim-end "Hello world" "world")))
    (is (= "Hello" (r/trim-end "Hello world" " world"))))
  (testing "Return original string if input does not end in substring."
    (is (= "Hello world" (r/trim-end "Hello world" "world!"))))
  (testing "Return original string on empty substring."
    (is (= "Hello world" (r/trim-end "Hello world" ""))))
  (testing "Does nothing on empty string."
    (is (= "" (r/trim-end "" "world"))))
  #?@(:clj
      [(testing "Throws exception on non-string input string."
         (is (thrown? AssertionError (r/trim-end 123 ""))))
       (testing "Throw exception on non-string substring."
         (is (thrown? AssertionError (r/trim-end "" nil))))]))


;;; Other

(deftest when-let*
  (testing "Returns expected result."
    (is (= 42 (r/when-let* [foo 6]
                (* foo 7))))
    (is (= 42 (r/when-let* [foo 6 bar 7]
                (* foo bar))))
    (is (= 42 (r/when-let* [x 3 y 7 z 2]
                (is (= x 3))
                (is (= y 7))
                (is (= z 2))
                (* x y z))))
    (is (= 42 (r/when-let* [x 2
                            y 7
                            z (* 3 x)]
                (is (= x 2))
                (is (= y 7))
                (is (= z 6))
                (* y z)))))
  (testing "Does not evaluate body and returns nil."
    (is (nil? (r/when-let* [foo nil]
                (is (not (nil? foo)))
                (* foo 7))))
    (is (nil? (r/when-let* [foo nil bar 7]
                (is (not (nil? foo)))
                (is (not= 7 bar))
                (* foo bar))))
    (is (nil? (r/when-let* [foo 6 bar false]
                (is (not= 6 foo))
                (is (not (or (false? bar) (nil? bar)))))))
    (is (nil? (r/when-let* [foo 6 bar 7 biz (when (= foo 7) 5)]
                (is (not= 6 foo))
                (is (not= 7 bar))
                (is (not (or (false? bar) (nil? bar) (= 5 bar))))))))
  (testing "Sort-circuits evaluation of binding forms and returns nil."
    (let [ran? (atom 0)]
      (is (zero? @ran?))
      (is (nil? (r/when-let* [x 12
                              y (when (< x 3) 5)
                              z (swap! ran? inc)]
                   (is (not= 12 x))
                   (is (not= 5 y))
                   (is (zero? z))
                   (swap! ran? inc)
                   (is (zero? @ran?)))))
      (is (zero? @ran?)))))

(deftest macro-body-opts
  (testing "No hash-map as first item in body, default opts to empty hash-map."
    (is (= [{} `((println "hello world") (+ 1 2 3))]
           (r/macro-body-opts `((println "hello world") (+ 1 2 3)))))
    (is (= [{} `((println "hello world") {:foo 1} {:bar 42})]
           (r/macro-body-opts `((println "hello world") {:foo 1} {:bar 42})))))
  (testing "More than one item in body and first item is hash-map, extract it."
    (is (= [{:foo [4 :five 6] :bar 42} `((println "hello world") (+ 1 2 3))]
           (r/macro-body-opts `({:foo [4 :five 6] :bar 42}
                                (println "hello world") (+ 1 2 3)))))
    (is (= [{:foo `(+ 4 5 6)} `((+ 1 2 3))]
           (r/macro-body-opts `({:foo (+ 4 5 6)} (+ 1 2 3)))))
    (is (= [{:foo [4 :five 6] :bar 42} `((println "hello world") {:foo 1} {:bar 42})]
           (r/macro-body-opts `({:foo [4 :five 6] :bar 42}
                                (println "hello world") {:foo 1} {:bar 42})))))
  (testing "Body of item 1 does not match hash-maps as options map."
    (is (= [{} `((+ 1 2 3))]
           (r/macro-body-opts `((+ 1 2 3)))))
    (is (= [{} `({:foo (+ 4 5 6)})]
           (r/macro-body-opts `({:foo (+ 4 5 6)}))))
    (is (= [{} `({:foo [4 :five 6] :bar 42})]
           (r/macro-body-opts `({:foo [4 :five 6] :bar 42}))))))

#?(:clj
   (deftest read-edn-resource
     (testing "Fetches and reads EDN file from JVM resource."
       (is (= {:foo true :bar {:hi ["hello" "world"]} :woz 42}
              (r/read-edn-resource "data.edn"))))
     (testing "File does not exist, return nil."
       (is (= nil (r/read-edn-resource "no_file.edn"))))))
