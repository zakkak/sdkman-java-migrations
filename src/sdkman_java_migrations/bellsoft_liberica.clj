(ns sdkman-java-migrations.bellsoft-liberica
  (:require [clojure.java.shell :as shell]
            [clojure.data.json :as json]
            [sdkman-java-migrations.util.sdkman :as sdkman]))

(def ^:private suffix "-librca")

(def ^:private base-url
  (str "https://api.bell-sw.com/v1/liberica/releases"
       "?arch=%s"
       "&bitness=64"
       "&bundle-type=%s"
       "&os=%s"
       "&package-type=%s"
       "&version-feature=%s"
       "&version-modifier=latest"))

(defn wire->internal
  [{:keys [downloadUrl featureVersion interimVersion updateVersion]}]
  {:version (str featureVersion "." interimVersion "." updateVersion)
   :url     downloadUrl})

(defn bundle-type
  [fx]
  (if fx
    "jdk-full"
    "jdk"))

(def package-type
  {:linux   "tar.gz"
   :macos   "zip"
   :windows "zip"})

(defn fetch-jdk
  [arch os version-feature fx]
  (let [url (format base-url arch (bundle-type fx) os ((keyword os) package-type) version-feature)
        {:keys [:exit :err :out]} (shell/sh "curl" url "-H" "accept: application/json")]
    (if (zero? exit)
      (->> (json/read-str out :key-fn keyword)
           first
           (wire->internal))
      (do (println "ERROR:" err)
          (System/exit 1)))))

(defn parse-version
  [{:keys [version]} fx]
  (if fx
    (str version ".fx" suffix)
    (str version suffix)))

(defn main
  ([version-feature os arch]
   (main version-feature os arch false))
  ([version-feature os arch fx]
   (let [os' (if (= os "macos") "mac" os)
         platform (sdkman/platform os' arch)
         last-jdk (fetch-jdk arch os version-feature fx)]
     (println (sdkman/internal->wire last-jdk (parse-version last-jdk fx) platform)))))

(defn -main
  [& args]
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

  (main "11" "linux" "arm" true)
  (main "11" "linux" "x86" true)
  (main "11" "macos" "x86" true)
  (main "11" "windows" "x86" true)

  (main "15" "linux" "arm" true)
  (main "15" "linux" "x86" true)
  (main "15" "macos" "x86" true)
  (main "15" "windows" "x86" true))