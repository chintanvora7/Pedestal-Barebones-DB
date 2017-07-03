(ns sample.component.db
  (:require [com.stuartsierra.component :as component]
            [meta-merge.core :refer [meta-merge]]
            [hikari-cp.core :as dbpool]
            [honeysql.helpers :refer :all]
            [pg-hstore.core :refer [to-hstore]])
  (:refer-clojure :exclude [update]))

(defn datasource-options [config]
  {:auto-commit        true
   :read-only          false
   :connection-timeout 30000
   :validation-timeout 5000
   :idle-timeout       600000
   :max-lifetime       1800000
   :minimum-idle       10
   :maximum-pool-size  10
   :pool-name          "db_pool"
   :adapter            "postgresql"
   :username           (:username config)
   :password           (:password config)
   :database-name      (:name config)
   :server-name        (:host config)
   :port-number        (:port config)
   :register-mbeans    false})


(defrecord Database [config]
  component/Lifecycle
  (start [component]
    (println "Starting Database")
    (let [options (datasource-options config)
          datasource (dbpool/make-datasource options)]
      (assoc component :datasource datasource)))

  (stop [component]
    (println "Stopping Database")
    (when-not (nil? (:datasource component))
      (println "Closing Connection")
      (dbpool/close-datasource  (:datasource component)))
    (assoc component :datasource nil)))

(defn construct-db
  [options]
  (map->Database {:config options}))

#_(defn query-by-id [db id]
    (jdbc/with-db-connection [conn {:datasource (:datasource db)}]
                             (first (jdbc/query conn
                                                (-> (select :*)
                                                    (from :test_table)
                                                    (where [:= :id (Integer. id)])
                                                    sql/format)))))


#_(defn sample []
    (let [sys (component/system-map :database (create gunter.astra.config/defaults))
          started (component/start sys)
          ds (:datasource (:database started))]
      (println ds)
      (component/stop started)))
