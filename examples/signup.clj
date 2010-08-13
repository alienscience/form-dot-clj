
(ns signup
  "Minimal Signup form"
  (:use compojure.core, ring.adapter.jetty)
  (:require [compojure.route :as route])
  (:use hiccup.core)
  (:use form-dot-clj.core)
  (:use form-dot-clj.jquery-tools))

;;  In slime ^c^k to compile this file
;;  To start a webserver running this example app
;; (signup/start)
;;  To stop the webserver
;; (signup/stop)

(def-field username
  [:maxlength 20]
  [:pattern "[a-zA-Z]+" "Only alphanumeric characters please"]
  [:no-match #"(?i)(root|admin)" "Sorry that username is reserved"])

(def-field first-name [:maxlength 50])
(def-field last-name  [:maxlength 50])
(def-field email      [:email "Sorry, we cannot handle that email address"])
(def-field password   [:maxlength 50])

(defn confirm-password
  "A user defined validation function."
  [params errors]
  (if-not (= (params "confirm-password")
             (params "password"))
    {:password "Password and confirmation are not the same."}
    nil))
                     
(def-form signup
  {:size 20 :required "Required" :check-fns [confirm-password]}
  :username          (textbox username)
  :first-name        (textbox first-name)
  :last-name         (textbox last-name {:required false})
  :email             (textbox email)
  :password          (textbox password {:type "password"})
  :confirm-password  (textbox password {:type "password"}))

(defn show-form []
  (html
   [:head
    [:title "Minimal Sign-up Form"]
    [:link {:rel "stylesheet"
            :type "text/css"
            :href "http://static.flowplayer.org/tools/css/standalone.css"}]
    [:link {:rel "stylesheet"
            :type "text/css"
            :href "http://static.flowplayer.org/tools/demos/validator/css/form.css"}]
    (include-js signup "myform")]
   [:body
    [:form#myform {:action "/" :method "post"}
     [:fieldset
      (show-controls signup)
      (default-submit "Sign Up")]]]))
  
(defn success [params]
  (html
   [:h1 "Successful Post"]
   (str params)))
     
(defroutes routing
  (GET "/" [] (show-form))
  (POST "/" {params :params}
    (on-post signup params success show-form))
  (route/not-found
   (html [:h1 "Page not found"])))

(def server (atom nil))

(defn stop []
  (if-not (nil? @server)
    (do
      (.stop @server)
      (reset! server nil))))

(defn start []
  (stop)
  (let [s (run-jetty routing {:port 8081 :join? false})]
    (reset! server  s)))
     
