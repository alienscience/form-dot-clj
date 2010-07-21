
(ns form-dot-clj.jquery-tools
  (:require [form-dot-clj.extend :as extend])
  (:use hiccup.core))

;;========== Javascript includes ===============================================

(defn include-js
  "Returns the javascript required to activate jquery-tools for the given
   form-id e.g
     (include-js \"myform\")"
  [form-id]
  (html
   [:script {:type "text/javascript"
             :src "http://cdn.jquerytools.org/1.2.3/full/jquery.tools.min.js"}]
   [:script {:type "text/javascript"}
    "$(document).ready(function() {"
    (str "$(\"#" form-id "\").validator();")
    "});"]))

                

;;========== Textbox ===========================================================

(defn textbox
  "Creates a textbox to handle the given field"
  [field options]
  (merge (select-keys field [:server-checks])
         (if (contains? field :maxlength)
           {:maxlength (first (field :maxlength))})
         (if (contains? field :pattern)
           {:pattern (first (field :pattern))})
         (select-keys options [:name :size :type :maxlength :required])
         {:Control ::Textbox}))
                      
(defmethod extend/show-html ::Textbox
  [control params]
  (let [options [:name :size :maxlength :type :pattern]
        value (-> control :name params)
        attributes (merge {:type "text"}
                          (if value {:value value})
                          (if (control :required) {:required "required"})
                          (select-keys control options))]
    (html [:input attributes])))
           
