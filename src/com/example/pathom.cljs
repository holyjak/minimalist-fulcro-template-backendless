(ns com.example.pathom
  "The Pathom parser that is our (in-browser) backend.
   
   Add your resolvers and 'server-side' mutations here." 
  {:clj-kondo/config '{:linters {:unused-namespace {:level :off}}}}
  (:require
   [cljs.core.async :as async]
   [com.wsscode.pathom.viz.ws-connector.core :as-alias pvc]
   [com.wsscode.pathom.viz.ws-connector.pathom3 :as pathom3-viz-conn]
   [com.wsscode.pathom3.cache :as p.cache]
   [com.wsscode.pathom3.connect.built-in.plugins :as pbip]
   [com.wsscode.pathom3.connect.built-in.resolvers :as pbir]
   [com.wsscode.pathom3.connect.foreign :as pcf]
   [com.wsscode.pathom3.connect.indexes :as pci]
   [com.wsscode.pathom3.connect.operation :as pco]
   [com.wsscode.pathom3.connect.operation.transit :as pcot]
   [com.wsscode.pathom3.connect.planner :as pcp]
   [com.wsscode.pathom3.connect.runner :as pcr]
   [com.wsscode.pathom3.error :as p.error]
   [com.wsscode.pathom3.format.eql :as pf.eql]
   [com.wsscode.pathom3.interface.async.eql :as p.a.eql]
   [com.wsscode.pathom3.interface.eql :as p.eql]
   [com.wsscode.pathom3.interface.smart-map :as psm]
   [com.wsscode.pathom3.path :as p.path]
   [com.wsscode.pathom3.plugin :as p.plugin]
   [promesa.core :as p]))

(pco/defresolver i-fail 
  [_ _]
  {::pco/input  []
   ::pco/output [:i-fail]}
  (throw (ex-info "Fake resolver error" {})))

(pco/defresolver person
  [_ {id :person/id}]
  {::pco/input  [:person/id]
   ::pco/output [:person/id :person/name]}
  {:person/id id, :person/name (str "Joe #" id)})

(pco/defmutation create-random-thing [_env {:keys [tmpid] :as _params}]
  ;; Fake generating a new server-side entity with
  ;; a server-decided actual ID
  ;; NOTE: To match with the Fulcro-sent mutation, we
  ;; need to explicitly name it to use the same symbol
  {::pco/op-name 'com.example.mutations/create-random-thing
   ;::pco/params [:tmpid] - derived from destructuring
   ::pco/output [:tempids]}
  (println "SERVER: Simulate creating a new thing with real DB id 123" tmpid)
  {:tempids {tmpid 123}})

(def my-resolvers-and-mutations 
  "Add any resolvers you make to this list (and reload to re-create the parser)"
  [create-random-thing i-fail person])

(def enable-pathom-viz false)

(defn connect-pathom-viz
  "Expose indices to standalone Pathom-Viz v2022+"
  [env]
  (try (when enable-pathom-viz 
         (pathom3-viz-conn/connect-env env {::pvc/parser-id `env}))
       (catch :default e
         (println "Failed to enable Pahom-Viz" e)
         env)))

(def env
  (-> {:com.wsscode.pathom3.error/lenient-mode? true}
      (p.plugin/register pbip/mutation-resolve-params) ; needed or not?
      (pci/register my-resolvers-and-mutations)
      ;; Uncomment the line below to enable Pathom Viz GUI to connect to the app
      connect-pathom-viz))

(defn new-parser "DIY parser" []
  (fn [eql]
    (let [ch (async/promise-chan)]
      (-> (p.a.eql/process env eql)
          (p/then #(do 
                     (println "PARSER:" eql "->" %)
                     (async/go (async/>! ch %)))))
      ch)))

(comment
  (p.eql/process env '[{:i-fail [*]}])

  (p.eql/process
    (pci/register
      (pco/resolver 'error
        {::pco/output [:error]}
        (fn [_ _]
          (throw (ex-info "Deu ruim." {})))))
    [:error])

  (p.eql/process
    (-> {:com.wsscode.pathom3.error/lenient-mode? true}
        (pci/register
          (pco/resolver 'error
            {::pco/output [:error]}
            (fn [_ _]
              (throw (ex-info "Deu ruim." {}))))))
    [:error ::pcr/attribute-errors ::pcr/mutation-error]) 
,)
