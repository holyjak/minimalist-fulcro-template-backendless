(ns com.example.app
  (:require
   [com.example.mock-server :as mock-server]
   [com.fulcrologic.fulcro.application :as app] ))

(defonce app (app/fulcro-app {:remotes {:remote (mock-server/mock-remote)}}))