
(ns demo
  "Demo of html controls"
  (:use compojure.core, ring.adapter.jetty)
  (:require [compojure.route :as route])
  (:use hiccup.core)
  (:use form-dot-clj.core)
  (:use form-dot-clj.html-controls))

;;  In slime ^c^k to compile this file
;;  To start a webserver running this example app
;; (demo/start)
;;  To stop the webserver
;; (demo/stop)

(def-field username
  [:maxlength 20]
  [:pattern "[a-zA-Z]+" "Only alphanumeric characters please"]
  [:no-match #"(?i)(root|admin)" "Sorry that username is reserved"])

(def-field email
  [:email "Sorry, we cannot handle that email address"])

(def-field num-computers
  [:integer 1 10 "This must be an integer between 1 and 10"])

(def-field dob
  [:date "1900-01-01" "2010-7-10" "Must be a date between 1900 and 2010"])

(def-field ability
  [:integer 1 10 "This must be an integer between 1 and 10"])
 
(def-field home-page
  [:maxlength 256]
  [:url "Sorry, we cannot handle that URL"])

(def-field os
  [:pattern "(windows|linux|bsd)"
  "Sorry, we can only handle windows, linux or bsd"])

(def-field likes-demo
  [:boolean])
  
(defn fill-num-computers
  "Function to fill a html select box"
  []
  (map (fn [i c] {:id i :content c})
       (range 1 11) (range 1 11)))

(defn fill-os
  "Function to fill the OS radiobutton"
  []
  [{:id "windows" :desc "Anything by Microsoft"}
   {:id "linux" :desc "Any flavour of Linux"}
   {:id "bsd" :desc "Open/Free/Net BSD"}
   {:id "macos" :desc "Mac OS"}])

(def-form demo
  {:size 20 :required "This field is compulsory"}
  :username          (textbox username)
  :email             (textbox email)
  :num-computers     (selectbox num-computers 
                                {:size 1 
                                 :fill-fn fill-num-computers})
  :os                (radiobutton os {:fill-fn fill-os
                                      :fill-keys [:id :desc]})
  :likes-demo        (checkbox likes-demo
                               {:label "Do you like this demo?"})
  :url               (textbox home-page)
  )

(defn show-form [params errors]
  (html
   [:head
    [:title "Demo Form"]
    [:style {:type "text/css"}
     "label { display: block }"]]
   [:body
    [:form#myform {:action "/" :method "post"}
     [:fieldset
      (show-controls demo params errors)
      [:p (default-submit "Submit")]]]]))
  
(defn success [params]
  (html
   [:h1 "Successful Post"]
   (str params)))
     
(defroutes routing
  (GET "/" [] (show-form {} {}))
  (POST "/" {params :params}
    (on-post demo params success show-form))
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
     
