(ns darzana.router
  (:use
    [compojure.core :as compojure :only (GET POST PUT ANY defroutes)])
  (:require
    [compojure.handler :as handler]
    [compojure.route :as route]))

(defn escape-string [x]
  (clojure.string/replace x #"^[':\\]" "\\\\$0"))

(defn code-to-json [x]
  (condp #(%1 %2) x
    number?   x
    symbol?  (str "\"" \' (name x) "\"")
    keyword? (str "\"" \: (name x) "\"")
    string?  (str "\"" (escape-string x) "\"")
    list?    (str "[" (clojure.string/join "," (cons "\"L\"" (map code-to-json x))) "]") 
    vector?  (str "[" (clojure.string/join "," (cons "\"V\"" (map code-to-json x))) "]")
    set?     (str "[" (clojure.string/join "," (cons "\"S\"" (map code-to-json x))) "]")
    map?     (str "{" (clojure.string/join ":" (mapcat #(map code-to-json %) x)) "}" )
    (throw (Exception. (format "Unsupported type: %s" (type x))))))

(defroutes routes
  (compojure/context "/router" []
    (GET "/*" { params :params }
      { :headers {"Content-Type" "application/json"}
        :body (code-to-json
                (read-string
                  (str "(" (slurp  (str "resources/router/" (params :*) ".clj")) ")")))})))

