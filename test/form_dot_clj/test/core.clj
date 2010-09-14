(ns form-dot-clj.test.core
  "Tests for form-dot-clj as a whole"
  (:use clojure.test)
  (:use form-dot-clj.core)
  (:use form-dot-clj.html-controls)
  (:require [clojure.contrib.string :as str])
  (:require [clj-time.core :as joda]))

(def-field string-field
  [:maxlength 10]
  [:pattern "[a-z]+" "pattern error"]
  [:match #"moo" "match error"]
  [:no-match #"cow" "no-match error"])

(def-field int-field [:integer -2 10 "integer error"])
(def-field float-field [:float -1.2 2.5 "float error"])
(def-field email-field [:email "email error"])
(def-field date-field [:date "2010-08-18" "2010-08-19" "date error"])
(def-field url-field [:url "url error"])
(def-field bool-field [:boolean])
(def-field for-html-element [:xss->element])
(def-field for-html-attribute [:xss->attribute])
(def-field for-js-data [:xss->js-data])
(def-field for-css-value [:xss->css])
(def-field for-url [:xss->url])

(def-form test-form
  {}
  :string-field (textbox string-field)
  :int-field (textbox int-field)
  :float-field (textbox float-field)
  :email-field (textbox email-field)
  :date-field (textbox date-field)
  :url-field (textbox url-field)
  :bool-field (textbox bool-field)
  :html-element-field (textbox for-html-element)
  :html-attribute-field (textbox for-html-attribute)
  :js-data-field (textbox for-js-data)
  :css-value-field (textbox for-css-value)
  :url-value-field (textbox for-url)
  )

(defn has-value
  "Checks that the given param map converts to the given value
   map"
  [params expected-values]
  (let [[validated errors] (validate test-form params)]
    (is (= validated expected-values) "Expected values")))
  
(deftest conversion
  (has-value {"string-field" "moo"}
             {:string-field "moo"})
  (has-value {"int-field" "2"}
             {:int-field 2})
  (has-value {"float-field" "-0.8"}
             {:float-field (Float. -0.8)})
  (has-value {"date-field" "2010-08-18"}
             {:date-field (joda/date-time 2010 8 18)})
  (has-value {"bool-field" "yes"} {:bool-field true})
  (has-value {"bool-field" "true"} {:bool-field true})
  (has-value {"bool-field" "NO"} {:bool-field false})
  (has-value {"bool-field" "no"} {:bool-field false})
  (has-value {"bool-field" "false"} {:bool-field false})
  (has-value {"bool-field" "FALSE"} {:bool-field false}))

(def *all-ascii* (str/map-str char (range 1 128)))

(deftest xss
  (has-value
   {"html-element-field" "&<>\"'/hello"}
   {:html-element-field "&amp;&lt;&gt;&quot;&#x27;&#x2F;hello"})
  (has-value
   {"html-attribute-field" *all-ascii*}
   {:html-attribute-field "&#x01;&#x02;&#x03;&#x04;&#x05;&#x06;&#x07;&#x08;&#x09;&#x0A;&#x0B;&#x0C;&#x0D;&#x0E;&#x0F;&#x10;&#x11;&#x12;&#x13;&#x14;&#x15;&#x16;&#x17;&#x18;&#x19;&#x1A;&#x1B;&#x1C;&#x1D;&#x1E;&#x1F;&#x20;&#x21;&#x22;&#x23;&#x24;&#x25;&#x26;&#x27;&#x28;&#x29;&#x2A;&#x2B;&#x2C;&#x2D;&#x2E;&#x2F;0123456789&#x3A;&#x3B;&#x3C;&#x3D;&#x3E;&#x3F;&#x40;ABCDEFGHIJKLMNOPQRSTUVWXYZ&#x5B;&#x5C;&#x5D;&#x5E;&#x5F;&#x60;abcdefghijklmnopqrstuvwxyz&#x7B;&#x7C;&#x7D;&#x7E;&#x7F;"})
  (has-value
   {"js-data-field" *all-ascii*}
   {:js-data-field "\\x01\\x02\\x03\\x04\\x05\\x06\\x07\\x08\\x09\\x0A\\x0B\\x0C\\x0D\\x0E\\x0F\\x10\\x11\\x12\\x13\\x14\\x15\\x16\\x17\\x18\\x19\\x1A\\x1B\\x1C\\x1D\\x1E\\x1F\\x20\\x21\\x22\\x23\\x24\\x25\\x26\\x27\\x28\\x29\\x2A\\x2B\\x2C\\x2D\\x2E\\x2F0123456789\\x3A\\x3B\\x3C\\x3D\\x3E\\x3F\\x40ABCDEFGHIJKLMNOPQRSTUVWXYZ\\x5B\\x5C\\x5D\\x5E\\x5F\\x60abcdefghijklmnopqrstuvwxyz\\x7B\\x7C\\x7D\\x7E\\x7F"})
  (has-value
   {"css-value-field" *all-ascii*}
   {:css-value-field "\\01\\02\\03\\04\\05\\06\\07\\08\\09\\0A\\0B\\0C\\0D\\0E\\0F\\10\\11\\12\\13\\14\\15\\16\\17\\18\\19\\1A\\1B\\1C\\1D\\1E\\1F\\20\\21\\22\\23\\24\\25\\26\\27\\28\\29\\2A\\2B\\2C\\2D\\2E\\2F0123456789\\3A\\3B\\3C\\3D\\3E\\3F\\40ABCDEFGHIJKLMNOPQRSTUVWXYZ\\5B\\5C\\5D\\5E\\5F\\60abcdefghijklmnopqrstuvwxyz\\7B\\7C\\7D\\7E\\7F"})
  (has-value
   {"url-value-field" *all-ascii*}
   {:url-value-field "%01%02%03%04%05%06%07%08%09%0A%0B%0C%0D%0E%0F%10%11%12%13%14%15%16%17%18%19%1A%1B%1C%1D%1E%1F%20%21%22%23%24%25%26%27%28%29%2A%2B%2C%2D%2E%2F0123456789%3A%3B%3C%3D%3E%3F%40ABCDEFGHIJKLMNOPQRSTUVWXYZ%5B%5C%5D%5E%5F%60abcdefghijklmnopqrstuvwxyz%7B%7C%7D%7E%7F"}))

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
  (validate-ok {"int-field" "10"})
  (validate-ok {"int-field" "-1"})
  (validate-ok {"float-field" "2.5"})
  (validate-ok {"float-field" "-0.9"})
  (validate-ok {"float-field" "1"})
  (validate-ok {"email-field" "fred@example.com"})
  (validate-ok {"date-field" "2010-08-18"})
  (validate-ok {"date-field" "2010-08-19"})
  (validate-ok {"url-field" "http://www.example.com"})
  (validate-ok {"url-field" "http://example.co.uk"})
  (validate-ok {"bool-field" "moo"})
  (validate-ok {"bool-field" "yes"})
  (validate-ok {"bool-field" "no"})
  (validate-ok {"bool-field" ""})
  (validate-error {"string-field" "abcdefghijk"} 
                  {:string-field "Too long."})
  (validate-error {"string-field" "with space"}
                  {:string-field "pattern error"})
  (validate-error {"string-field" "nocow"}
                  {:string-field "match error"})
  (validate-error {"string-field" "moocow"}
                  {:string-field "no-match error"})
  (validate-error {"int-field" "moo"}
                  {:int-field "integer error"})
  (validate-error {"int-field" "11"}
                  {:int-field "integer error"})
  (validate-error {"int-field" "-3"}
                  {:int-field "integer error"})
  (validate-error {"float-field" "moo"}
                  {:float-field "float error"})
  (validate-error {"float-field" "-2.1"}
                  {:float-field "float error"})
  (validate-error {"float-field" "3"}
                  {:float-field "float error"})
  (validate-error {"email-field" "fred"}
                  {:email-field "email error"})
  (validate-error {"email-field" "fred@example#"}
                  {:email-field "email error"})
  (validate-error {"date-field" "2010-08-09"}
                  {:date-field "date error"})
  (validate-error {"date-field" "2010-08-11"}
                  {:date-field "date error"})
  (validate-error {"date-field" "moo"}
                  {:date-field "date error"})
  (validate-error {"url-field" "moo"}
                  {:url-field "url error"}))

(def-form required-form
  {:required "required"}
  :string-field (textbox string-field)
  :int-field (textbox int-field)
  :float-field (textbox float-field)
  :email-field (textbox email-field)
  :date-field (textbox date-field)
  :url-field (textbox url-field)
  :bool-field (textbox bool-field)
  :select-field (selectbox string-field)
  :check-field (selectbox string-field))

(deftest required
  (let [[validated errors] (validate required-form {})]
    (is (= errors
           {:string-field "required"
            :int-field "required"
            :float-field "required"
            :email-field "required"
            :date-field "required"
            :url-field "required"
            :bool-field "required"
            :select-field "required"
            :check-field "required"}))))

            

