(ns leiningen.ring.server
  (:require [leinjacker.deps :as deps])
  (:use [leinjacker.eval :only (eval-in-project)]
        leiningen.ring.util))

(defn load-namespaces
  "Create require forms for each of the supplied symbols. This exists because
  Clojure cannot load and use a new namespace in the same eval form."
  [& syms]
  `(require
    ~@(for [s syms :when s]
        `'~(if-let [ns (namespace s)]
             (symbol ns)
             s))))

(defn server-task
  "Shared logic for server and server-headless tasks."
  [project options]
  (ensure-handler-set! project)
  (let [project (update-in project [:ring] merge options)]
    (eval-in-project
     (deps/add-if-missing project '[ring-server "0.2.5"])
     `(ring.server.leiningen/serve
       '~(select-keys project [:ring]))
     (load-namespaces
      'ring.server.leiningen
      (-> project :ring :handler)
      (-> project :ring :init)
      (-> project :ring :destroy)))))

(defn server
  "Start a Ring server and open a browser."
  ([project]
     (server-task project {}))
  ([project port]
     (server-task project {:port (Integer. port)})))
