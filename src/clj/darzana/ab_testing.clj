(ns darzana.ab-testing)

(defn ab-testing-participate [context test-id alternatives]
  (let [client-id (get-in context [:request :cookies "darzana-client-id"] (str (java.util.UUID/randomUUID)))]
    )

(defn ab-testing-convert [context test-id]
  )
