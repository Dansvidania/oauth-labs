(ns oauth-labs.github
  (:require [compojure.core :refer [defroutes GET]]
            [clj-http.client :as client]
            [ring.middleware.params :refer [wrap-params]]
            [clojure.pprint :refer [pprint]]
            [hiccup2.core :as h]
            [cheshire.core :as json]))

(def github-client-id (System/getenv "GITHUB_CLIENT_ID"))
(def github-client-secret (System/getenv "GITHUB_CLIENT_SECRET"))

(def redirect-url "http://localhost:3000/oauth-callback")

(def authorize-params (str "client_id=" github-client-id
                           "&redirect_url=" redirect-url
                           "&scope=\"read:user\""
                           "&state=ermagerd"))

(def authorize-url "https://github.com/login/oauth/authorize")

(def token-params (str "client_id=" github-client-id
                       "&client_secret=" github-client-secret
                       "&redirect_url=" redirect-url))

(def token-url "https://github.com/login/oauth/access_token")

(def app-state (atom {}))

(defn home-handler [_]
  (str (h/html [:div [:h1 "Home Page"]
                [:a {:href (str authorize-url "?" authorize-params)} "GITHUB LOGIN"]])))

(defn callback-handler [req]
  (let [response (client/post (str token-url "?" token-params "&code=" (get (:params req) "code")) {:headers {"Accept" "application/json"}})
        access-token (:access_token (json/parse-string (:body response) true))]

    (swap! app-state assoc :api-token access-token)
    (pprint @app-state)

    {:status 302
     :headers {"Location" "/example"}}))

(defn example-handler [_]
  (let [response (client/get "https://api.github.com/octocat" {:headers {"Authorization" (str "Bearer " (:api-token @app-state)) "Accept" "application/vnd.github+json"}})]
    (str (h/html [:pre [:code (:body response)]]))))

(defroutes app-routes
  (GET "/" [] home-handler)
  (GET "/oauth-callback" req (callback-handler req))
  (GET "/example" req (example-handler req)))

(def app
  (wrap-params app-routes))

