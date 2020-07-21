(ns server.port
  (:require
   [config.core]))

(defonce state (atom {:server-port nil}))

(defn port []
  (-> @state :server-port))

(defn- parse-port [port]
  (try
    (if (integer? port)
      port
      (Integer/parseInt port))
    (catch Exception e nil)))

(defn set! [port]
  (let [p (or (parse-port port)
              (:server-port config.core/env)
              3000)]
    (swap! state assoc :server-port p)))
