
(ns enlive-demo
   "Demo of form-dot-clj used with Enlive"
   (:use compojure.core, ring.adapter.jetty)
   (:require [compojure.route :as route])
   (:use net.cgrand.enlive-html)
   (:use form-dot-clj.core)
   (:use form-dot-clj.jquery-tools))

;;  Run slime or a repl in the form-dot-clj directory
;;  In slime ^c^k to compile this file
;;  To start a webserver running this example app
;; (enlive-demo/start)
;;  To stop the webserver
;; (enlive-demo/stop)

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
                     
(def-form demo
  {:size 20 :required "This field is compulsory" :check-fns [confirm-password]}
  :username          (textbox username)
  :first-name        (textbox first-name)
  :last-name         (textbox last-name {:required false})
  :email             (textbox email)
  :password          (textbox password {:type "password"})
  :confirm-password  (textbox password {:type "password"}))

;;====== The easy way to display a form ========================================

;; form-dot-clj simply replaces everything in the fieldset
(deftemplate show-form (java.io.File. "examples/form.html")
  [params errors]
  [:fieldset] (html-content (show-controls demo params errors)))

;;====== Display a form using the HTML file as a template ======================

;; This snippet uses the first control in the fieldset as a template
(defsnippet format-control
  (java.io.File. "examples/form.html") [:fieldset [:p (nth-of-type 1)]]
  [label control-html error]
  [:label] (content label)
  [:input] (substitute (html-snippet control-html))
  [:.error] (if error (content error) (substitute "")))

;; This template puts the form into the fieldset using the snippet above
(deftemplate show-form-2 (java.io.File. "examples/form.html")
  [params errors]
  [:fieldset] (content (map-controls demo params errors format-control)))


;;====== routing ===============================================================

(deftemplate success (java.io.File. "examples/form.html")
  [params]
  [:fieldset] (content (str params)))

(defroutes routing
  (GET "/" [] (show-form-2 {} {}))
  (POST "/" {params :params}
    (on-post demo params success show-form))
  (route/not-found
   "<h1>Page not found</h1>"))

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
     

