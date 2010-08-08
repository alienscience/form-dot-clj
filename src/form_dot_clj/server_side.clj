
(ns form-dot-clj.server-side
  "Server side validation checks"
  (:use clojure.contrib.def)
  (:use clj-time.core)
  (:use clj-time.format))

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

(defn no-match
  "Executes the given regex on a string. Returns an error
   if there is a match"
  [re error-message]
  (fn [s]
    (if (re-find re s)
      {:error error-message}
      {})))

(defn check-integer
  "Returns a function to convert and check an integer."
  [min max error-message]
  (fn [s]
    (try
      (let [i (Integer. s)]
        (if-not (and (>= i min) (<= i max))
          {:error error-message}
          {:value i}))
      (catch NumberFormatException e
        {:error error-message}))))
      
(defn check-float
  "Returns a function to convert and check a floating point number."
  [min max error-message]
  (fn [s]
    (try
      (let [f (Float. s)]
        (if-not (and (>= f min) (<= f max))
          {:error error-message}
          {:value f}))
      (catch NumberFormatException e
        {:error error-message}))))

(defn email
  "Returns a function to check if an email address is valid.
   Maximum length:
     http://stackoverflow.com/questions/386294/maximum-length-of-a-valid-email-id
   Uses regex based on:
     http://www.w3.org/TR/html5/states-of-the-type-attribute.html#e-mail-state"
  [error-message]
  (let [re #"^(?i)[-a-z0-9~!$%^&*_=+}{\'?]+(\.[-a-z0-9~!$%^&*_=+}{\'?]+)*@[-a-z0-9_][-a-z0-9_]*(\.[-a-z0-9_]+)*$"]
    (fn [s]
      (if-not (and (<= (.length s) 256)
                   (re-find re s))
        {:error error-message}
        {}))))

(def date-format (formatters :date))
 
(defn check-date
  "Returns a function to check if a date is correct"
  [min-date max-date error-message]
  (let [date-range (interval (parse date-format min-date)
                             (parse date-format max-date))]
    (fn [s]
      (try
        (let [d (parse date-format s)]
          (if-not (within? date-range d)
            {:error error-message}
            {:value d}))
        (catch Exception e
          {:error error-message})))))

(defn check-url
  "Returns a function to check is a url is correct.
   Does not enforce a max length."
  [error-message]
  (let [re #"^\w{3,}:(//)[\w\d:#@%/;$()~_?\+-=\\\.&]{3,}$"]
    (fn [s]
      (if-not (re-find re s)
        {:error error-message}
        {}))))

(defn check-match
  "Returns a function that matches a regular expression to a string."
  [re error-message]
  (fn [s]
    (if-not (re-find re s)
      {:error error-message}
      {})))
   
(defn get-boolean
  "Returns a function that extracts a boolean no matter what"
  []
  (let [re-no #"(?i)(no|false)"]
    (fn [s]
      (if (or (nil? s)
              (= (.length s) 0)
              (re-find re-no s))
        {:value false}
        {:value true}))))


(defvar- validation-fns
  {:maxlength maxlength
   :pattern pattern
   :no-match no-match
   :integer check-integer
   :float check-float
   :email email
   :date check-date
   :url check-url
   :match check-match
   :boolean get-boolean})


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
