(ns com.example.mock-server
  "A mock remote for Fulcro that talks to the in-browser Pathom parser
   
   Inspired heavily by https://github.com/fulcrologic/fulcro-developer-guide/blob/master/src/book/book/pathom.cljs"
  (:require
   [com.example.pathom :as pathom]
   [com.fulcrologic.fulcro.algorithms.tx-processing :as txn]    
   [com.fulcrologic.fulcro.networking.mock-server-remote :refer [mock-http-server]]))

(defn mock-remote
  "A remote in Fulcro is just a map with a `:transmit!` key"
  ([]
   (let [parser    (pathom/new-parser)
         transmit! (:transmit! (mock-http-server {:parser (fn [eql] (parser eql))}))]
     {:transmit! (fn [this send-node]
                   (js/setTimeout ; simulate some network delay, for fun
                    #(transmit! this send-node
                                (update send-node
                                        ::txn/result-handler
                                        (fn [handler]
                                          ;; FIXME: Does this work for mutations?!
                                          (fn logging-wrapper [res] (println "MOCK SERVER RESULT>" res)
                                            (handler res)))))
                    100))})))
