(ns com.example.ui
  (:require 
    [com.example.mutations :as mut]
    [com.fulcrologic.fulcro.algorithms.merge :as merge]
    [com.fulcrologic.fulcro.algorithms.tempid :as tempid]
    [com.fulcrologic.fulcro.algorithms.data-targeting :as targeting]
    [com.fulcrologic.fulcro.algorithms.normalized-state :as norm]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc transact!]]
    [com.fulcrologic.fulcro.mutations :as m]
    [com.fulcrologic.fulcro.raw.components :as rc]
    [com.fulcrologic.fulcro.data-fetch :as df]    
    [com.fulcrologic.fulcro.dom :as dom :refer [button div form h1 h2 h3 input label li ol p ul]]
    [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
    [com.fulcrologic.fulcro.application :as app]
    [com.fulcrologic.fulcro.ui-state-machines :as uism]))

(defsc DefaultTarget [_ _]
  {:ident (fn [] [:component/id ::DefaultTarget])
   :query ['*]
   :initial-state {}
   :route-segment ["default"]}
  (dom/p "DefaultTarget"))

(defsc PersonDetails1 [_ {:person/keys [biography]}]
  {:ident :person/id
   :query [:person/id :person/biography]
   :initial-state {}
   :route-segment ["details1"]
   :will-enter (fn [app params]
                 (println "PersonDetails1 params" params)
                 (dr/route-immediate [:person/id (:person/id params)]))}
  (dom/p "PersonDetails1: bio=" biography))

(def ui-person-details1 (comp/factory PersonDetails1))

;; Store the subroute into the person then route to the target
(m/defmutation route-to-person-with-subroute [{:keys [ident subroute]}]
  (action [{:keys [app state]}]
    (swap! state assoc-in (conj ident :ui/subroute) subroute)
    (comp/transact! app [(dr/target-ready {:target ident})])))

(defsc Person [this {:person/keys [id name] 
                     :keys [>/person-details1 ui/subroute] :as props}]
  {:ident :person/id
   :query [:person/id :person/name 
           {:>/person-details1 (comp/get-query PersonDetails1)}
           :ui/subroute]
   :initial-state {:>/person-details1 {}}
   :route-segment ["person" :person-id]
   :will-enter (fn [app params]
                 (println "Person params" params)
                 (let [ident [:person/id (js/parseInt (:person-id params))]]
                   (dr/route-deferred
                     ident
                     #(comp/transact! app [(route-to-person-with-subroute {:ident ident, :subroute (:subroute params)})]))))}
  (dom/div {:style {:border "black solid 2px"}}
    (dom/p (str "Person #" id ": ") (dom/strong name))
    (case subroute
      "details1" (ui-person-details1 person-details1))
    ;(dom/pre "router props: " (pr-str router))
    #_
    (ui-person-details-router router)))

(dr/defrouter PagesRouter [_ _]
  {:router-targets [Person]})

(def ui-pages-router (comp/factory PagesRouter))

(defsc Root [_ {:keys [pages-router ui/ready?]}]
  {:query [:ui/ready? {:pages-router (comp/get-query PagesRouter)}]
   :initial-state {:pages-router {}}}
  (dom/div
    (if ready?
      (ui-pages-router pages-router)
      (dom/p "UI not ready yet..."))))

(m/defmutation set-ui-ready [_]
  (action [{:keys [state]}]
    (swap! state assoc :ui/ready? true)))

(m/defmutation init-ui [_]
  (action [{:keys [app]}]
    ;; Change route here, after the dr/initialize! transaction finished: 
    (dr/change-route! app ["person" "123"] {:subroute "details1"})
    ;; Load the data:
    (df/load! app [:person/id 123] Person
      {:target [:person]
       :post-mutation `set-ui-ready})))

(defn init [app]
  ;; Avoid startup async timing issues by pre-initializing things before mount
  (app/set-root! app Root {:initialize-state? true})
  (dr/initialize! app)
  (comp/transact! app [(init-ui nil)])
  (app/mount! app Root "app" {:initialize-state? false}))
