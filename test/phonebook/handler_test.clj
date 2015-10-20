(ns phonebook.handler-test
  (:require [clojure.test :refer :all]
            [clojure.edn :as edn]
            [ring.mock.request :as mock]
            [phonebook.handler :refer :all]
            [midje.sweet :refer :all]))

(import java.util.UUID)

(def phonebook {:db {  "80a8ea00-6072-11e5-960a-d35f77d80ceb"
                               {:first-name "Thomas"
                                :surname "van der Veen"
                                :phonenumber "0783312345"
                                :adress {:street "High Street"
                                        :postcode "SO21 1QQ"}}
                      "38d77ce0-6073-11e5-960a-d35f77d80ceb"
                               {:firstname "Paul"
                                :surname "M"
                                :phonenumber "07123456"} }
                :last-added "38d77ce0-6073-11e5-960a-d35f77d80ceb"})

(facts "GET test"
  (with-state-changes [(before :facts (do (reset! phonebook-db phonebook)))]
    (fact "Test getting all entries"
      (let [response (app (mock/request :get "/v1/phonebook"))]
        (:status response) => 200
        (edn/read-string (:body response)) =>  (:db phonebook)))))

(facts "POST tests" 
  (with-state-changes [(before :contents (do (reset! phonebook-db phonebook)))]
    (fact "Adding one entry"
      (let [data-to-add {:firstname "Ralph" :surname "B" :phonenumber "0123456"}
            response (app (mock/request :post "/v1/phonebook"
                             (pr-str data-to-add))) 
            body (edn/read-string  (:body response))]
        (:status response) => 201 
        (fact "check if added"  
          (let [  response (app (mock/request :get "/v1/phonebook"))]
            (:status response) => 200
            (edn/read-string (:body response))  =>  (:db  (assoc-in phonebook [:db body] data-to-add ))))))))

(facts "DELETE tests"
  (with-state-changes [(before :contents (do (reset! phonebook-db phonebook)))]
    (fact "removing valid entry")
      (let [response (app (mock/request :delete "/v1/phonebook/80a8ea00-6072-11e5-960a-d35f77d80ceb" ))]
      (:status response) => 200
      (:body response) => "")
    (fact "check result"
      (let [response (app (mock/request :get "/v1/phonebook"))]
      (:status response) => 200
      (:body response) => (pr-str {"38d77ce0-6073-11e5-960a-d35f77d80ceb"
                                    {:surname "M" :firstname "Paul" :phonenumber "07123456"}} )))))

(facts "PUT tests"
  (with-state-changes [(before :contents (do (reset! phonebook-db phonebook)))]
    (fact "update a valid user"
      (let [response (app (mock/request :put "/v1/phonebook/80a8ea00-6072-11e5-960a-d35f77d80ceb"
                            (pr-str {:firstname "Thomas" :surname "van der Veen" :phonenumber "01234567"}))) ]
        (:status response) => 200))
        (fact "check updated user"
          (let [response (app (mock/request :get "/v1/phonebook"))]
            (:status response) => 200
            (get (edn/read-string (:body response))
              "80a8ea00-6072-11e5-960a-d35f77d80ceb") => 
                {:firstname "Thomas" :surname "van der Veen" :phonenumber "01234567"}))))

(facts "search test"
  (with-state-changes [(before :contents (do (reset! phonebook-db phonebook)))]
    (fact "search for mising user"
      (let [response (app (mock/request :get "/v1/phonebook/search?surname=m"))]  
        (:status response) => 200
        (:body response) => "{}")))
  (fact "search for an existing user"
    (let [response (app (mock/request :get "/v1/phonebook/search?surname=M"))]
      (:status response) => 200
      (:body response) =>  (pr-str { "38d77ce0-6073-11e5-960a-d35f77d80ceb"
                                    {:firstname "Paul" :surname "M" :phonenumber "07123456"}}))))


(facts "test schemas"
  (fact "test correct schema"
    (let [r (validate   (get (:db phonebook) "38d77ce0-6073-11e5-960a-d35f77d80ceb" ))]
          r => {:valid true}))
  (fact "test schema with address"
    (let [r (validate {:firstname "" :surname "" :phonenumber "" :address {:place "" :country ""}})]
          r => {:valid true}))
  (fact "test incorrect schema"
    (let [r (validate {:firstnme "" :surname "" :phonenumber ""})]
          r => {:invalid true :reason
             "Value does not match schema: {:firstname missing-required-key, :firstnme disallowed-key}"}))
  (fact "with wrong value"
    (let [r (validate {:firstname "" :surname "" :phonenumber 12334})]
          r => {:invalid true :reason 
             "Value does not match schema: {:phonenumber (not (instance? java.lang.String 12334))}"})))
