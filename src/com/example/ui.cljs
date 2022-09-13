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

(defsc Root [this props]
  {:query [[df/marker-table :load-progress] :new-thing :thing/list]}
  (div
   (p "Hello from the ui/Root component!")
   (div {:style {:border "1px dashed", :margin "1em", :padding "1em"}}
    (p "Invoke a load! that fails and display the error:")
    (when-let [m (get props [df/marker-table :load-progress])]
      (dom/p "Progress marker: " (str m)))
    (button {:onClick #(df/load! this :i-fail (rc/nc '[*]) {:marker :load-progress})} "I fail!"))
   (div {:style {:border "1px dashed", :margin "1em", :padding "1em"}}
    (p "Simulate creating a new thing with server-assigned ID, leveraging Fulcro's tempid support:")
    (button {:onClick #(let [tmpid (tempid/tempid)]
                         (comp/transact! this [(mut/create-random-thing {:tmpid tmpid})]))}
            "I create!")
    (when-let [things (:new-thing props)]
      (p (str "Created a thing with the ID: " (first (keys things))))))
    (div {:style {:border "1px dashed", :margin "1em", :padding "1em"}}
      (p "Invoke a load! of a *list* of things:")
      (if-let [things (seq (:thing/list props))]
        (dom/pre (pr-str things))
        (p "Not loaded yet"))
      (button {:onClick #(df/load! this :thing/list (rc/nc '[:thing/id :thing/name]))} "Load things"))))
        