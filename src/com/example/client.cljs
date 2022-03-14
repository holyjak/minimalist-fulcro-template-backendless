(ns com.example.client
  (:require
   ;[com.example.app :refer [app]]
   [com.fulcrologic.fulcro.application :as app]
   [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
   [com.fulcrologic.fulcro.algorithms.merge :as merge]
   [com.fulcrologic.fulcro.dom :as dom]
   ["react-dom" :as rdom]))

(defsc Tab [this {:keys [label]}]
  {}
  (dom/li label))

(def ui-tab (comp/factory Tab {:keyfn :label}))

(defsc Root [this props] ; <1>
  {}                     ; <2>
  (dom/div               ; <3>
    (dom/h1 :#hdr1.pagetitle "Hello " (:username props) "!") ; <4>
    (dom/p {:style {:border "1px black"}} "Below are some tabs")
    (dom/ul
      (mapv ui-tab (:tabs props)))))

(def props
  {:username "Sokrates"
   :tabs [{:label "Tab 1"}]})

;(defonce app (app/fulcro-app))

(defn ^:export init
  "Called by shadow-cljs upon initialization, see shadow-cljs.edn"
  []
  #_#_
  (app/mount! app Root "app")
  (merge/merge! app props []))

(defn ^:export refresh "Called by shadow-cljs upon hot code reload, see shadow-cljs.edn" [])
;; FIXME: Impl refresh, really useful for deving

; native Fulcro solution
(app/mount! 
  (app/fulcro-app {:initial-db props}) 
  Root "app" {:initialize-state? false})

#_
; direct React interop
(rdom/render (comp/with-parent-context (app/fulcro-app) 
                ((comp/factory Root) props)) 
  (js/document.getElementById "app"))