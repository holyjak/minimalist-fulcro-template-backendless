(ns com.example.ui
  "# Data modelling in Fulcro
   
   ## UI
   
   * Left = Menu, i.e. a list of: Today, Uncategorized, Projects [prj1, prj2, ..], Tags [tag1, tag2, ...]
   * Middle - details of the selected menu item
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

(defn ui-sublist [{:keys [items key label]} {:keys [list-selected]}]
  (dom/div label
    (dom/ul
      (map #(dom/li {:key %} (dom/a {:href "#" :onClick (fn [] (list-selected [key %]))} %)) 
        items))))

(defsc Today [_ {:keys [sprints tasks]}]
  {:ident(fn [] [:component/id ::Today])
   :query [:sprints :tasks]}
  (dom/div
    (dom/h3 "Sprints")
    (dom/p "TODO: implement...")
    (count sprints)
    (dom/h3 "Tasks")
    (count tasks)))

(def ui-today (comp/computed-factory Today))

(defsc TaskList [_ {:keys [all-tasks], [type key] :selected-list :as props}]
  {:ident (fn [] [:component/id ::TaskList])
   :query [:all-tasks [:selected-list '_]]}
  (dom/div
    (dom/h3 "Task for " (name type) " " key)
    (dom/ul
      (->> all-tasks
           (filter (constantly true))
           (map (fn [{:task/keys [label tags project]}] 
                  (dom/li {:key label} 
                    (cond
                      (and (= type :project) (= key project)) "✅"
                      (and (= type :tag) (some #{key} tags)) "✅"
                      :else "❌")
                    label)))))))

(def ui-task-list (comp/computed-factory TaskList))

(defsc Menu [_ {:keys [projects tags]} {:keys [list-selected]}]
  {:ident (fn [] [:component/id ::Menu])
   :query [:projects :tags]}
  (dom/ul
    (dom/li (dom/a {:href "#" :onClick #(list-selected :today)} "Today"))
    (dom/li (dom/a {:href "#" :onClick #(list-selected :uncategorized)} "Uncategorized"))
    (dom/li (ui-sublist {:items projects :key :project :label "Projects"} {:list-selected list-selected}))
    (dom/li (ui-sublist {:items tags :key :tag :label "Tags"} {:list-selected list-selected}))))

(def ui-menu (comp/computed-factory Menu))

(m/defmutation select-list
  [{:keys [selected-list]}]
  (action [{:keys [state]}]
    (println "select-list:" selected-list)
    (swap! state assoc :selected-list selected-list)))

(defsc Root [this {:keys [task-filters >/task-list selected-list today user calendar]}]
  {:query [:selected-list 
           {:task-filters (comp/get-query Menu)}
           {:>/task-list (comp/get-query TaskList)}
           {:today (comp/get-query Today)}
           :user 
           :calendar]
   :initial-state
   (fn [_]
     {:selected-list :today
      :task-filters
      {:projects ["KosmoTimeApp"  "Private Stuff" "Kid's Stuff" "Private Works"]
       :tags ["Important" "Urgent" "2" "3" "4"]}
      :>/task-list ; Pathom would add this itself but in init. state we must add it manually
      {:all-tasks [#:task{:label "Product Planning" :url "url.." :project "KosmoTimeApp"}
                   #:task{:label "Ex enim nisi ..." :url "url.." :project "Kid's Stuff" :tags ["Important" "2" "3" "4"]}]}
      :today {:sprints [#:list{:label "App Related Spring", :tasks [#:task{:label "Set up OKR Meetings" :url "url.." :project "KosmoTimeApp"}
                                                                    #:task{:label "Checklist for the PH" :url "url.." :project "Private Stuff" :tags ["Urgent"]}
                                                                    #:task{:label "LinkedIn Strategy" :project "KosmoTimeApp"}]}
                        #:list{:label "Planning for the new website", :tasks [#:task{} #:task{}]}
                        #:list{:label "Product Strategy", :tasks [#:task{} #:task{}]}]
              :tasks [#:task{:label "Product Planning" :url "url.." :project "KosmoTimeApp"}
                      #:task{:label "Kosmo Time (Desktop Version)" :url "url.." :project "KosmoTimeApp" :tags ["Important" "2" "3"] :worked-time "03:40:45"}
                      #:task{:label "Presentation - Product" :url "url.." :project "KosmoTimeApp" :tags ["Important"]}
                      #:task{:label "Ex enim nisi ..." :url "url.." :project "Kid's Stuff" :tags ["Important" "2" "3" "4"]}
                      #:task{:label "LinkedIn Strategy"}]}
      :user #:user{:name "Mik Skuza" :icon "todo"}
      :calendar nil})}
  (dom/div
    (dom/h1 "Project Task App")
    (dom/table
      (dom/tbody
        (dom/tr {:style {:verticalAlign "top"}}
          (dom/td
            (dom/h2 "Left Pane")
            (ui-menu task-filters {:list-selected #(comp/transact! this [(select-list {:selected-list %})])}))
          (dom/td
            (dom/h2 "Middle")
            (cond
              (= selected-list :today) (ui-today today)
              (= selected-list :uncategorized) (dom/h3 "Uncategorized (unimplemented)")
              :else (ui-task-list task-list)))
          (dom/td
            (dom/h2 "Right")
            (dom/p "A fake calendar...")))))))