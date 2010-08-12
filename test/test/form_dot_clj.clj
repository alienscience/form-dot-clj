(ns test.form-dot-clj
  "Tests for form-dot-clj as a whole"
  (:use clojure.test)
  (:use form-dot-clj)
  (:use form-dot-clj.html-controls))

(def-field string-field
  [:maxlength 10]
  [:pattern "[a-z]+" "pattern error"]
  [:match #"moo" "match error"]
  [:no-match #"cow" "no-match error"])

(def-form test-form
 {:required "required error"}
 :string-field (textbox string-field))

(defn validate-error
  "Checks that the given parameters validates with the given errors:"
  [params expected-errs]
  (let [[validated errors] (validate test-form params)]
    (is (= errors expected-errs) "Expected errors")))

(deftest validate
  (validate-error {"string-field" "abcdefghijk"} 
                  {:string-field "Too long."}))
