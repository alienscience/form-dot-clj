
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
         (select-keys options [:name :label :size :type :maxlength :required])
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
  (let [[fill-id fill-content] (or (options :fill-keys) [:id :content])]
        (merge (select-keys field [:server-checks])
               (select-keys options [:name :label :size :fill-fn :required])
               {:fill-id fill-id :fill-content fill-content}
               {:Control ::Selectbox})))

(defmethod extend/show-html ::Selectbox
  [control params]
  (let [options [:name :size]
        value (-> control :name params)
        attributes (select-keys control options)
        {:keys [fill-fn fill-id fill-content]} control]
    (html
     [:select attributes
      (for [{id fill-id content fill-content} (fill-fn)
            :let [id-str (.toString id)]]
        (html
         [:option (merge {:value id} (if (= value id-str)
                                       {:selected "selected"}))
        content]))])))
           
;;========== Radiobutton =======================================================

(defn radiobutton
  "Creates a set of html radiobuttons"
  [field options]
  (let [[fill-id fill-content] (or (options :fill-keys) [:id :content])]
        (merge (select-keys field [:server-checks])
               (select-keys options [:name :label :fill-fn])
               {:fill-id fill-id :fill-content fill-content}
               {:Control ::Radiobutton})))

(defmethod extend/show-html ::Radiobutton
  [control params]
  (let [options [:name]
        value (-> control :name params)
        attributes (select-keys control options)
        {:keys [fill-fn fill-id fill-content]} control]
    (html
     (for [{id fill-id content fill-content} (fill-fn)
           :let [id-str (.toString id)]]
       (html
        [:div.radio
         [:input (merge attributes
                        {:type "radio" :value id}
                        (if (= id-str value)
                          {:checked true}))
         content]])))))
           
;;========== Checkbox ==========================================================

(defn checkbox
  "Creates a checkbox"
  [field options]
  (merge (select-keys field [:server-checks])
         (select-keys options [:name :label :required])
         {:value (or (options :value) "yes")}
         {:Control ::Checkbox}))

(defmethod extend/show-html ::Checkbox
  [control params]
  (let [options [:name :value]
        value (-> control :name params)
        attributes (select-keys control options)]
    (html
     [:input (merge attributes
                    {:type "checkbox"}
                    (if value {:checked true}))])))
           
