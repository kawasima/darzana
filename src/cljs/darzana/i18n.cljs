(ns darzana.i18n
  (:require [clojure.string :as str])
  (:use-macros [jayq.macros :only [let-ajax]]))

(defn fq-name "Like `name` but inclues namespace in string when present."
  [x] (if (string? x) x
        (let [n (name x)]
          (if-let [ns (namespace x)] (str ns "/" n) n))))
(defn explode-keyword [k] (str/split (fq-name k) #"[\./]"))

(defn merge-keywords [ks & [as-ns?]]
  (let [parts (->> ks (filterv identity) (mapv explode-keyword) (reduce into []))]
    (when-not (empty? parts)
      (if as-ns?
        (keyword (str/join "." parts))
        (let [ppop (pop parts)]
          (keyword (when-not (empty? ppop) (str/join "." ppop))
            (peek parts)))))))

(def locale
  (memoize
    (fn [loc] "en"))) ;; TODO

(def locale-key "Returns locale keyword for given Locale object or locale keyword."
  (memoize #(keyword (str/replace (str (locale %)) "_" "-"))))

(defn fmt-str "Like clojure.core/format but takes a locale."
  ^String [loc fmt & args] (apply goog.string/format fmt args))


(def dev-mode?       "Global fallback dev-mode?." (atom true))
(def fallback-locale "Global fallback dev-mode?." (atom :en))

(def scoped "Merges scope keywords: (scope :a.b :c/d :e) => :a.b.c.d/e"
  (memoize (fn [& ks] (merge-keywords ks))))

(def ^:dynamic *tscope* nil)
(def ^:private dict-cache (atom {}))

(defn- compile-dict-path
  "[:locale :ns1 ... :nsN unscoped-key<decorator> translation] =>
  {:ns1.<...>.nsN/unscoped-key {:locale (f translation decorator)}}"
  [raw-dict path]
  (assert (>= (count path) 3) (str "Malformed dictionary path: " path))
  (let [[loc :as path] (vec path)
        translation (peek path)
        scope-ks    (subvec path 1 (- (count path) 2)) ; [:ns1 ... :nsN]
        [_ unscoped-k decorator] (->> (re-find #"([^!\*_]+)([!\*_].*)*"
                                               (name (peek (pop path))))
                                      (mapv keyword))
        translation (if-not (keyword? translation)
                      translation
                      (let [target ; Translation alias
                            (get-in raw-dict
                                    (into [loc]
                                          (->> (utils/explode-keyword translation)
                                               (mapv keyword))))]
                        (when-not (keyword? target) target)))]
    (when-let [translation
               (when translation
                 (case decorator
                   (:_comment :_note) nil
                   (:_html :!)        translation
                   (:_md   :*)        (-> translation utils/html-escape
                                          (utils/markdown {:inline? false}))
                   (-> translation utils/html-escape
                       (utils/markdown {:inline? true}))))]

      {(apply scoped (conj scope-ks unscoped-k)) {loc translation}})))

(defn- inherit-parent-trs
  "Merges each locale's translations over its parent locale translations."
  [dict]
  (into {}
    (for [loc (keys dict)]
      (let [loc-parts (str/split (name loc) #"[-_]")
             loc-tree  (mapv #(keyword (str/join "-" %))
                         (take-while identity (iterate butlast loc-parts)))]
        [loc (apply utils/merge-deep (map dict (rseq loc-tree)))]))))



(defn- compile-dict [raw-dict dev-mode?]
  (if-let [dd (and (or
                     (not dev-mode?)
                     (not (string? raw-dict))
                     ;;(not (utils/file-responses-modified? [raw-dict]))
                     )
                (@dict-cache raw-dict))]
    @dd
    (let [dd
           (delay
             (let [raw-dict
                    (if-not (string? raw-dict)
                      raw-dict
                      (let-ajax [raw-dict { :url raw-dict
                                            :dataType :json }]
                        raw-dict))]
               (->> (inherit-parent-trs raw-dict)
                 (leaf-nodes)
                 (map (partial compile-dict-path raw-dict))
                 (apply merge-with merge))
               ))]
      (swap! dict-cache assoc raw-dict dd)
      @dd)))

(defn translate
  [loc config scope k-or-ks & fmt-args]
 (let [{ :keys [ dev-mode? dictionary fallback-locale log-missing-translation-fn root-scope fmt-fn]
         :or   { dev-mode?       @dev-mode?
                 fallback-locale (or (:default-locale config)
                                   @fallback-locale)
                 fmt-fn          fmt-str
                 }} config

         dict    (compile-dict dictionary dev-mode?)
         ks      (if (vector? k-or-ks) k-or-ks [k-or-ks])
         get-tr  #(get-in dict [(scoped scope %1) (locale-key %2)])
         tr
         (or (some #(get-tr % loc) (take-while keyword? ks))
           (let [last-k (peek ks)]
             (if-not (keyword? last-k)
               last-k
               (do (when-let [log-f log-missing-translation-fn]
                     (log-f { :dev-mode? dev-mode?
                              :locale loc :scope scope :ks ks}))
                 (or
                   (some #(get-tr % fallback-locale) ks)
                   (when-let [pattern (or (get-tr :missing loc)
                                        (get-tr :missing fallback-locale))]
                     (let [str* #(if (nil? %) "nil" (str %))]
                       (fmt-str loc pattern (str* loc) (str* scope) (str* ks)))))))))]
    (if-not fmt-args tr
      (apply fmt-fn loc tr fmt-args))))

(defn t "Like `translate` but uses a thread-local translation scope."
  [loc config k-or-ks & fmt-str-args]
  (apply translate loc config ::scope-var k-or-ks fmt-str-args))

