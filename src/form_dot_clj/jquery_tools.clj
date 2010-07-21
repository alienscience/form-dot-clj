
(ns form-dot-clj.jquery-tools
  (:require [form-dot-clj.extend :as extend])
  (:use hiccup.core))


;;========== Textbox ===========================================================

(defn textbox
  "Creates a textbox to handle the given field"
  [field options]
  (merge (select-keys field [:server-checks])
         (if (contains? field :maxlength)
           {:maxlength (first (field :maxlength))})
         (if (contains? field :pattern)
           {:pattern (first (field :pattern))})
         (select-keys options [:name :size :type :maxlength])
         {:Control ::Textbox}))
                      
(defmethod extend/show-html ::Textbox
  [control params]
  (let [options [:name :size :maxlength :type :pattern]
        value (-> control :name params)
        attributes (merge {:type "text"}
                          (if value {:value value}) 
                          (select-keys control options))]
    (html [:input attributes])))
           
