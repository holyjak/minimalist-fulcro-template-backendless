(ns com.example.mutations
  "Client-side mutations.
   
   'Server-side' mutations could normally be also defined here, only with
   `#?(:clj ...)` but here even the 'server' runs in the browser so we must
   define them in another ns, which we do in `...pathom`."
  (:require
   [com.fulcrologic.fulcro.mutations :refer [defmutation]]))

(defmutation unused [{:keys [tmpid]}]
  (action [{:keys [state] :as env}]
          state)
  (remote [_] true))