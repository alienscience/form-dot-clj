(defproject uk.org.alienscience/form-dot-clj "0.0.1"
  :description "HTML form display and validation."
  :namespaces [form-dot-clj.core]
  :dependencies [[hiccup "0.2.6"]
                 [clj-time "0.1.0-SNAPSHOT"]]
  :dev-dependencies [[org.clojure/clojure "1.1.0"]
                     [org.clojure/clojure-contrib "1.1.0"]
                     [swank-clojure "1.2.1"]
                     [ring/ring-jetty-adapter "0.2.5"]
                     [compojure "0.4.1"]]
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "same as Clojure"})

