
(ns form-dot-clj.html-helpers
  "Helper functions for generating a form as HTML"
  (:use hiccup.core))

(defn default-error
  "The default way of displaying an error"
  [error]
  (html
   [:span.error error]))
  
(defn default-control
  "The default way of displaying a control on a form."
  [label control]
  (html
   [:p
    [:label {:for (control :name)} label]
    (show control)
    (on-error control default-error)]))

(defn default-submit
  "The default way of displaying the submit button."
  [label]
  (html
   [:label][:input {:type "submit" :value label}]))
