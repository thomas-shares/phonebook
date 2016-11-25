(ns phonebook.handler-test
  (:require [clojure.test :refer :all]
            [clojure.edn :as edn]
            [clojure.spec :as s]
            [phonebook.handler :refer :all]
            [ring.mock.request :as mock]
            [clojure.spec.test :as stest]))

(stest/instrument `atomic-user-add)

(def phonebook {:db {  #uuid "80a8ea00-6072-11e5-960a-d35f77d80ceb"
                               {:first-name "Thomas"
                                :surname "van der Veen"
                                :phonenumber "0783312345"
                                :address {:street "High Street"
                                          :postcode "SO21 1QQ"}}
                      #uuid "38d77ce0-6073-11e5-960a-d35f77d80ceb"
                               {:first-name "Paul"
                                :surname "M"
                                :phonenumber "07123456"}}
                :last-added #uuid "38d77ce0-6073-11e5-960a-d35f77d80ceb"})

(atomic-user-add  phonebook  {:first-name "Paul"
                              :surname "M"
                              :phonenumber "07123456"})

(defn set-atom [f]
  (reset! phonebook-db phonebook)
  (f))

(use-fixtures :each set-atom)

(deftest get-tests
  (testing "testing if get works"
    (let [response (app (mock/request :get "/v1/phonebook"))]
      ;(println response)
      (is (= (:status response) 200))
      (is (= (edn/read-string (:body response)) (:db phonebook)))
      (is (= (:headers response) {"Content-Type" "application/edn"})))))

(deftest post-tests
  (testing "post tests"
    (let [data-to-add {:first-name "Ralph" :surname "B" :phonenumber "0123456"}
          response (app (mock/request :post "/v1/phonebook"
                           (pr-str data-to-add)))
          body (edn/read-string (:body response))]
     ;(println response " "  (:body response))
     (is (= (:status response) 201))
     (is (uuid? (edn/read-string(:body response))))
     (let [response (app (mock/request :get "/v1/phonebook"))]
       (is (= (:status response) 200))
       (is (= (edn/read-string (:body response)) (:db (assoc-in phonebook [:db body] data-to-add))))))))

(deftest delete-test
  (testing "deleting a user"
      (let [del-response (app (mock/request :delete "/v1/phonebook/80a8ea00-6072-11e5-960a-d35f77d80ceb"))
            get-response (app (mock/request :get "/v1/phonebook"))]
        (is (= (:status del-response) 200))
        (is (= (:status get-response) 200))
        (is (= (edn/read-string (:body get-response)) {#uuid "38d77ce0-6073-11e5-960a-d35f77d80ceb" {:first-name "Paul", :surname "M", :phonenumber "07123456"}})))))

(deftest put-test
  (testing "updating a user"
    (let [update-data {:first-name "Thomas" :surname "van der Veen" :phonenumber "01234567"}
          put-response (app (mock/request :put "/v1/phonebook/80a8ea00-6072-11e5-960a-d35f77d80ceb"
                                  (pr-str update-data)))
          get-response (app (mock/request :get "/v1/phonebook"))]
      ;(println put-response)
      (is (=  (:status put-response)))
      (is (= (:status put-response) 200)))))

(deftest search-test
  (testing "Search for a user that is not in the system"
    (let [missing-response (app (mock/request :get "/v1/phonebook/search?surname=m"))]
      (is (= (:status missing-response) 200))
      (is (= (:body missing-response) "{}"))))
  (testing "search for a user that is in the system"
    (let [working-response (app (mock/request :get "/v1/phonebook/search?surname=M"))]
      (is (= (:status working-response) 200))
      (is (= (edn/read-string (:body working-response)) {#uuid "38d77ce0-6073-11e5-960a-d35f77d80ceb" {:first-name "Paul", :surname "M", :phonenumber "07123456"}})))))

;(deftest spec-test)
;  (testing "Test if our spec is correct"))
;    (let [entry-db  (get (:db phonebook) "38d77ce0-6073-11e5-960a-d35f77d80ceb")])))
;          r (s/valid? :phonebook.handler/entry entry-db)])))
;      (is (true? r)))))
;      (println entry-db))))
