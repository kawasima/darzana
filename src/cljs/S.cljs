(ns darzana.sexp)

(defn parse-routers [sexp-json]
  (map #((parse-route (second %)) (rest sexp-json))))

(defn parse-route [route-json]
  { :url (nth 2 route-json)
    :processor (parse-processor-block (last route-json))})

(defn parse-processor-block [processor-block-json]
  (if (= "'->" (nth processor-block-json 1))
    (map parse-processor (drop 3 processor-block-json))))

(defn parse-processor [processor-json]
  (condp = (nth processor-json 1)
    "'call-api" (parse-call-api (drop 2 processor-json))
    "'render"   (parse-render   (drop 2 processor-json))))

(defn parse-call-api [argument]
  {})

(defn parse-call-render [argument]
  {})
