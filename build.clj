(ns build
  (:require [clojure.tools.build.api :as b]
            [org.corfield.build :as bb]))

(def lib 'uk.axvr/refrain)

(def version (subs (b/git-process {:git-args ["describe" "--tags" "--abbrev=0"]}) 1))

(defn jar
  "Build the JAR."
  [opts]
  (-> opts
      (assoc :lib lib :version version)
      (bb/clean)
      (bb/jar)))

(defn install
  "Install the JAR locally."
  [opts]
  (-> opts
      (assoc :lib lib :version version)
      (bb/install)))

(defn deploy
  "Deploy the JAR to Clojars."
  [opts]
  (-> opts
      (assoc :lib lib :version version)
      (bb/deploy)))
