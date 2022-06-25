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
  (li name " is " age))

(def ui-child (comp/factory Child {:keyfn :child/id}))

(defsc Parent [_ {:parent/keys [name children]}]
  {:ident :parent/id
   :query [:parent/id :parent/name
           {:parent/children (comp/get-query Child)}]} ; <1>
  (div "Children of " name ":" (ul (map ui-child children))))

(def ui-parent (comp/factory Parent))

(defsc Root [this props]
  {:query [{[:parent/id 1] (comp/get-query Parent)}]}
  (div
    (ui-parent (get props [:parent/id 1]))))
        