(ns darzana.code-mirror)

(.defineMode js/CodeMirror "mustache"
  (fn [config parserConfig]
    (.overlayMode js/CodeMirror
      (.getMode js/CodeMirror config (aget parserConfig "backdrop" "text/html"))
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
