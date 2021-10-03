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

(defsc AltTarget [_ _]
  {:ident (fn [] [:component/id ::AltTarget])
   :query ['*]
   :initial-state {}
   :route-segment ["alt"]}
  (dom/p "AltTarget"))

(dr/defrouter PersonDetailsRouter [this {:keys [current-state route-factory route-props] :as props}]
  {:router-targets [DefaultTarget AltTarget]
   :always-render-body? true}
  ;; The body of the router is displayed only when the target is not ready,
  ;; i.e. in one of the states below (unless you set `:always-render-body?`)
  (println "ROUTE STATE" current-state (js/Date.))
  (case current-state
    nil (println "MISTAKE: PersonDetailsRouter is displayed but has never been routed to yet")
    :pending (dom/div "Router: Loading...")
    :failed (dom/div "Router: Failed!") ; Note: There seem to be timing issues in JS and this is sometimes trigger just after 2-3s, not 5s?!
    :routed (dom/div (str "Router: Routed to: " (:queryid (meta route-factory)))
              (dom/pre "props: " (pr-str props)))
    (println "Should never come here:" current-state)))

(def ui-person-details-router (comp/factory PersonDetailsRouter))

(defsc Person [_ {:person/keys [id name biography] router :ui/router}]
  {:ident :person/id
   :query [:person/id :person/name :person/biography {:ui/router (comp/get-query PersonDetailsRouter)}]
   :initial-state {:ui/router {}}
   :route-segment ["person" :person-id]}
  (dom/div {:style {:border "black solid 2px"}}
    (dom/p (str "Person #" id ": ") (dom/strong name) " - " biography)
    ;(dom/pre "router props: " (pr-str router))
    (ui-person-details-router router)))

(def ui-person (comp/factory Person))

(defsc Root [this {:keys [person ui/ready?] :as props}]
  {:query [:ui/ready? {:person (comp/get-query Person)}]
   :initial-state {:person {}}}
  (dom/div
    (if ready?
      (ui-person person)
      (dom/p "UI not ready yet..."))))

(m/defmutation set-ui-ready [_]
  (action [{:keys [state]}]
    (swap! state assoc :ui/ready? true)))

(m/defmutation init-ui [_]
  (action [{:keys [app]}]
    ;; Change route here, after the dr/initialize! transaction finished: 
    (dr/change-route! app ["alt"])
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
