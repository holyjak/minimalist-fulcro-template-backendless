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
   [com.fulcrologic.fulcro.dom :as dom :refer [button div form h1 h2 h3 input label li ol p ul]]))

(defsc Child [_ {:child/keys [name age]}]
  {:ident :child/id
   :query [:child/id :child/name :child/age]}
  (div name " is " age))

(def ui-child (comp/factory Child {:keyfn :child/id}))

(defsc ChildrenList [_ {:parent/keys [children]}]
  {:ident :parent/id
   :query [:parent/id {:parent/children (comp/get-query Child)}]}
  (div "ChildrenList is:"
    (map #(div {:key (:child/id %)} (ui-child %)) children)))

(def ui-child-list (comp/factory ChildrenList))

(defsc Parent [_ {:parent/keys [name] :as props}]
  {:ident :parent/id
   :query [:parent/id :parent/name
           {:artificial/child-list (comp/get-query ChildrenList)}]
   :pre-merge (fn [{parent :data-tree}]
                (-> parent
                    (assoc :artificial/child-list
                      (select-keys parent [:parent/id]))))}
  (div "I am the terrible" name "!"
    (ui-child-list (:artificial/child-list props))))

(def ui-parent (comp/factory Parent))

(defsc Root [this props]
  {:query [{[:parent/id 1] (comp/get-query Parent)}]}
  (div
    (ui-parent (get props [:parent/id 1]))))
