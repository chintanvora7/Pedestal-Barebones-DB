(ns sample.runtime)
;; https://github.com/weavejester/duct
;; https://github.com/duct-framework/duct/blob/master/duct/src/duct/util/runtime.clj

(def ^:private hooks (atom {}))

(defonce ^:private init-shutdown-hook
  (delay (.addShutdownHook (Runtime/getRuntime)
                           (Thread.
                            #(doseq [f (vals @hooks)] (f))))))

(defn add-shutdown-hook! [k f]
  (force init-shutdown-hook)
  (swap! hooks assoc k f))

(defn remove-shutdown-hook! [k]
  (swap! hooks dissoc k))


;; https://stuartsierra.com/2015/05/27/clojure-uncaught-exceptions

;; "This is a global, JVM-wide setting. There can be only one
;; default uncaught exception handler. Individual Threads and ThreadGroups can have their own handlers, which get called in preference to the default handler. See Thread.setDefaultUncaughtExceptionHandler. "

;; http://docs.oracle.com/javase/8/docs/api/java/lang/Thread.html#setDefaultUncaughtExceptionHandler-java.lang.Thread.UncaughtExceptionHandler-

(defn set-default-uncaught-exception-handler!
  "Adds a exception handler, which is a 2-arity function of thread name and exception."
  [f]
  (Thread/setDefaultUncaughtExceptionHandler
   (reify Thread$UncaughtExceptionHandler
     (uncaughtException [_ thread ex]
       (f thread ex)))))

(defn unset-default-uncaught-exception-handler!
  []
  (Thread/setDefaultUncaughtExceptionHandler nil))
