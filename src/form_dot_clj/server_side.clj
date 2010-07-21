
(ns form-dot-clj.server-side
  "Server side validation checks"
  (:use clojure.contrib.def))

(defn maxlength
  "Performs a maxlength check on a string.
   This test should only fail when submitting using a script."
  [length]
  (fn [s]
    (if (> (count s) length)
      {:error "Too long."}
      {})))
  
(defn pattern
  "Performs a pattern match on a string. Pattern is a string.
   The pattern is in the style of HTML5 and will be matched the whole string,
   a leading ^ and trailing $ are not required."
  [pattern error-message]
  (let [full-match (str "^" pattern "$")
        re (re-pattern full-match)]
  (fn [s]
    (if-not (re-find re s)
      {:error error-message}
      {}))))
  
(defvar- validation-fns
  {
   :maxlength maxlength
   :pattern pattern
   })


(defn generate-check-fn
  "Generate a check function for the given check"
  [check args]
  (if (contains? validation-fns check)
    (apply (validation-fns check) args)))
   
(defn generate-check-fns
  "Generate server side check functions from a field definition"
  [field]
  (remove nil?
          (map (fn [x] (generate-check-fn x (field x)))
               (field :validation-seq))))
