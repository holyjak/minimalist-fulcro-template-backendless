(ns com.example.app
  (:require
   [com.example.mock-server :as mock-server]
   [com.fulcrologic.fulcro.application :as app] ))

(def app (app/fulcro-app {:remotes {:remote (mock-server/mock-remote)}
                          ;; Add a troubleshooting middleware during development, see
                          ;; https://github.com/holyjak/fulcro-troubleshooting:
                          :render-middleware 
                          (when goog.DEBUG
                            js/holyjak.fulcro_troubleshooting.troubleshooting_render_middleware)}))