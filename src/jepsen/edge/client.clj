(ns jepsen.edge.client
  "Client for edge."
  (:refer-clojure :exclude [read])
  )

(def port 5000)
(def signer {:private-key "6acc43ba21cfff332106a9318e9ed08c11e7222273419c2c728dbe1d1a9aa032",
             :address "0xCB7038f9Bd7762a46bBb2A5208B6644e1945cb52"})
(def recipient "0x9927ff21b9bb0eee9b0ee4867ebf9102d12d6ecb")

;; (defn set-signer [] (reset! cloth/global-signer signer))

;; (defn set-rpc-endpoint
;;   [node]
;;   (reset! cloth.chain/ethereum-rpc (str "http://" node ":" port)))

(defn default-send-tx!
  "Parameterless send tx"
  [node]
  ()
  ;; (->
  ;;  (set-rpc-endpoint node)
  ;;  @(cloth/sign-and-send! {:to recipient :value 100000000N} signer))
  )
  

(defn default-get-balance
  "Parameterless get balance"
  [node]
  ()
  ;; (->
  ;;  (set-rpc-endpoint node)
  ;;  (cloth.chain/get-balance recipient))
  )
   