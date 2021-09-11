(ns com.example.ui
  (:require 
    [com.example.mutations :as mut]
    [com.fulcrologic.fulcro.algorithms.merge :as merge]
    [com.fulcrologic.fulcro.mutations :as m]
    [com.fulcrologic.fulcro.algorithms.tempid :as tempid]
    [com.fulcrologic.fulcro.algorithms.data-targeting :as targeting]
    [com.fulcrologic.fulcro.algorithms.normalized-state :as norm]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc transact!]]
    [com.fulcrologic.fulcro.raw.components :as rc]
    [com.fulcrologic.fulcro.data-fetch :as df]    
    [com.fulcrologic.fulcro.dom :as dom :refer [button div form h1 h2 h3 input label li ol p ul]]))

(defsc Root [this props]
  {:query [[df/marker-table :load-progress] :new-thing ::m/mutation-error]}
  (div
    (p "Hello from the ui/Root component!")
    ;; Load fail
    (div {:style {:border "1px dashed", :margin "1em", :padding "1em"}}
      (p "Invoke a load! that fails and display the error:")
      (when-let [m (get props [df/marker-table :load-progress])]
        (dom/p "Progress marker: " (str m)))
      (button {:onClick #(df/load! this :i-fail (rc/nc '[*])
                           {:marker :load-progress})} 
        "I fail to load!"))
    ;; Mutate: fail
    (div {:style {:border "1px dashed", :margin "1em", :padding "1em"}}
      (p "Invoke a *mutation* that fails:")
      (button {:onClick #(comp/transact! this [(mut/failing-mut)])} 
        "I fail to mutate!"))
    ;; Mutate: create new
    (div {:style {:border "1px dashed", :margin "1em", :padding "1em"}}
      (p "Simulate creating a new thing with server-assigned ID, leveraging Fulcro's tempid support:")
      (button {:onClick #(let [tmpid (tempid/tempid)]
                           (comp/transact! this [(mut/create-random-thing {:tmpid tmpid})]))}
        "I create!")
      (when-let [err (::m/mutation-error props)] ; FIXME must fix :remote-error? to mark the err as err
        (p {:style {:color :red}} (str "The mutation failed: " err)))
      (when-let [things (:new-thing props)]
        (p (str "Created a thing with the ID: " (first (keys things))))))))
        