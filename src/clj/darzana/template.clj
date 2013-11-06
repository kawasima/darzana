(ns darzana.template
  (:require
    [clojure.java.io :as io]
    [clojure.data.json :as json]
    [darzana.workspace :as workspace])
  (:import
    [com.github.jknack.handlebars Handlebars Handlebars$SafeString Handlebars$Utils]
    [com.github.jknack.handlebars Helper]
    [com.github.jknack.handlebars.io FileTemplateLoader]))

(defn make-path
  ([] (.. (io/file (workspace/current-dir) "template") getPath))
  ([ws] (.. (io/file (@workspace/config :workspace) ws "template")  getPath))
  ([ws template]
    (.. (io/file (@workspace/config :workspace) ws "template" template) getPath)))

(def handlebars (ref (Handlebars. (FileTemplateLoader. (make-path)))))

(defn refresh-loader []  (. @handlebars with (into-array [(FileTemplateLoader. (make-path))])))

(.registerHelper @handlebars "debug"
  (reify Helper
    (apply [this context options]
      (Handlebars$SafeString.
        (str
          "<link rel=\"stylesheet\" href=\"/admin/css/debug.css\"/>"
          "<script src=\"/admin/js/debug.js\"></script>"
          "<script>var DATA="
          (json/write-str (.model (.context options))
            :value-fn
            (fn [k v]
              (cond (= (type v) net.sf.json.JSONNull) nil
                :else v)))
          ";document.write('<div class=\"darzana-debug\">' + Debug.formatJSON(DATA) + '</div>');"
          "Debug.collapsible($('.darzana-debug'), 'Debug Infomation');</script>")))))

(defn walk [dir pattern]
  (doall (filter #(re-matches pattern (.getName %))
                 (file-seq dir))))

(dosync (alter workspace/config update-in [:hook :change] conj
          refresh-loader))

