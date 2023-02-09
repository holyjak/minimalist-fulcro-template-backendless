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

(pc/defresolver xyz [env _]
  {::pc/input  #{}
   ::pc/output [{:all-accounts [:account/id :account/owner :account/balance]}]}
  {:all-accounts [#:account{:id 1 :owner "Joe" :balance 100}
                  #:account{:id 2 :owner "Jane" :balance 200}]})

(def my-resolvers-and-mutations
  "Add any resolvers you make to this list (and reload to re-create the parser)"
  [xyz index-explorer])

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
                 p/error-handler-plugin
                 p/request-cache-plugin
                 (p/post-process-parser-plugin p/elide-not-found)]}))
