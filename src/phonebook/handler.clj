(ns phonebook.handler
  (:require [clojure.edn :as edn]
            [compojure.core :refer :all]
            [clj-uuid :as uuid]
            [schema.core :as s]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.session :as session ]
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

(def schema {:first-name s/Str
             :surname s/Str
             :phone-number s/Str
             (s/optional-key :address) {:place s/Str
                                        :country s/Str }})

(def phonebook-db (atom {:db { }
                         :last-added "38d77ce0-6073-11e5-960a-d35f77d80ceb"}))

(defn validate [data]
  ; returns a map with key :valid if it has been successfully validated
  ; and return a map with key :invalid if not, in that case it also contains
  ; the error message as a value for key :reason
  ;(println data)
  (try
    (s/validate schema data)
    {:valid true}
  (catch Exception e 
    {:invalid true :reason (.getMessage e)} )))

(defn get-phonebook []
  (let [data (pr-str (:db @phonebook-db))]
    (-> (r/response data)
        (r/content-type "application/edn"))))

(defn atomic-user-add [db data]
  (let [new-uuid (.toString (UUID/randomUUID))
        new-db (assoc-in db [:db new-uuid] data)]
    (assoc-in new-db [:last-added] (clojure.string/replace new-uuid "\"" "" ))))

(defn add-user [data]
  (let [parsed-data (edn/read-string data)]
    (if-let [error (s/check schema parsed-data)]
      {:status 400 :body (str "malformd request:\n" error) }
      (do
        (let [{id :last-added} (swap! phonebook-db atomic-user-add parsed-data)]
        {:status 201 :body (pr-str id)})))))

(defn delete-user [id]
  (if (contains? (:db @phonebook-db ) id)
    (do (swap! phonebook-db update-in [:db] dissoc id)
      {:status 200})
    {:status 404 :body (str id " does not exist\n")}))

(defn update-user [id data]
  (let [parsed-data (edn/read-string data)]
    (if (contains? (:db @phonebook-db) id)
      (do 
        (if-let [error (s/check schema parsed-data)]
          {:status 400 :body (str "malformed request\n" error)}
          (do 
            (swap! phonebook-db assoc-in [:db id] parsed-data)
            {:status 200})))
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
