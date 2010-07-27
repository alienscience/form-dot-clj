
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
           

;;========== Selectbox =========================================================

(defn selectbox
  "Creates a html select"
  [field options]
  (merge (select-keys field [:server-checks])
         (select-keys options [:name :size :fill-fn])
         {:fill-id (or (options :fill-id) :id)
          :fill-content (or (options :fill-content) :content)}
         {:Control ::Selectbox}))


(defmethod extend/show-html ::Selectbox
  [control params]
  (let [options [:name :size]
        value (-> control :name params)
        attributes (select-keys control options)
        {:keys [fill-fn fill-id fill-content]} control]
    (html
     [:select attributes
     (for [{id fill-id content fill-content} (fill-fn)]
       [:option (merge {:value id} (if (= value id)
                                     {:selected "selected"}))
        content])])))
           
