(ns uk.axvr.refrain-test
  (:require [clojure.test :refer [deftest testing is]]
            [uk.axvr.refrain :as ref]))

(deftest trim-start
  (testing "Trims the start of string."
    (is (= " world" (ref/trim-start "Hello world" "Hello")))
    (is (= "world" (ref/trim-start "Hello world" "Hello "))))
  (testing "Return original string if input does not start in substring."
    (is (= "Hello world" (ref/trim-start "Hello world" "world!"))))
  (testing "Return original string on empty substring."
    (is (= "Hello world" (ref/trim-start "Hello world" ""))))
  (testing "Does nothing on empty string."
    (is (= "" (ref/trim-start "" "world"))))
  (testing "Throws exception on non-string input string."
    (is (thrown? AssertionError (ref/trim-start 123 ""))))
  (testing "Throw exception on non-string substring."
    (is (thrown? AssertionError (ref/trim-start "" nil)))))

(deftest trim-end
  (testing "Trims the end of string."
    (is (= "Hello " (ref/trim-end "Hello world" "world")))
    (is (= "Hello" (ref/trim-end "Hello world" " world"))))
  (testing "Return original string if input does not end in substring."
    (is (= "Hello world" (ref/trim-end "Hello world" "world!"))))
  (testing "Return original string on empty substring."
    (is (= "Hello world" (ref/trim-end "Hello world" ""))))
  (testing "Does nothing on empty string."
    (is (= "" (ref/trim-end "" "world"))))
  (testing "Throws exception on non-string input string."
    (is (thrown? AssertionError (ref/trim-end 123 ""))))
  (testing "Throw exception on non-string substring."
    (is (thrown? AssertionError (ref/trim-end "" nil)))))

;; TODO
(deftest deep-merge-with
  (testing ""))

;; TODO
(deftest deep-merge
  (testing ""))

;; TODO
(deftest submap?
  (testing ""))

(deftest in?
  (testing "Returns true if coll contains element."
    (is (true? (ref/in? [1 2 3 4] 1)))
    (is (true? (ref/in? [1 2 3 4] 2)))
    (is (true? (ref/in? [1 2 3 4] 3)))
    (is (true? (ref/in? [1 2 3 4] 4))))
  (testing "Returns false if coll does not contain element."
    (is (false? (ref/in? [1 2 3 4] 0)))
    (is (false? (ref/in? [1 2 3 4] 5)))))

(deftest assoc*
  (testing "Works like assoc."
    (is (= {:foo 1} (ref/assoc* nil :foo 1)))
    (is (= {:foo 1 :bar 2} (ref/assoc* {} :foo 1 :bar 2)))
    (is (= {:foo 1 :bar 2} (ref/assoc* nil :foo 1 :bar 2)))
    (is (= {:foo 2 :bar 3} (ref/assoc* {:foo 1 :bar 3} :foo 2)))
    (is (= {:foo 1 :bar 2 :biz 3} (ref/assoc* {:foo 4 :biz 1} :foo 1 :bar 2 :biz 3))))
  (testing "No key values given, return input or empty hash-map."
    (is (= {} (ref/assoc* nil)))
    (is (= {} (ref/assoc* {})))
    (is (= {:foo 1 :bar 2} (ref/assoc* {:foo 1 :bar 2}))))
  (testing "Ignores nil values."
    (is (= {} (ref/assoc* nil :foo nil)))
    (is (= {:bar 2} (ref/assoc* {} :foo nil :bar 2)))
    (is (= {:foo 1} (ref/assoc* nil :foo 1 :bar nil)))
    (is (= {:foo 2 :bar nil} (ref/assoc* {:foo 1 :bar nil} :foo 2)))
    (is (= {:foo 4 :biz 3} (ref/assoc* {:foo 4 :biz 1} :foo nil :bar nil :biz 3))))
  (testing "Ignores nil keys."
    (is (= {} (ref/assoc* nil nil 1)))
    (is (= {:bar 2} (ref/assoc* {} nil 1 :bar 2)))
    (is (= {:foo 1} (ref/assoc* nil :foo 1 :bar nil)))
    (is (= {:foo 2 nil 3} (ref/assoc* {:foo 1 nil 3} :foo 2)))
    (is (= {:foo 4 :biz 3} (ref/assoc* {:foo 4 :biz 1} nil 1 nil 2 :biz 3))))
  (testing "Throws exception on non-even number of key value pairs."
    (is (thrown? IllegalArgumentException (ref/assoc* {} :foo)))
    (is (thrown? IllegalArgumentException (ref/assoc* {} :foo 1 :bar)))))

;; TODO
(deftest dissoc-in
  (testing ""))

;; TODO
(deftest when-let*
  (testing ""))

;; TODO
(deftest macro-body-opts
  (testing ""))

;; TODO
(deftest derefable?
  (testing ""))

;; TODO
(deftest date-compare
  (testing ""))

;; TODO
(deftest read-edn-resource
  (testing ""))

;; TODO
(deftest |>
  (testing ""))

;; TODO
(deftest |>>
  (testing ""))
