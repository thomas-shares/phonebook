(ns phonebook.handler
  (:require [clojure.edn :as edn]
            [clojure.spec :as s]
            [compojure.core :refer :all]
        ;;    [clj-uuid :as uuid]
        ;;    [schema.core :as s]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.session :as session]
            [ring.util.response :as r]))

(import java.util.UUID)

;Acceptance criteria.
;- List all entries in the phone book.
;- Create a new entry to the phone book.
;- Remove an existing entry in the phone book.
;- Update an existing entry in the phone book.
;- Search for entries in the phone book by surname.

;A phone book entry must contain the following details:
;- Surname
;- First name
;- Phone number
;- Address (optional)

;(def spec   {:first-name s/Str})
;             :surname s/Str})
;             :phone-number s/Str})
;             (s/optional-key :address) {:place s/Str}})
;                                        :country s/Str}})
(s/def ::first-name string?)
(s/def ::surname string?)
(s/def ::phonenumber string?)
(s/def ::place string?)
(s/def ::country string?)

(s/def ::address (s/keys :req-un [::country ::place]))

(s/def ::entry (s/keys :req-un [::first-name ::surname ::phonenumber]
                   :opt-un [::address]))

(s/explain  ::entry   {:first-name :key
                       :surname "van der Veen"
                       :phonenumber "0783312345"
                       :address {:country "High Street"
                                 :place   "SO21 1QQ"}})

(s/def ::db (s/* ::entry))
(s/def ::last-added uuid?)

(def phonebook-db-spec (s/keys :req-un [::db ::last-added]))

(def phonebook-db (atom {:db {}
                         :last-added #uuid "38d77ce0-6073-11e5-960a-d35f77d80ceb"}))

(defn get-phonebook []
  (let [data (pr-str (:db @phonebook-db))]
    (-> (r/response data)
        (r/content-type "application/edn"))))

;(s/fdef atomic-user-add)
;  :args (s/cat :db phonebook-db-spec :data ::entry))
  ;:ret phonebook-db-spec)
;  :fn)

(defn atomic-user-add [db data]
  (let [new-uuid (UUID/randomUUID)
        new-db (assoc-in db [:db new-uuid] data)]
    (assoc-in new-db [:last-added] new-uuid)))

(defn add-user [data]
  (let [parsed-data (edn/read-string data)]
    ;(println parsed-data)
    (if (s/valid? ::entry parsed-data)
      (do
        ;(println "valid data")
        (let [{id :last-added} (swap! phonebook-db atomic-user-add parsed-data)]
         {:status 201 :body (pr-str id)}))
      {:status 400 :body (str "malformd request:\n" (s/explain ::entry parsed-data))})))


(defn delete-user [id]
  (let [uuid (java.util.UUID/fromString id)]
    (if (contains? (:db @phonebook-db ) uuid)
      (do (swap! phonebook-db update-in [:db] dissoc uuid)
        {:status 200})
      {:status 404 :body (str id " does not exist\n")})))

(defn update-user [id data]
  (let [uuid ( java.util.UUID/fromString id)
        parsed-data (edn/read-string data)]
    (if (contains? (:db @phonebook-db) uuid)
      (do
        (if (s/valid? ::entry parsed-data)
          (do
            (swap! phonebook-db assoc-in [:db uuid] parsed-data)
            {:status 200})
          {:status 400 :body (str "malformed request\n" (s/explain ::entry parsed-data))}))
      {:status 404 :body (str id " does not exist\n")})))

(defn search-users [params]
  (let [surname (:surname params)
        filtered (into {}
                   (filter #(= surname (:surname (second %)))
                     (:db @phonebook-db)))]
      (-> (r/response (pr-str filtered))
          (r/content-type "application/edn"))))

(defroutes app-routes
  (GET  "/v1/phonebook" [] (get-phonebook))
  (POST "/v1/phonebook" {body :body}  (add-user (slurp body)))
  (PUT  "/v1/phonebook/:id" {body :body params :params}
                            (update-user  (:id params) (slurp body)))
  (DELETE "/v1/phonebook/:id" [id] (delete-user id))
  (GET    "/v1/phonebook/search" {params :params} (search-users params))
  (route/not-found "Not Found"))

(def app
  (-> app-routes
      (handler/site)
      (session/wrap-session)))
