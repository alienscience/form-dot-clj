
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
  

(defvar- validation-fns
  {
   :maxlength maxlength
   })


(defn generate-check-fn
  "Generate a check function for the given check"
  [check args]
  (if (contains? validation-fns check)
    (apply (validation-fns check) args)))
   
(defn generate-check-fns
  "Generate server side check functions from a field definition"
  [field]
  (map (fn [x] (generate-check-fn x (field x)))
       (field :validation-seq)))
