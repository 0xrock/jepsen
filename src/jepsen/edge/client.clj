(ns jepsen.edge.client
  "Client for edge."
  (:refer-clojure :exclude [read])
  (:require [jepsen.control :as c]))
  
;; (def signer {:private-key "6acc43ba21cfff332106a9318e9ed08c11e7222273419c2c728dbe1d1a9aa032",
;;              :address "0xCB7038f9Bd7762a46bBb2A5208B6644e1945cb52"})

(def web3-path "/jepsen/jepsen/web3")
(def port 10002)
(def sender-private-key "6acc43ba21cfff332106a9318e9ed08c11e7222273419c2c728dbe1d1a9aa032")
(def sender-address "0xCB7038f9Bd7762a46bBb2A5208B6644e1945cb52")
(def recipient-address "0x9927ff21b9bb0eee9b0ee4867ebf9102d12d6ecb")
(defn node-url [node] (str "http://" node ":" port))

;; (defn set-signer [] (reset! cloth/global-signer signer))

;; (defn set-rpc-endpoint
;;   [node]
;;   (reset! cloth.chain/ethereum-rpc (str "http://" node ":" port)))

(defn default-send-tx!
  "Parameterless send tx"
  [node]
  (def command (str
                "npm run start -- sendtx "
                (node-url node) " "
                sender-private-key " "
                recipient-address " "
                "--value 10 "
                "--gas 21000 "
                "--nonce 0"))
  (-> (c/exec :echo (str "<default-send-tx> Executing command: " command))
      (c/cd web3-path (c/exec command))))

(defn default-get-balance
  "Parameterless get balance"
  [node]
  (def command (str
                "npm run start -- balance "
                (node-url node) " "
                sender-private-key " "
                recipient-address))
  (-> (c/exec :echo (str "<default-get-balance> Executing command: " command))
      (c/cd web3-path (c/exec command))))
  
   