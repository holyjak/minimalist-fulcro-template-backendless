(ns com.example.ui
  (:require 
    [com.example.mutations :as mut]
    [com.fulcrologic.fulcro.algorithms.merge :as merge]
    [com.fulcrologic.fulcro.algorithms.tempid :as tempid]
    [com.fulcrologic.fulcro.algorithms.data-targeting :as targeting]
    [com.fulcrologic.fulcro.algorithms.normalized-state :as norm]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc transact!]]
    [com.fulcrologic.fulcro.raw.components :as rc]
    [com.fulcrologic.fulcro.data-fetch :as df]    
    [com.fulcrologic.fulcro.dom :as dom :refer [button div form h1 h2 h3 input label li ol p ul]]
    [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
    [com.fulcrologic.fulcro.application :as app]))

(defsc AllPeople [_ {:keys [all-people]}]
  {:ident (fn [] [:component/id ::AllPeople])
   :query [{:all-people [:person/id :person/name]}]
   :initial-state {}
   :route-segment ["all"]}
   (dom/div
     (dom/h3 "All People")
     (dom/ul
       (mapv (fn [{:person/keys [id name]}] (dom/li {:key id} name))
         all-people))))

(defsc Person [_ {:person/keys [id name biography]}]
  {:ident :person/id
   :query [:person/id :person/name :person/biography]
   :initial-state {}
   :will-enter (fn [app {id :person-id :as route-params}]
                 (let [ident [:person/id (js/parseInt id)]]
                   (if (-> (app/current-state app) (get-in ident) :person/biography)
                     (dr/route-immediate ident)
                     (dr/route-deferred ident
                       #(df/load! app ident Person
                          {:post-mutation `dr/target-ready
                           :post-mutation-params {:target ident}})))))
   :route-segment ["person" :person-id]}
  (dom/p (str "Person #" id ": ") (dom/strong name) " - " biography))

(dr/defrouter MyRouter [_ {:keys [current-state route-factory route-props]}]
  {:router-targets [AllPeople Person]}
  ;; The body of the router is displayed only when the target is not ready,
  ;; i.e. in one of the states below (unless you set `:always-render-body?`)
  (println "ROUTE STATE" current-state (js/Date.))
  (case current-state
    nil (println "MISTAKE: MyRouter is displayed but has never been routed to yet")
    :pending (dom/div "Loading...")
    :failed (dom/div "Failed!") ; Note: There seem to be timing issues in JS and this is sometimes trigger just after 2-3s, not 5s?!
    (println "Should never come here:" current-state)))

(def ui-my-router (comp/factory MyRouter))

(defsc Root [this {:ui/keys [router] :as props}]
  {:query [{:ui/router (comp/get-query MyRouter)}]
   :initial-state {:ui/router {}}}
  (dom/div
    (dom/p (dom/button {:onClick #(dr/change-route! this ["all"])} "All")
      (dom/button {:onClick #(dr/change-route! this ["person" "123"]
                               {:error-timeout 5000 :deferred-timeout 100})}
        "Person 123"))
    (ui-my-router router)))

(defn init [app]
  ;; Avoid startup async timing issues by pre-initializing things before mount
  (app/set-root! app Root {:initialize-state? true})
  (dr/initialize! app) ; note: "async", only triggers a mutation
  (dr/change-route! app ["all"]) ; after set-root or mount!
  (run! #(merge/merge-component! app Person %
           :append (conj (comp/get-ident AllPeople {}) :all-people))
    [#:person{:id 100 :name "Kamča"}
     #:person{:id 123 :name "Doubravka"}])
   ;; or: (dr/change-route! app (dr/path-to AllPeople))
  (app/mount! app Root "app" {:initialize-state? false}))

