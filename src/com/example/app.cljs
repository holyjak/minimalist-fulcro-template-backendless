(ns com.example.app
  (:require
   [com.example.mock-server :as mock-server]
   [com.fulcrologic.fulcro.application :as app]
   [com.fulcrologic.fulcro.react.version18 :refer [with-react18]]
   [edn-query-language.core :as eql]))

(defn global-eql-transform
  [ast]
  (-> ast
      (app/default-global-eql-transform)
      ;; Make sure that if Pathom sends ::p/errors, Fulcro does not remove it:
      (update :children conj (eql/expr->ast :com.wsscode.pathom.core/errors))))

(defonce app (-> (app/fulcro-app {:remotes {:remote (mock-server/mock-remote)}
                                  :global-eql-transform global-eql-transform
                                  :remote-error?
                                  (fn [result]
                                    (or
                                     (app/default-remote-error? result)
                                     (:com.wsscode.pathom.core/errors (:body result))))
                                  :global-error-action
                                  (fn [{{:keys [body status-code error-text]} :result :as env}]
                                    (println "WARN: Remote call failed"
                                             status-code
                                             error-text
                                             (:com.wsscode.pathom.core/errors body)))})
                 (with-react18)))