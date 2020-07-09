(defproject garmin-data-vis-oz "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [metasoarous/oz "1.5.4"]
                 [org.clojure/data.csv "1.0.0"]
                 [tick "0.4.26-alpha"]]
  :repl-options {:init-ns garmin-data-vis-oz.core})
