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
    [com.fulcrologic.fulcro.application :as app]
    [com.fulcrologic.fulcro.ui-state-machines :as uism))

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
   :will-enter (fn [app route-params]
                 (dr/route-immediate
                   [:person/id
                    (js/parseInt (:person-id route-params))]))
   :route-segment ["person" :person-id]}
  (dom/p (str "Person #" id ": ") (dom/strong name) " - " biography))

(dr/defrouter MyRouter [_ _]
  {:router-targets [AllPeople Person]})

(def ui-my-router (comp/factory MyRouter))

(defsc Root [this {:ui/keys [router]}]
  {:query [{:ui/router (comp/get-query MyRouter)}]
   ;:query [{:ui/router (comp/get-query MyRouter)}
   ;        [::uism/asm-id ::MyRouter]]
   :initial-state {:ui/router {}}}
  ;(let [router-state (or (uism/get-active-state this ::MyRouter) :initial)]
  ;  (if (= :initial router-state)
  ;    (dom/div :.loading "Loading...")
  (dom/div
    (dom/p (dom/button {:onClick #(dr/change-route! this ["all"])} "All")
           (dom/button {:onClick #(dr/change-route! this ["person" "123"])} "Person 123"))
    (ui-my-router router))) ;))

(defn init [app]
  ;; Avoid startup async timing issues by pre-initializing things before mount
  (app/set-root! app Root {:initialize-state? true})
  (dr/initialize! app)
  (run! #(merge/merge-component! app Person %
           :append (conj (comp/get-ident AllPeople {}) :all-people))
    [#:person{:id 100 :name "Kamča" :biography "A singer born in Ostrava..."}
     #:person{:id 123 :name "Doubravka" :biography "A princess born in the 10th century..."}])
  (dr/change-route! app ["all"]) ; after set-root or mount!
   ;; or: (dr/change-route! app (dr/path-to AllPeople))
  (app/mount! app Root "app" {:initialize-state? false}))

