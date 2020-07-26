(ns server.routes
  (:require
   [compojure.core :as compojure]
   [ring.middleware.json :refer [wrap-json-response]]
   [clojure.java.io :as io]
   [ring.util.response :refer [response]]
   [server.specs :as specs]))


(defn debug-middleware [handler]
  (fn [req]
    (clojure.pprint/pprint req)
    (handler req)))

(compojure/defroutes routes
  (compojure/GET "/" [] (slurp (io/resource "README.html")))
  (compojure/GET "/person" [] (response (specs/gen-person)))
  (compojure/GET "/persons" [] (response (specs/gen-persons))))

(def handler
  (-> #'routes
      wrap-json-response
      (debug-middleware)))
