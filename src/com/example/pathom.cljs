(ns com.example.pathom
  "The Pathom parser that is our (in-browser) backend.
   
   Add your resolvers and 'server-side' mutations here."
  (:require
    [com.wsscode.pathom.core :as p]
    [com.wsscode.pathom.connect :as pc]))

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

(pc/defmutation failing-mut [_ _]
  {::pc/sym 'com.example.mutations/failing-mut}
  (throw (ex-info "The failing mutation always fails" {:doomed? true})))

(def my-resolvers-and-mutations 
  "Add any resolvers you make to this list (and reload to re-create the parser)"
  [index-explorer i-fail 
   create-random-thing failing-mut])

(defn new-parser 
  "Create a new Pathom parser with the necessary settings"
  []
  ;; NOTE: By default use `parser` in Clojure and `async-parser` in the 1-threaded JS
  ;;       (so that you can call e.g. js/fetch and return its result via core.async)
  (p/async-parser 
    {::p/env     {::p/reader [p/map-reader
                              pc/parallel-reader
                              pc/open-ident-reader]
                  ::pc/mutation-join-globals [:tempids]}
     ::p/mutate  pc/mutate-async
     ::p/plugins [(pc/connect-plugin {::pc/register my-resolvers-and-mutations})
                  p/error-handler-plugin
                  p/request-cache-plugin
                  (p/post-process-parser-plugin p/elide-not-found)]}))
