
(ns signup
  "Example of form-dot-clj - a signup form"
  (:use hiccup.core) ;; Optional
  (:use form-dot-clj.core)
  (:use form-dot-clj.html-controls))

(def-field username
  [:maxlength 20]
  [:pattern #"^\w+$" "Only alphanumeric characters please"]
  [:not-pattern #"(?i)(root|admin)" "Sorry that username is reserved"])

(def-field first-name [:maxlength 50])
(def-field last-name  [:maxlength 50])
(def-field email      [:type :email
                       "Sorry, we cannot handle that email address"])
(def-field password   [:maxlength 50])

(defn confirm-password
  "A user defined validation function."
  [params errors]
  (if-not (= (params "confirm-password")
             (params "password"))
    {:password "Password and confirmation are not the same."}
    nil))
                     
(def-form signup
  {:size 20 :required true :check-fns [confirm-password]}
  :username          (textbox username)
  :first-name        (textbox first-name)
  :last-name         (textbox last-name {:required false})
  :email             (textbox email)
  :password          (textbox password {:type "password"})
  :confirm-password  (textbox password {:type "password"}))

;;==== The easy way to display a form ==========================================

(defn sign-up-form-1
  "The easy way to display a form"
  []
  (html
   [:form {:action "/signup" :method "post"}
    (show-controls signup)
    (default-submit "Sign Up")]))

;;==== Display a form with control over some of the HTML =======================

(defn error-fn
  "Formats an error message"
  [error]
  (html [:span.error error]))

(defn format-control
  "Returns the HTML for a control on a form."
  [label control]
  (html
   [:label label] (show control)
   (on-error control error-fn)))
   
(defn sign-up-form-2
  "With formating of each control"
  []
  (html
   [:form {:action "/signup" :method "post"}
    (show-controls signup format-control)
    [:label][:input {:type "submit" :value "Sign Up"}]]))

;;==== Display a form and layout manually ======================================

(defn sign-up-form-3
  "Displays the sign-up form"
  []
  (html
   [:form {:action "/signup" :method "post"}
    [:label "Username"] (show signup :username)
    (on-error signup :username error-fn)
    [:label "First Name"] (show signup :first-name)
    (on-error signup :first-name error-fn)
    [:label "Last Name"] (show signup :last-name)
    (on-error signup :last-name error-fn)
    [:label "Email"] (show signup :email)
    (on-error signup :email error-fn)
    [:label "Password"] (show signup :password)
    (on-error signup :password error-fn)
    [:label "Confirm Password"] (show signup :confirm-password)
    (on-error signup :confirm-password error-fn)
    [:input {:type "submit" :value "Sign Up"}]]))

;;==== Handle a post ===========================================================

(defn create-user
  "A real version of this function would write to db etc"
  [params])

(defn sign-up-post-1
  "The easy way to handle a post"
  [params]
  (on-post signup params create-user sign-up-form-1))
  
(defn sign-up-post-3
  "Handle a post with more detail"
  [params]
  (let [[validated errors] (validate signup params)]
    (if errors
      (bind-controls params errors (sign-up-form-1))
      (create-user validated))))
           
