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

(defsc Account [_ props]
  {:ident :account/id
   :query [:account/id :account/owner :account/balance]}
  (p (str props)))
(def ui-account (comp/factory Account))

(defsc AccountList [_ {:account-list/keys [accounts]}]
 ;; Note: In practice, this would be UI-only comp. with no query
 ;; and we would put the list of accounts directly under Root
  {:ident (fn [] [:component/id :AccountList])
   :query [{:account-list/accounts (comp/get-query Account)}]
   :initial-state {}}
  (div
   (h2 "Accounts")
   (map ui-account accounts)))
(def ui-account-list (comp/factory AccountList))

;; LEFT OUT Customer, CustomerList, their ui-* ;;

(defsc Root [this {:root/keys [accounts]}]
  {:query [{:root/accounts (comp/get-query AccountList)}
           #_{:root/customers (comp/get-query CustomerList)}]
   :initial-state #:root{:accounts {}}}
  (div
   (h1 "Your bank")
   (dom/button {:onClick #(df/load! this :all-accounts Account ;
                                    {:target (targeting/replace-at [:component/id :AccountList :account-list/accounts])})} "Load!")
   (ui-account-list accounts)
   #_(ui-customer-list customers)))

(comment
  ;; Somewhere during app startup, we would do:
  (do
    (df/load! com.example.app/app :all-accounts Account ;
              {:target (targeting/replace-at [:component/id :AccountList :account-list/accounts])}) ;
    (df/load! app :all-customers Customer
              {:target (targeting/replace-at [:component/id :CustomerList :customer-list/customers])}))

  )
        