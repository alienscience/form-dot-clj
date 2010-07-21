
(ns form-dot-clj.html-controls
  (:require [form-dot-clj.extend :as extend])
  (:use hiccup.core))

;;========== Textbox ===========================================================

(defn textbox
  "Creates a textbox to handle the given field"
  [field options]
  (merge (select-keys field [:server-checks])
         (if (contains? field :maxlength)
           {:maxlength (first (field :maxlength))}
           {})
         (select-keys options [:name :size :type :maxlength :required])
         {:Control ::Textbox}))

(defmethod extend/show-html ::Textbox
  [control params]
  (let [options [:name :size :maxlength :type]
        value (-> control :name params)
        attributes (merge {:type "text"}
                          (if value {:value value}) 
                          (select-keys control options))]
    (html [:input attributes])))
           
