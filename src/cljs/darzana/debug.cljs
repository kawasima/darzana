(ns darzana.debug)

(defn formatJSON [json]
  (let [x (type json)]
    (cond
      (nil? x) nil

      (or (= x js/Object) (map? json))
      (apply str (flatten ["<table>"
                            (map #(str "<tr><th>" (first %) "</th><td>"
                                    (formatJSON (second %)) "</td></tr>") (js->clj json))
                            "</table>"]))
      (or (= x js/Array) (coll? json))
      (apply str (flatten ["table"
                            (map-indexed #(str "<tr><th>" %1 "</th><td>"
                                            (formatJSON %2) "</td></tr>") (js->clj json))
                            "</table>"]))
      :else json)))

