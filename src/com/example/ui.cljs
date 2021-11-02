(ns com.example.ui
  "# Data modeling in Fulcro
   
   ## UI
   
   * Left - list Sprints, Tasks, Uncategorized, ...
   * Middle - details of selected => *Details => union | dynamic | all@every-t
   * Right - Calendar 
   "
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

(defsc FIXME [_ _] {})
(defsc SprintItem [_ _] {:query []})
(defsc TaskItem [_ _] {:query []})
(defsc UncategorizedItem [_ _] {:query []})

(defsc SprintsDetails [_ _]
  {:query []})

(def ui-sprints (comp/computed-factory SprintItem))
(def ui-tasks (comp/computed-factory TaskItem))
(def ui-uncategorized (comp/computed-factory UncategorizedItem))

(defsc TaskLists [_ {:keys [sprints tasks uncategorized]} {:keys [list-selected]}]
  {:query [{:sprints (comp/get-query SprintItem)}
           {:tasks (comp/get-query TaskItem)}
           {:uncategorized (comp/get-query UncategorizedItem)}]}
  (dom/div
    (ui-sprints sprints {:list-selected (partial list-selected :sprints)})
    (ui-tasks tasks {:list-selected (partial list-selected :tasks)})
    (ui-uncategorized uncategorized {:list-selected (partial list-selected :uncategorized)})))

(def ui-task-lists (comp/computed-factory TaskLists))

(defsc Root [this {:keys [task-lists selected-list]}]
  {:query [{:task-lists (comp/get-query TaskLists)} :selected-list]
   :initial-state
   (fn [_]
     {:selected-list :sprints
      :task-lists
      {:sprints [#:sprint{:label "Sprint 1", :tasks ["Analyse" "Design" "Implement"]}]
       :tasks []
       :uncategorized []}
      :calendar nil})}
  (dom/div
    (dom/h1 "Project Task App")
    (ui-task-lists task-lists {:list-selected #(m/set-value! this :selected-list %)})
    (println :selected-list selected-list)
    (case selected-list
      :sprints nil
      :tasks nil
      :uncategorized nil)))