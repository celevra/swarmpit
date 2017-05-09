(ns swarmpit.handler
  (:require [bidi.ring :refer [make-handler]]
            [clojure.walk :as walk]
            [swarmpit.api :as api]
            [swarmpit.token :as token]))

(defn json-error
  [status response]
  {:status  status
   :headers {"Content-Type" "application/json"}
   :body    {:error response}})

(defn json-ok
  ([] {:status 200})
  ([status response] {:status status
                      :body   response}))

;;; Login handler

(defn login
  [{:keys [headers]}]
  (let [token (get headers "authorization")]
    (if (nil? token)
      (json-error 400 "Authorization header missing")
      (let [user (api/user-by-token token)]
        (if (nil? user)
          (json-error 400 "User or password wrong")
          (json-ok 200 {:token (str "Bearer " (->> (token/claim user)
                                                   (token/generate-token)))}))))))

;;; Service handler

(defn services
  [_]
  (let [services (api/services)]
    (json-ok 200 services)))

(defn service
  [{:keys [route-params]}]
  (let [service (api/service (:id route-params))]
    (json-ok 200 service)))

(defn service-create
  [{:keys [params]}]
  (let [payload (walk/keywordize-keys params)
        response (api/create-service payload)]
    (json-ok 201 response)))

(defn service-update
  [{:keys [route-params params]}]
  (let [payload (walk/keywordize-keys params)]
    (api/update-service (:id route-params) payload)
    (json-ok)))

(defn service-delete
  [{:keys [route-params]}]
  (api/delete-service (:id route-params))
  (json-ok))

;;; Network handler

(defn networks
  [_]
  (let [networks (api/networks)]
    (json-ok 200 networks)))

(defn network
  [{:keys [route-params]}]
  (let [network (api/network (:id route-params))]
    (json-ok 200 network)))

(defn network-create
  [{:keys [params]}]
  (let [payload (walk/keywordize-keys params)
        response (api/create-network payload)]
    (json-ok 201 response)))

(defn network-delete
  [{:keys [route-params]}]
  (api/delete-network (:id route-params))
  (json-ok))

;;; Node handler

(defn nodes
  [_]
  (let [nodes (api/nodes)]
    (json-ok 200 nodes)))

(defn node
  [{:keys [route-params]}]
  (let [node (api/node (:id route-params))]
    (json-ok 200 node)))

;;; Task handler

(defn tasks
  [_]
  (let [tasks (api/tasks)]
    (json-ok 200 tasks)))

(defn task
  [{:keys [route-params]}]
  (let [task (api/task (:id route-params))]
    (json-ok 200 task)))

;;; Repository handler

(defn repositories
  [_]
  (let [registries (api/registries)]
    (json-ok 200 registries)))

;;; Registry handler

(defn registry
  [{:keys [route-params]}]
  (let [registry (api/registry (:id route-params))]
    (json-ok 200 registry)))

(defn registry-create
  [{:keys [params]}]
  (let [payload (walk/keywordize-keys params)
        response (api/create-registry payload)]
    (json-ok 201 response)))

;;; Handler

(def handler
  (make-handler ["/" {"login"        {:post login}
                      "services"     {:get  services
                                      :post service-create}
                      "services/"    {:get    {[:id] service}
                                      :delete {[:id] service-delete}
                                      :post   {[:id] service-update}}
                      "networks"     {:get  networks
                                      :post network-create}
                      "networks/"    {:get    {[:id] network}
                                      :delete {[:id] network-delete}}
                      "nodes"        {:get nodes}
                      "nodes/"       {:get {[:id] node}}
                      "tasks"        {:get tasks}
                      "tasks/"       {:get {[:id] task}}
                      "repositories" {:get repositories}
                      "registry"     {:post registry-create}
                      "registry/"    {:get {[:id] registry}}}]))