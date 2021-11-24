(ns com.example.pathom
  "The Pathom parser that is our (in-browser) backend.
   
   Add your resolvers and 'server-side' mutations here."
  (:require
   [com.wsscode.pathom3.cache :as p.cache]
   [com.wsscode.pathom3.connect.built-in.resolvers :as pbir]
   [com.wsscode.pathom3.connect.built-in.plugins :as pbip]
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
   [promesa.core :as p]
   [cljs.core.async :as async]))

;; (pco/defresolver index-explorer 
;;   "This resolver is necessary to make it possible to use 'Load index' in Fulcro Inspect - EQL"
;;   [env _]
;;   {::pco/input  #{:com.wsscode.pathom.viz.index-explorer/id}
;;    ::pco/output [:com.wsscode.pathom.viz.index-explorer/index]}
;;   {:com.wsscode.pathom.viz.index-explorer/index
;;    (-> (get env ::pc/indexes)
;;        (update ::pc/index-resolvers #(into {} (map (fn [[k v]] [k (dissoc v ::pc/resolve)])) %))
;;        (update ::pc/index-mutations #(into {} (map (fn [[k v]] [k (dissoc v ::pc/mutate)])) %)))})

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

(pco/defresolver my-sequence
  [_ _]
  {::pco/output [:sequence]}
  {:sequence (map (fn [i] #:tst{:id i :val (* 10 i)}) (range 4))})

(pco/defmutation create-random-thing [env {:keys [tmpid] :as params}]
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
  [my-sequence #_index-explorer create-random-thing i-fail person])

(def env 
  (-> #_ {:com.wsscode.pathom3.error/lenient-mode? true}
      (p.plugin/register pbip/mutation-resolve-params) ; needed or not?
      (pci/register my-resolvers-and-mutations)))

(defn new-parser []
  (fn [eql]
    (let [ch (async/promise-chan)]
      (-> (p.a.eql/process env eql)
          (p/then #(do 
                     (println "PARSER:" eql "->" %)
                     (async/go (async/>! ch %)))))
      ch)))

;; (defn new-parser 
;;   "Create a new Pathom parser with the necessary settings"
;;   []
;;   (p/parallel-parser
;;     {::p/env     {::p/reader [p/map-reader
;;                               pc/parallel-reader
;;                               pc/open-ident-reader]
;;                   ::pc/mutation-join-globals [:tempids]}
;;      ::p/mutate  pc/mutate-async
;;      ::p/plugins [(pc/connect-plugin {::pc/register my-resolvers-and-mutations})
;;                   p/error-handler-plugin
;;                   p/request-cache-plugin
;;                   (p/post-process-parser-plugin p/elide-not-found)]}))

(comment
  (p.eql/process env '[{:i-fail [*]}])
  
  (p.eql/process
    (-> {:com.wsscode.pathom3.error/lenient-mode? true}
        (pci/register
          (pco/resolver 'error
            {::pco/output [:error]}
            (fn [_ _]
              (throw (ex-info "Deu ruim." {}))))))
    [:error ::pcr/attribute-errors ::pcr/mutation-error]))