(ns app
  (:use [darzana.api]))

;; Github
(defapi github-repository-search
  (url "https://api.github.com/search/repositories")
  (query-keys
    (assign "clojure" => :q))
  (expire 300)
  (headers (assign "application/vnd.github.preview" => :Accept)))

;; Twitter

(defapi twitter-authorize
  (url "https://api.twitter.com/oauth2/token")
  (query-keys
    (assign "client_credentials" => :grant_type))
  (basic-auth :twitter-consumer-key :twitter-consumer-secret)
  (method :post))

(defapi twitter-timeline
  (url "https://api.twitter.com/1.1/search/tweets.json")
  (query-keys
    (assign 10 => :count)
    (assign "clojure" => :q))
  (expire 300)
  (oauth-token [:twitter-authorize :access_token]))

;; Hotpepper

(defapi hotpepper-gourmet
  (url "http://webservice.recruit.co.jp/hotpepper/gourmet/v1/")
  (query-keys (assign :hotpepper-key => :key) :name :middle_area)
  (expire 300))

(defapi hotpepper-large-area
  (url "http://webservice.recruit.co.jp/hotpepper/large_area/v1/")
  (query-keys (assign :hotpepper-key => :key))
  (expire 1800))

(defapi hotpepper-middle-area
  (url "http://webservice.recruit.co.jp/hotpepper/middle_area/v1/")
  (query-keys (assign :hotpepper-key => :key) :large_area :middle_area)
  (expire 1800))

;; Yahoo

(defapi y-local
  (url "http://search.olp.yahooapis.jp/OpenLocalPlatform/V1/localSearch")
  (query-keys
    (assign :y-app-id => :appid)
    :query :gc :ac :lat :lon :dist :bbox :sort)
  (expire 300))

(defapi y-news
  (url "http://news.yahooapis.jp/NewsWebService/V2/topics")
  (query-keys
    (assign :y-app-id => :appid)
    :category :topicname :pickupcategory :query :sort :results :start))

;; ATND

(defapi atnd-events
  (url "http://api.atnd.org/events/")
  (query-keys :event_id :keyword :ym :ymd :user_id :twitter_id :start :count :format)
  (expire 300))

(defapi atnd-event-users
  (url "http://api.atnd.org/events/users/")
  (query-keys :event_id :user_id :twitter_id :start :count :format)
  (expire 300))

;; Cacoo

(defapi cacoo-request-token
  (url "https://cacoo.com/oauth/request_token")
  (method :post))

;; Hatena

(defapi hatena-oauth-initiate
  (url "https://www.hatena.com/oauth/initiate")
  (method :post)
  (query-keys (assign "read_public" => :scope))
  (oauth-1-authorization
    (assign :hatena-consumer-key => :oauth_consumer_key)
    (assign :hatena-consumer-secret => :oauth_consumer_secret)
    (assign :hatena-callback => :oauth_callback)))

(defapi hatena-oauth-token
  (url "https://www.hatena.com/oauth/token")
  (method :post)
  (oauth-1-authorization
    (assign :hatena-consumer-key => :oauth_consumer_key)
    (assign :hatena-consumer-secret => :oauth_consumer_secret)
    (assign :hatena-request-toekn-secret => :oauth_token_secret)
    :oauth_verifier :oauth_token ))

(defapi hatena-my-bookmark
  (url "http://api.b.hatena.ne.jp/1/my/tags")
  (oauth-1-authorization
    (assign :hatena-consumer-key => :oauth_consumer_key)
    (assign :hatena-consumer-secret => :oauth_consumer_secret)
    (assign :hatena-access-token => :oauth_token)
    (assign :hatena-access-token-secret => :oauth_token_secret))
  (expire 900))

(defapi hatena-bookmark-feed
  (url "http://b.hatena.ne.jp/:user_id/rss")
  (query-keys :tag)
  (expire 900))

