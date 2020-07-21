(ns server.server
  (:require
   [org.httpkit.server :as httpkit.server]
   [compojure.core :as compojure]
   [server.port :as port]
   [config.core :refer [env]]
   [nrepl.server]
   [server.specs :as specs]
   [taoensso.timbre :as timbre]))

(defonce state (atom {:stop-server-fn nil
                      :nrepl-server nil}))

(defn nrepl-server-start [address port]
  (swap! state assoc :nrepl-server
         (nrepl.server/start-server :bind address :port port)))

(defn debug-middleware [handler]
  (fn [req]
    (clojure.pprint/pprint req)
    (handler req)))

(compojure/defroutes routes
  (compojure/GET "/" [] "<h1>Hello World</h1>")
  (compojure/GET "/person" [] (specs/gen-person)))

(def handler
  (-> #'routes
      (debug-middleware)))

(defn start-server
  "Connect a new db instance and start the server."
  [port]
  (println "Starting server")
  (swap! state assoc :stop-server-fn
         (httpkit.server/run-server #'handler {:port port})))

(defn stop-server
  "Stop server."
  []
  (if-let [stop-server-fn (some-> @state (:stop-server-fn))]
    (do
      (println "Stopping server")
      (stop-server-fn))
    (println "Server instance not found!! Is it started?")))

(defn -main
  "Main entry to start the server."
  [& [port-from-arg]]
  (port/set! port-from-arg)
  (println "Starting server on port: " (port/port))
  (timbre/info "Starting server on port: " (port/port))
  (start-server (port/port))
  (let [nrepl-address "127.0.0.1"
        nrepl-port    7888]
    (when (:production env)
      (println "NREPL starting on: " nrepl-address ", " nrepl-port)
      (nrepl-server-start nrepl-address nrepl-port))))


(comment
  (-main)
  (stop-server)
  )
