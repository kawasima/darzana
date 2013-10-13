(ns darzana.code-mirror)

(. js/CodeMirror defineMode "mustache"
  (fn [config parserConfig]
    (. js/CodeMirror overlayMode
      (. js/CodeMirror getMode config (or (. parserConfig -backdrop) "text/html"))
      (js-obj
        "token"
        (fn [stream state]
          (if (.match stream "{{")
            (do
              (loop [ch (.next stream)]
                (when-not (and (= ch "}") (= (.next stream) "}"))
                  (recur (.next stream))))
              (.eat stream "}"))
            (loop [ch (.next stream)]
              (when-not (or (nil? ch) (.match stream "{{" false))
                (recur (.next stream))))))))))
