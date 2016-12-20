(defproject phonebook "0.1.0-SNAPSHOT"
  :description "A simple phonebook App that has a RESTful interface"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.9.0-alpha14"]
                 [compojure "1.5.1"]
                 [ring/ring-defaults "0.2.1"]
                 [ring/ring-anti-forgery "1.0.1"]]
                ; [danlentz/clj-uuid "0.1.6"]]

  :plugins [[lein-ring "0.10.0"]]
  :ring {:handler phonebook.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [org.clojure/test.check "0.9.0"]
                        [proto-repl "0.3.1"]
                        [ring/ring-mock "0.3.0"]]}})
