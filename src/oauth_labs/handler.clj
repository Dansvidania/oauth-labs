(ns oauth-labs.handler
  (:require [compojure.core :refer [defroutes GET]]
            [clj-http.client :as client]
            [ring.middleware.params :refer [wrap-params]]
            [clojure.pprint :refer [pprint]]
            [hiccup2.core :as h]))

(def github-client-id (System/getenv "GITHUB_CLIENT_ID"))
(def github-client-secret (System/getenv "GITHUB_CLIENT_SECRET"))

(def authorize-params (str "client_id=" github-client-id
                           "&redirect_url=http://localhost:3000/oauth-callback"
                           "&scope=\"read:user\""
                           "&state=ermagerd"))

(def authorize-url "https://github.com/login/oauth/authorize")

(def token-params (str "client_id=" github-client-id
                       "&client_secret=" github-client-secret
                       "&redirect_url=http://localhost:3000/oauth-callback"))

(def token-url "https://github.com/login/oauth/access_token")

(defn home-handler [_]
  (str (h/html [:div [:h1 "Home Page"]
                [:a {:href (str authorize-url "?" authorize-params)} "GITHUB LOGIN"]])))

(defn callback-handler [req]
  (let [body (client/post (str token-url "?" token-params "&code=" (get (:params req) "code")))]
    (pprint body)
    {:status 200 :body (:body body)}))

(defroutes app-routes
  (GET "/" [] home-handler)
  (GET "/oauth-callback" req (callback-handler req)))

(def app
  (wrap-params app-routes))
