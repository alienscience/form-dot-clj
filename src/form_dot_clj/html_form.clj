
(ns form-dot-clj.core
  "Functions for generating a form as HTML"
  (:use clojure.contrib.def)
  (:use hiccup.core))

;; TODO: see about making controls/errors configurable by the control library

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

(defn generate-form
  "Generates the HTML for a form given the HTML for the
   individual controls"
  [attributes controls submit]
  (html
   [:form attributes
    (apply str controls)
    submit]))
