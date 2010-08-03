
(ns form-dot-clj.core
  (:use clojure.contrib.def)
  (:use hiccup.core)
  (:require [form-dot-clj.server-side :as server]
            [form-dot-clj.extend :as extend]))

;;======== Variables for binding ===============================================
(defvar- *params* {} "Parameters posted from a form")
(defvar- *errors* {} "Validation errors")

(defn- build-field-def
  "Builds a field definition datastructure from a field definition"
  [validation]
  (let [as-vectors (map (fn [x]
                          [(first x) (apply vector (rest x))])
                        validation)
        order (into [] (map first (seq validation)))]
    (-> {}
        (into as-vectors)
        (assoc :validation-seq order))))

(defmacro def-field
  "Creates a var with the given name to hold a field definition.
   e.g
    (def-field username [:maxlength 20])  ;; creates (var username)"
  [field-name & validation]
  (let [field-def (build-field-def validation)
        server-checks (into [] (server/generate-check-fns field-def))
        field (assoc field-def :server-checks server-checks)]
  `(def ~field-name ~field)))

(defn- build-control-def
  "Builds a control definition from a control tuple"
  [defaults control-tuple]
  (let [[k [fun field options]] control-tuple
        control-name {:name (name k)}]
   `[~k (~fun ~field (merge ~defaults ~control-name ~options))]))
    
(defmacro def-form
  "Creates a var in the current namespace that contains
   the given form definition e.g
   (def-form login
     {:size 20}
     :username (textbox username)
     :password (textbox password {:type \"password\"}))"
  [name options & definition]
  (let [form-options (select-keys options [:check-fns])
        defaults (dissoc options :check-fns)
        pairs (partition 2 definition)
        order (into [] (map first pairs))
        controls (into {}
                       (map #(build-control-def defaults %)
                            pairs))
        form (merge form-options
                    {:display-order order}
                    {:controls controls})]
    `(def ~name ~form)))

(defn show
  "Generates the HTML for the control with the given key."
  ([control]
     (extend/show-html control *params*))
  ([form k]
     (let [control (-> form :controls k)]
       (show control))))

(defn check-until-error
  "Runs the given check-fns on posted parameters until an error occurs."
  [param check-fns]
  (if (empty? check-fns)
    {:value param}
    (let [check-result ((first check-fns) param)
          new-param (get check-result :value param)]
      (if-let [error (check-result :error)]
        {:error error :value new-param}
        (recur new-param (rest check-fns))))))
      
   
(defn check-control
  "Check the post params for the given control.
   Returns updated versions of value-map and errors-map.
   Returns on the first error."
  [value-map error-map control-tuple params]
  (let [control-key (first control-tuple)
        control (second control-tuple)
        control-name (control :name)
        param (-> control :name params)
        required (control :required)]
    (if (or (nil? param)
            (= (.length param) 0))
      (if required
        [value-map (assoc error-map control-name required)]
        [value-map error-map])
      (let [result (check-until-error param (control :server-checks))]
        [(assoc value-map control-key (result :value))
         (if (contains? result :error)
           (assoc error-map control-name (result :error))
           error-map)]))))

(defn check-controls
  "Check the values posted to the given controls.
    params    - a map of parameter names to posted values
    controls  - a map of the controls to check"
  [params controls]
  (loop [value-map {}
         error-map {}
         todo controls]
    (if (empty? todo)
      [value-map error-map]
      (let [[new-values new-errors]
            (check-control value-map error-map (first todo) params)]
        (recur new-values new-errors (rest todo))))))
  
(defn validate
  "Checks the values posted to the given controls.
   Returns a two entry vector containing a map of validated values
   and a map of error messages.
    params    - a map of parameter names to posted values
    form      - the form to check"
  [form params]
  (let [{:keys [controls check-fns]} form
        check-fn (if (seq check-fns)
                   (apply juxt check-fns)
                   (fn [& x])) 
        [value-map error-map] (check-controls params controls)
        errors (merge
                (apply merge (check-fn params error-map))
                error-map)]
    [value-map (if (empty? errors) nil errors)]))

(defn get-control-error
  "Returns the error for the given control"
  [control]
  (-> control :name *errors*))

(defn on-error
  "Executes error-fn if the given control has an error.
   form     - the form the control is on
   k        - the key of the control
   error-fn - (fn [error-message] ... )"
  ([control error-fn]
     (if-let [error (get-control-error control)]
       (error-fn error)))
  ([form k error-fn]
     (let [control (-> form :controls k)]
       (on-error control error-fn))))
    
(defmacro bind-controls
  "Binds the given parameter and error maps so they are available to
   form controls and then executes body."
  [params errors body]
  `(binding [*params* ~params
             *errors* ~errors]
     ~body))

(defn make-label
  "Converts a keyword into a human readable label"
  [k]
  (-> (name k) (.replaceAll "-" " ")))

(defn get-label
  "Gets the label for the control with the given key"
  [form k]
  (or (-> form :controls k :label)
      (make-label k)))
  
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
    [:label label]
    (show control)
    (on-error control default-error)]))

(defn default-submit
  "The default way of displaying the submit button."
  [label]
  (html
   [:label][:input {:type "submit" :value label}]))

(defn show-controls
  "Displays controls on the given form. 
   Optionally takes a function (fn [label control] ...)
   that can be used to generate the html surrounding a control."
  ([form]
     (show-controls form default-control))
  ([form format-fn]
     (apply str
            (map (fn [k]
                   (format-fn (get-label form k)
                              (-> form :controls k)))
                 (form :display-order)))))
  
(defn on-post
  "Function that handles a form post.
   Executes success-fn on success, fail-fn on fail.
   The success-fn takes a single parameter containing a map of validated
   parameters.
   The fail-fn has no parameters."
  [form params success-fn fail-fn]
  (let [[validated errors] (validate form params)]
    (if errors
      (bind-controls params errors (fail-fn))
      (success-fn validated))))
           
