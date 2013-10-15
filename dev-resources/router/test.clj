(defmarga GET "/" (render "index"))

(defmarga GET "/github/"
  (call-api [app/github-repository-search])
  (render "github/index"))
(defmarga
 GET
 "/hotpepper/"
 (call-api [app/hotpepper-large-area])
  (ab-testing-participate "darzana-ab"
    (ab-testing-alternative "type-a" (render "hotpepper/large_area_A"))
    (ab-testing-alternative "type-b" (render "hotpepper/large_area_B"))))
(defmarga
 GET
 "/hotpepper/middle_area"
 (call-api [app/hotpepper-middle-area])
 (render "hotpepper/middle_area"))
(defmarga
 GET
 "/hotpepper/gourmet"
 (call-api [app/hotpepper-gourmet app/hotpepper-middle-area])
  (ab-testing-convert "darzana-ab")
 (render "hotpepper/gourmet"))
(defmarga
 GET
 "/hatena/"
 (call-api [app/hatena-oauth-initiate])
 (store-session
  (assign
   [:hatena-oauth-initiate :oauth_token_secret]
   =>
   :hatena-request-toekn-secret))
 (redirect
  "https://www.hatena.ne.jp/oauth/authorize"
  {:query-keys
   [(assign [:hatena-oauth-initiate :oauth_token] => :oauth_token)]}))
(defmarga
 GET
 "/hatena/authorize"
 (call-api [app/hatena-oauth-token])
 (store-session
  (assign [:hatena-oauth-token :oauth_token] => :hatena-access-token))
 (store-session
  (assign
   [:hatena-oauth-token :oauth_token_secret]
   =>
   :hatena-access-token-secret))
 (redirect "http://localhost:3000/hatena/my-bookmark"))
(defmarga
 GET
 "/hatena/my-bookmark"
 (call-api [app/hatena-my-bookmark])
 (render "hatena/my-bookmark"))
(defmarga
 GET
 "/hatena/show/:tag"
 (call-api [app/hatena-bookmark-feed])
 (render "hatena/show"))
(defmarga
 GET
 "/twitter/"
 (if-contains
  :twitter-consumer-key
   (->
     (if-contains :twitter-authorize (->) (call-api [app/twitter-authorize]))
     (call-api [app/twitter-timeline])
     (store-session :twitter-authorize)
     (render "twitter/index"))
   (render "guide")))
