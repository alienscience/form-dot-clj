
(ns form-dot-clj.jquery-tools
  (:require [form-dot-clj.extend :as extend])
  (:use hiccup.core)
  (:require [hiccup.page-helpers :as ph])
  (:use form-dot-clj.js-utils)
  (:use com.reasonr.scriptjure))

;;========== Javascript includes ===============================================

(defn controls-on-ready
  "Returns the javascript to be run on-ready for each control"
  (remove nil?
          (map :on-ready
               (vals (form :controls)))))

(defn include-js
  "Returns the javascript required to activate jquery-tools for the given
   form and form-id e.g
     (include-js the-form \"myform\")"
  [form form-id]
  (html
   (ph/include-js "http://cdn.jquerytools.org/1.2.3/full/jquery.tools.min.js")
   (script
    (on-ready
     (js*
      (.validator (clj (html-id form-id)))
      (clj (controls-on-ready form))
      )))))

   ;; old below
   [:script {:type "text/javascript"}
    "$(document).ready(function() {"
    (str "$(\"#" form-id "\").validator();")
    (for [c (vals (form :controls))
          :let [onr (c :on-ready)]
          :when onr]
      onr)
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
         (if (contains? field :email)
           {:type "email"})
         (if (contains? field :url)
           {:type "url"})
         (select-keys options [:name :label :size :type :maxlength :required])
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
           

;;========== Number-input ======================================================

(defn number-input
  "Creates a box that accepts numbers"
  [field options]
  (let [num-field (or (field :integer) (field :float))]
    (merge (select-keys field [:server-checks])
           (if num-field
             {:min (first num-field) :max (second num-field)})
           (select-keys options [:name :label :size :required])
           {:Control ::Number-input})))
                      

(defmethod extend/show-html ::Number-input
  [control params]
  (let [options [:name :size :min :max]
        value (-> control :name params)
        attributes (merge {:type "number"}
                          (if value {:value value})
                          (if (control :required) {:required "required"})
                          (select-keys control options))]
    (html [:input attributes])))
           

;;========== Date-input ========================================================

(defn- date-on-ready
  [id target-id date-format]
  (str "$('#" id "').dateinput({"
       "format: '" date-format "',"
       "change: function() { $('#" target-id "').val(this.getValue('yyyy-mm-dd')); }"
       "});"))

(defn- display-name [name] (str "dis" name))

(defn date-input
  "Creates a date control to handle the given field"
  [field options]
  (merge (select-keys field [:server-checks])
         (if (contains? field :date)
           {:min (-> field :date first) :max (-> field :date second)})
         (select-keys options [:name :label :size :required])
         {:Control ::Date-input
          :on-ready (date-on-ready (display-name (options :name))
                                   (options :name)
                                   (or (options :format)
                                       "d mmmm yyyy"))}))
                      
(defmethod extend/show-html ::Date-input
  [control params]
  (let [options [:size :min :max]
        control-name (control :name)
        dn (display-name control-name)
        value (params control-name)
        attributes (merge {:type "date" :name dn :id dn}
                          (if value {:value value})
                          (select-keys control options))]
    (html
     [:input {:type "hidden" :name control-name :id control-name}]
     [:input attributes])))
           


;;========== Range-input =======================================================

(def range-on-ready
     (str "$(':range').rangeinput();"))

(defn range-input
  "Creates a range input to handle the given field"
  [field options]
  (let [num-field (or (field :integer) (field :float))]
    (merge (select-keys field [:server-checks])
           (if num-field
             {:min (first num-field) :max (second num-field)}
             {:min 1 :max 10})
           (select-keys options [:name :label :step :required])
           {:Control ::Range-input
            :on-ready range-on-ready})))
                      
(defmethod extend/show-html ::Range-input
  [control params]
  (let [options [:name :min :max :step]
        value (-> control :name params)
        attributes (merge {:type "range"}
                          (if value {:value value})
                          (select-keys control options))]
    (html [:input attributes])))

