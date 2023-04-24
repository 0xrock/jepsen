(ns jepsen.edge.core
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.pprint :refer [pprint]]
            [clojure.tools.logging :refer :all]
            [knossos.model :as model]
            [slingshot.slingshot :refer [try+]]
            [jepsen.checker :as checker]
            [jepsen.client :as client]
            [jepsen.cli :as cli]
            [jepsen.control :as c]
            [jepsen.db :as db]
            [jepsen.generator :as gen] 
            [jepsen.independent :as independent]
            [jepsen.nemesis :as nemesis]
            [jepsen.tests :as tests] 
            [jepsen.checker.timeline :as timeline]
            [jepsen.nemesis.time :as nt]
            [jepsen.control.util :as cu]
            [jepsen.util :as util :refer [timeout with-retry map-vals]]
            [jepsen.os.debian :as debian] 
            [jepsen.edge.db :as td]
            [jepsen.edge.client :as ec]))

(defn balance   [] {:type :invoke, :f :get_balance,  :value nil})
(defn tx   [] {:type :invoke, :f :send_transaction, :value nil})

(defrecord EdgeClient [node]
  client/Client

  (setup! [this test])

  (open! [_ test node]
    (EdgeClient. node))

  (invoke! [_ test op]
    (let [[k v] (:value op)
          crash (if (= (:f op) :get_balance)
                  :fail
                  :info)]
      (try+
       (case (:f op)
         :get_balance  (assoc op
                       :type :ok
                       :value (independent/tuple k (ec/default-get-balance node)))
         :send_transaction (do (ec/default-send-tx! node)
                    (assoc op :type :ok)))

       (catch [] e
         (assoc op :type :fail)))))

  (teardown! [_ test])

  (close! [_ test]))

(defn workload
  "Given a test map, computes

      {:generator a generator of client ops
       :client    a client to execute those ops}."
  [test]
  (let [n (count (:nodes test))]
    (case (:workload test)
      :edge {:client    (EdgeClient. nil)
                     :concurrency (* 2 n)
                     :generator (independent/concurrent-generator
                                 (* 2 n)
                                 (range)
                                 (fn [k]
                                  ;;  (gen/once {:f :send_transaction})
                                   (gen/once (tx))))
                     :final-generator (delay
                                        (independent/concurrent-generator
                                         (* 2 n)
                                         (range)
                                         (fn [k]
                                          ;;  (gen/once {:f :get_balance})
                                           (gen/once (balance))
                                           )))})))

(defn edge-test
  "Given an options map from the command line runner (e.g. :nodes, :ssh,
  :concurrency ...), constructs a test map."
  [opts]
  (let [test (merge opts
                    tests/noop-test
                    {:name "edge-test"
                     :os debian/os
                     :pure-generators true})
        dbt (td/db test)
        workload (workload test)
        test-with-db (merge test {:db dbt
                                  :client     (:client workload)
                                  :concurrency     (:concurrency workload)
                                  :generator  (gen/phases
                                               (->> (:generator workload)
                                                    (gen/time-limit (:time-limit opts)))
                                               (gen/sleep 30)
                                               (gen/clients
                                                (:final-generator workload)))})]
    test-with-db))
