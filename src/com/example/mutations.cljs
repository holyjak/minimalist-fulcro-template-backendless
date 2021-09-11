(ns com.example.mutations
  "Client-side mutations.
   
   'Server-side' mutations could normally be also defined here, only with
   `#?(:clj ...)` but here even the 'server' runs in the browser so we must
   define them in another ns, which we do in `...pathom`."
  (:require
   [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
   [com.fulcrologic.fulcro.raw.components :as rc]))

(defmutation create-random-thing [{:keys [tmpid]}]
  (action [{:keys [state] :as env}]
          (swap! state assoc-in [:new-thing tmpid] {:id tmpid, :txt "A new thing!"}))
  ;(remote [_] true)
  (remote [env] true (m/returning env (rc/nc '[:fake]))))


(defmutation failing-mut [_]
  (remote [_] true))