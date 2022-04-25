(ns com.example.pathom
  "The Pathom parser that is our (in-browser) backend.

   Add your resolvers and 'server-side' mutations here."
  (:require [com.wsscode.pathom.connect :as pc]
            [com.wsscode.pathom.core :as p]
            [edn-query-language.core :as eql]))

(pc/defresolver index-explorer
  "This resolver is necessary to make it possible to use 'Load index' in Fulcro Inspect - EQL"
  [env _]
  {::pc/input  #{:com.wsscode.pathom.viz.index-explorer/id}
   ::pc/output [:com.wsscode.pathom.viz.index-explorer/index]}
  {:com.wsscode.pathom.viz.index-explorer/index
   (-> (get env ::pc/indexes)
       (update ::pc/index-resolvers #(into {} (map (fn [[k v]] [k (dissoc v ::pc/resolve)])) %))
       (update ::pc/index-mutations #(into {} (map (fn [[k v]] [k (dissoc v ::pc/mutate)])) %)))})

(pc/defresolver i-fail
  [_ _]
  {::pc/input  #{}
   ::pc/output [:i-fail]}
  (throw (ex-info "Fake resolver error" {})))

(pc/defresolver person
  [_ {id :person/id}]
  {::pc/input  #{:person/id}
   ::pc/output [:person/id :person/name]}
  {:person/id id, :person/name (str "Joe #" id)})

(pc/defresolver person-interests
  [{{type-filter :interest/type} :query-params :as env} {id :person/id :as X}]
  {::pc/input  #{:person/id}
   ::pc/output [:person/interests]}
  {:person/id id,
   :person/interests 
   (cond->> [{:interest/id :running :interest/type :sport}
             {:interest/id :swimming :interest/type :sport}
             {:interest/id :sleeping :interest/type :other}]
     type-filter (filter (comp #{type-filter} :interest/type)))})

(pc/defmutation create-random-thing [env {:keys [tmpid] :as params}]
  ;; Fake generating a new server-side entity with
  ;; a server-decided actual ID
  ;; NOTE: To match with the Fulcro-sent mutation, we
  ;; need to explicitly name it to use the same symbol
  {::pc/sym 'com.example.mutations/create-random-thing
   ::pc/params [:tempid]
   ::pc/output [:tempids]}
  (println "SERVER: Simulate creating a new thing with real DB id 123" tmpid)
  {:tempids {tmpid 123}})

(def my-resolvers-and-mutations
  "Add any resolvers you make to this list (and reload to re-create the parser)"
  [index-explorer create-random-thing i-fail person person-interests])

(def query-params-to-env-plugin
  ;; copied from https://github.com/fulcrologic/fulcro-rad/blob/b21ac2b327fce322dd2cfe067370592b0cdf78bc/src/main/com/fulcrologic/rad/pathom.clj#L110
  ;; For Pathom 3, see https://github.com/fulcrologic/fulcro-rad/blob/b21ac2b327fce322dd2cfe067370592b0cdf78bc/src/main/com/fulcrologic/rad/pathom3.clj#L122
  "Adds top-level load params to env, so nested parsing layers can see them."
  {::p/wrap-parser
   (fn [parser]
     (fn [env tx]
       (let [children     (-> tx eql/query->ast :children)
             query-params (reduce
                            (fn [qps {:keys [type params] :as x}]
                              (cond-> qps
                                (and (not= :call type) (seq params)) (merge params)))
                            {}
                            children)
             env          (assoc env :query-params query-params)]
         (parser env tx))))})


(defn new-parser
  "Create a new Pathom parser with the necessary settings"
  []
  (p/parallel-parser
   {::p/env     {::p/reader [p/map-reader
                             p/env-placeholder-reader
                             pc/parallel-reader
                             pc/open-ident-reader]
                 ::pc/mutation-join-globals [:tempids]
                 ::p/placeholder-prefixes #{">"}}
    ::p/mutate  pc/mutate-async
    ::p/plugins [(pc/connect-plugin {::pc/register my-resolvers-and-mutations})
                 query-params-to-env-plugin
                 p/error-handler-plugin
                 p/request-cache-plugin
                 (p/post-process-parser-plugin p/elide-not-found)]}))
