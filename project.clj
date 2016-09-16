(defproject phonebook "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.9.0-alpha11"]
                 [compojure "1.5.1"]
                 [ring/ring-defaults "0.2.1"]
                 [ring/ring-anti-forgery "1.0.1"]
                 [danlentz/clj-uuid "0.1.6"]]

  :plugins [[lein-ring "0.8.13"]]
  :ring {:handler phonebook.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [midje "1.8.3"]
                        [ring-mock "0.1.5"]]}})
