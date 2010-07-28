
(ns demo
  "Demo of jquery tools"
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

;; TODO: email, number and URL types, pattern, min, max, date input, range input

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

(defn fill-num-computers
  "Function to fill a html select box"
  []
  (map (fn [i c] {:id i :content c})
       (range 1 11) (range 1 11)))
        
(def-form demo
  {:size 20 :required "This field is compulsory"}
  :username          (textbox username)
  :email             (textbox email)
  :num-computers     (selectbox num-computers
                                {:size 5
                                 :fill-fn fill-num-computers})
;;  :ability           (range-input ability {:step 2})
;;  :dob               (date-input dob {:format "dd mmm yy"})
  :url               (textbox home-page)
  )

(defn stylesheet [href]
  (html
   [:link {:rel "stylesheet" :type "text/css" :href href}]))

(defn show-form []
  (html
   [:head
    [:title "Demo Form"]
    (stylesheet "http://static.flowplayer.org/tools/css/standalone.css")
    (stylesheet "http://static.flowplayer.org/tools/demos/validator/css/form.css")
    (stylesheet "http://static.flowplayer.org/tools/demos/dateinput/css/skin1.css")
    (stylesheet "http://static.flowplayer.org/tools/demos/rangeinput/css/skin1.css")
    [:style {:type "text/css"}
     ".slider { width: 200px;}"
     ".range {display block; float: none;}"]
    (include-js demo "myform")]
   [:body
    [:form#myform {:action "/" :method "post"}
     [:fieldset
      (show-controls demo)
      [:p (default-submit "Submit")]]]]))
  
(defn success [params]
  (html
   [:h1 "Successful Post"]
   (str params)))
     
(defroutes routing
  (GET "/" [] (show-form))
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
     
