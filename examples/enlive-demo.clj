
(ns enlive-demo
   "Demo of form-dot-clj used with Enlive"
   (:use net.cgrand.enlive-html)
   (:use form-dot-clj.core)
   (:use form-dot-clj.html-controls))

(def-field username
  [:maxlength 20]
  [:pattern "[a-zA-Z]+" "Only alphanumeric characters please"]
  [:no-match #"(?i)(root|admin)" "Sorry that username is reserved"])


(def-form demo
  {:size 20 :required "This field is compulsory"}
  :username (textbox username))

(deftemplate show-form "enlive.html"
  []
  ;; [:fieldset] (content (show-controls demo)))
  [:fieldset] (content "Hello world"))

(deftemplate success "/home/saul/projects/form-dot-clj/examples/enlive.html"
  [params]
  [:fieldset] (content (show-controls demo)))

(defroutes routing
  (GET "/" [] (show-form))
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
     

