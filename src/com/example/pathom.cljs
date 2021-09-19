(ns com.example.pathom
  "The Pathom parser that is our (in-browser) backend.
   
   Add your resolvers and 'server-side' mutations here."
  (:require
    [com.wsscode.pathom.core :as p]
    [com.wsscode.pathom.connect :as pc]
    [cljs.core.async :as async :refer [go <!]]))

(pc/defresolver index-explorer 
  "This resolver is necessary to make it possible to use 'Load index' in Fulcro Inspect - EQL"
  [env _]
  {::pc/input  #{:com.wsscode.pathom.viz.index-explorer/id}
   ::pc/output [:com.wsscode.pathom.viz.index-explorer/index]}
  {:com.wsscode.pathom.viz.index-explorer/index
   (-> (get env ::pc/indexes)
       (update ::pc/index-resolvers #(into {} (map (fn [[k v]] [k (dissoc v ::pc/resolve)])) %))
       (update ::pc/index-mutations #(into {} (map (fn [[k v]] [k (dissoc v ::pc/mutate)])) %)))})

(pc/defresolver person
  [_ {:person/keys [id] :as params}]
  {::pc/input  #{:person/id}
   ::pc/output [:person/id :person/name :person/biography]}
  #_(throw (ex-info "Fake error" {}))
  (go
    (<! (async/timeout 1000))
    (println "Returning delayed...")
    #:person{:id id :name "Doubravka" :biography "A princess born in the 10th century..."}))

(def my-resolvers-and-mutations 
  "Add any resolvers you make to this list (and reload to re-create the parser)"
  [index-explorer person])

(defn new-parser 
  "Create a new Pathom parser with the necessary settings"
  []
  (p/async-parser
    {::p/env     {::p/reader [p/map-reader
                              pc/async-reader2
                              pc/open-ident-reader]
                  ::pc/mutation-join-globals [:tempids]}
     ::p/mutate  pc/mutate-async
     ::p/plugins [(pc/connect-plugin {::pc/register my-resolvers-and-mutations})
                  p/error-handler-plugin
                  p/request-cache-plugin
                  (p/post-process-parser-plugin p/elide-not-found)]}))
