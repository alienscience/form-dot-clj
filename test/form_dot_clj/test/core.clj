(ns form-dot-clj.test.core
  "Tests for form-dot-clj as a whole"
  (:use clojure.test)
  (:use form-dot-clj.core)
  (:use form-dot-clj.html-controls))

(def-field string-field
  [:maxlength 10]
  [:pattern "[a-z]+" "pattern error"]
  [:match #"moo" "match error"]
  [:no-match #"cow" "no-match error"])

(def-form test-form
 {:required "required error"}
 :string-field (textbox string-field))

(defn validate-ok
  "Checks that the given parameters validate with no errors"
  [params]
  (let [[validated errors] (validate test-form params)]
    (is (empty? errors) "Empty errors")))
  
(defn validate-error
  "Checks that the given parameters validates with the given errors:"
  [params expected-errs]
  (let [[validated errors] (validate test-form params)]
    (is (= errors expected-errs) "Expected errors")))

(deftest validation
  (validate-ok {"string-field" "moo"})
  (validate-error {"string-field" "abcdefghijk"} 
                  {"string-field" "Too long."})
  (validate-error {"string-field" "with space"}
                  {"string-field" "pattern error"})
  (validate-error {"string-field" "nocow"}
                  {"string-field" "match error"})
  (validate-error {"string-field" "moocow"}
                  {"string-field" "no-match error"}))
