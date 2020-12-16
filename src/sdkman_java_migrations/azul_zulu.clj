(ns sdkman-java-migrations.azul-zulu
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [sdkman-java-migrations.adapters.release :as adapters.release]
            [sdkman-java-migrations.logic.version :as logic.version]
            [sdkman-java-migrations.util.sdkman :as sdkman]))

(def ^:private vendor "zulu")
(def ^:private suffix (str "-" vendor))

(def ^:private base-url
  (str "https://api.azul.com/zulu/download/community/v1.0/bundles/latest/"
       "?jdk_version=%s"
       "&bundle_type=jdk"
       "&os=%s"
       "&arch=%s"
       "&ext=%s"
       "&hw_bitness=64"
       "&javafx=%s"))

(defn wire->internal
  [{:keys [url jdk_version]}]
  (let [[major minor patch] jdk_version]
    {:version (str major "." minor "." patch)
     :url     url}))

(def ext
  {:linux   "tar.gz"
   :macos   "tar.gz"
   :windows "zip"})

(defn fetch-jdk
  [version os arch fx]
  (let [url (format base-url version os arch ((keyword os) ext) fx)
        {:keys [status body]} (client/get url)]
    (when (= 200 status)
      (->> (json/read-str body :key-fn keyword)
           (wire->internal)))))

(defn parse-version
  [{:keys [version]}
   fx]
  (if fx
    (str version ".fx" suffix)
    (str version suffix)))

(defn main
  ([version os arch]
   (main version os arch false))
  ([version os arch fx]
   (let [os'      (if (= os "macos") "mac" os)
         platform (sdkman/platform os' arch)
         last-jdk (fetch-jdk version os arch fx)
         sdk-version (parse-version last-jdk fx)]
     (if (logic.version/is-valid? sdk-version)
       (println (adapters.release/internal->wire last-jdk sdk-version platform))
       (log/warn (str sdk-version " exceeds length."))))))

(defn -main
  []
  (main "8" "linux" "arm")
  (main "8" "linux" "x86")
  (main "8" "macos" "x86")
  (main "8" "windows" "x86")

  (main "11" "linux" "arm")
  (main "11" "linux" "x86")
  (main "11" "macos" "x86")
  (main "11" "windows" "x86")

  (main "15" "linux" "arm")
  (main "15" "linux" "x86")
  (main "15" "macos" "x86")
  (main "15" "windows" "x86")

  (main "8" "linux" "x86" true)
  (main "8" "macos" "x86" true)
  (main "8" "windows" "x86" true)

  (main "11" "linux" "x86" true)
  (main "11" "macos" "x86" true)
  (main "11" "windows" "x86" true)

  (main "15" "linux" "x86" true)
  (main "15" "macos" "x86" true)
  (main "15" "windows" "x86" true))
