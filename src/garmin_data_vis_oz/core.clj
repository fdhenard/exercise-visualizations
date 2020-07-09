(ns garmin-data-vis-oz.core
  (:require [clojure.pprint :as pp]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.data.csv :as csv]
            [oz.core :as oz]
            [tick.alpha.api :as tick]))

#_(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))

(defn make-play-data [& names]
  (for [n names
        i (range 20)]
    {:time i :item n :quantity (+ (Math/pow (* i (count n)) 0.8) (rand-int (count n)))}))

(defn example-line-plot [play-data]
  {:data {:values play-data}
   :encoding {:x {:field "time"}
              :y {:field "quantity"}
              :color {:field "item" :type "nominal"}}
   :mark "line"})

(comment

  (oz/start-plot-server!)

  (do
    (def play-data (make-play-data "monkey" "slipper" "broom"))
    play-data)
;; => ({:time 0, :item "monkey", :quantity 5.0}
;;     {:time 1, :item "monkey", :quantity 6.192962712629476}
;;     {:time 2, :item "monkey", :quantity 12.30037210271847}
;;     {:time 3, :item "monkey", :quantity 12.097596309015797}
;;     {:time 4, :item "monkey", :quantity 17.71068609258575}
;;     {:time 5, :item "monkey", :quantity 19.194870523363548}
;;     {:time 6, :item "monkey", :quantity 22.580936309501134}
;;     {:time 7, :item "monkey", :quantity 19.888381054913122}
;;     {:time 8, :item "monkey", :quantity 26.13058987556147}
;;     {:time 9, :item "monkey", :quantity 29.317280693371046}
;;     {:time 10, :item "monkey", :quantity 26.45580618665162}
;;     {:time 11, :item "monkey", :quantity 32.55190884598056}
;;     {:time 12, :item "monkey", :quantity 33.610188015018984}
;;     {:time 13, :item "monkey", :quantity 35.63440373999379}
;;     {:time 14, :item "monkey", :quantity 37.62768266080517}
;;     {:time 15, :item "monkey", :quantity 39.59266228400805}
;;     {:time 16, :item "monkey", :quantity 40.53159496449108}
;;     {:time 17, :item "monkey", :quantity 44.44642466614132}
;;     {:time 18, :item "monkey", :quantity 43.338844810888254}
;;     {:time 19, :item "monkey", :quantity 48.21034266761982}
;;     {:time 0, :item "slipper", :quantity 1.0}
;;     {:time 1, :item "slipper", :quantity 5.743276393803367}
;;     {:time 2, :item "slipper", :quantity 8.258523872989459}
;;     {:time 3, :item "slipper", :quantity 12.42287530066645}
;;     {:time 4, :item "slipper", :quantity 17.378925219250924}
;;     {:time 5, :item "slipper", :quantity 19.189151347155786}
;;     {:time 6, :item "slipper", :quantity 21.888381054913122}
;;     {:time 7, :item "slipper", :quantity 28.498670948012276}
;;     {:time 8, :item "slipper", :quantity 26.03516289842348}
;;     {:time 9, :item "slipper", :quantity 27.508850275948053}
;;     {:time 10, :item "slipper", :quantity 29.928050775697603}
;;     {:time 11, :item "slipper", :quantity 36.299260572778856}
;;     {:time 12, :item "slipper", :quantity 40.62768266080517}
;;     {:time 13, :item "slipper", :quantity 41.9175705807045}
;;     {:time 14, :item "slipper", :quantity 44.17246133441246}
;;     {:time 15, :item "slipper", :quantity 46.39533859324643}
;;     {:time 16, :item "slipper", :quantity 43.58875032686557}
;;     {:time 17, :item "slipper", :quantity 51.75489563854074}
;;     {:time 18, :item "slipper", :quantity 47.89569020671064}
;;     {:time 19, :item "slipper", :quantity 50.012816499808885}
;;     {:time 0, :item "broom", :quantity 1.0}
;;     {:time 1, :item "broom", :quantity 7.623898318388478}
;;     {:time 2, :item "broom", :quantity 10.309573444801934}
;;     {:time 3, :item "broom", :quantity 11.727161387290321}
;;     {:time 4, :item "broom", :quantity 13.98560543306118}
;;     {:time 5, :item "broom", :quantity 17.132639022018836}
;;     {:time 6, :item "broom", :quantity 18.194870523363548}
;;     {:time 7, :item "broom", :quantity 21.189151347155786}
;;     {:time 8, :item "broom", :quantity 22.127049995800743}
;;     {:time 9, :item "broom", :quantity 23.016965485301043}
;;     {:time 10, :item "broom", :quantity 23.86525259636632}
;;     {:time 11, :item "broom", :quantity 24.676874454922782}
;;     {:time 12, :item "broom", :quantity 29.45580618665162}
;;     {:time 13, :item "broom", :quantity 32.20529752834578}
;;     {:time 14, :item "broom", :quantity 30.928050775697603}
;;     {:time 15, :item "broom", :quantity 34.62634547570625}
;;     {:time 16, :item "broom", :quantity 33.30212829607493}
;;     {:time 17, :item "broom", :quantity 34.95707936800063}
;;     {:time 18, :item "broom", :quantity 38.59266228400805}
;;     {:time 19, :item "broom", :quantity 38.210162462449645})
  (do    
    (def line-plot (example-line-plot play-data))
    (oz/v! line-plot))


  )

(def raw-csv
  (with-open [reader (io/reader "data/Activities_2020-07-08_1-orig.csv")]
    (doall
     (csv/read-csv reader))))

(defn str->long [str-in]
  (when-not (= "--" str-in)
    (-> str-in
        (str/replace "," "")
        Long/parseLong)))

(defn yes-no->boolean [yn-str]
  (case yn-str
    "Yes" true
    "No" false
    (throw (ex-info "invalid value for yes-no"
                    {:value yn-str
                     :type (type yn-str)}))))

(defn parse-duration [cdd]
  (let [splitted (reverse (str/split cdd #":"))
        ;; put it in reverse order (seconds, minutes, hours) so that it's
        ;; always in that order
        count-splitted (count splitted)
        _ (when (or (< count-splitted 2)
                    (> count-splitted 3))
            (throw (ex-info "should have 2 to 3 values"
                            {:colon-delimited-duration cdd})))
        #_ (pp/pprint {:splitted splitted})
        seconds (nth splitted 0)
        minutes (nth splitted 1)
        hours (nth splitted 2 "0")
        #_ (pp/pprint {:seconds seconds
                      :minutes minutes
                      :hours hours})
        parsable (str "PT" hours "H" minutes "M" seconds "S")]
    (try
      (java.time.Duration/parse parsable)
      (catch java.time.format.DateTimeParseException dtpe
        (throw (ex-info (.getMessage dtpe)
                        {:colon-delimited-duration cdd
                         :parsable parsable}))))))

(defn raw-csv-activity->activity-hashmap [raw-csv-activity]
  (let [[type
         time-start
         favorite?
         title
         distance-in-miles
         calories
         duration
         heartrate-avg
         heartrate-max
         run-cadence-avg
         run-cadence-max
         pace-avg
         pace-best
         elev-gain
         elev-loss
         stride-length-avg
         vertical-ratio-avg
         vertical-oscillation-avg
         training-stress-score
         grit
         flow
         time-bottom
         temp-min
         surface-interval
         decompression?
         lap-time-best
         num-laps
         temp-max] raw-csv-activity
        type-kw (-> type
                    str/lower-case
                    (str/replace " " "-")
                    keyword)]
    {:type type-kw
     :time-start (-> time-start
                     (str/replace " " "T")
                     #_tick/clock
                     java.time.LocalDateTime/parse)
     :favorite? (case favorite?
                  "true" true
                  "false" false
                  false)
     :title title
     :distance-in-miles (Double/parseDouble distance-in-miles)
     :calories (str->long calories)
     :duration (parse-duration duration)
     :heartrate-avg (str->long heartrate-avg)
     :heartrate-max (str->long heartrate-max)
     :run-cadence-avg (str->long run-cadence-avg)
     :run-cadence-max (str->long run-cadence-max)
     :pace-avg (case type-kw
                 :cardio (Double/parseDouble pace-avg)
                 (parse-duration pace-avg))
     :pace-best (case type-kw
                  :cardio (Double/parseDouble pace-best)
                  (parse-duration pace-best))
     :elev-gain (str->long elev-gain)
     :elev-loss (str->long elev-loss)
     :stride-length-avg (Double/parseDouble stride-length-avg)
     :vertical-ratio-avg (Double/parseDouble vertical-ratio-avg)
     :vertical-oscillation-avg (Double/parseDouble vertical-oscillation-avg)
     :training-stress-score (Double/parseDouble training-stress-score)
     :grit (Double/parseDouble grit)
     :flow (Double/parseDouble flow)
     :time-bottom (parse-duration time-bottom)
     :temp-min (Double/parseDouble temp-min)
     :surface-interval (parse-duration surface-interval)
     :decompression? (yes-no->boolean decompression?)
     :lap-time-best (parse-duration lap-time-best)
     :num-laps (str->long num-laps)
     :temp-max (Double/parseDouble temp-max)}))

(defn activities-csv->list-of-maps [raw-activities-csv]
  (->> (rest raw-activities-csv)
       (map raw-csv-activity->activity-hashmap)))

(comment

  (do
    (def activities (activities-csv->list-of-maps raw-csv))
    activities)

 )

(defmulti year-month :type)

(defmethod year-month :default [activity]
  (let [time-start (:time-start activity)
        year (.getYear time-start)
        month-num (.. time-start getMonth getValue)]
    #_[year (.getValue month)]
    (str year "-" month-num)))

(comment

  (map year-month activities)

  (group-by year-month activities)

  )

(defn activities->counts-by-year-month [activities]
  (let [grouped (group-by year-month activities)]
    (map (fn [[k v]]
           [k (count v)]
           {:year-month k
            :activity-count (count v)
            :item "any-activity-type"})
         grouped)))

(comment

  (do
    (def counts-by-year-month (activities->counts-by-year-month activities))
    counts-by-year-month)

  )

(defn activities-line-plot [activities]
  {:data {:values (activities->counts-by-year-month activities)}
   :encoding {:x {:field "year-month" :type "temporal"}
              :y {:field "activity-count"}
              :color {:field "item" :type "nominal"}}
   :mark "line"
   :width 1200})

(comment

  (do
    (def line-plot (activities-line-plot activities))
    (oz/v! line-plot))

  )

(defn activities->counts-by-year-month-type [activities]
  (let [grouped (group-by #(hash-map :year-month (year-month %)
                                     :type (:type %))
                          activities)
        #_ (pp/pprint grouped)]
    (map (fn [[k v]]
           (let []
            {:year-month (:year-month k)
             :activity-count (count v)
             :type (:type k)}))
         grouped)))

(comment

  (do
    (def counts-by-year-month-type (activities->counts-by-year-month-type activities))
    counts-by-year-month-type)

  )

(defn activities-by-type-bar-plot [activities]
  (let [counts-by-year-month-type (activities->counts-by-year-month-type activities)
        ;; counts (map :activity-count counts-by-year-month-type)
        ;; max-count (first (sort > counts))
        ;; y-axis-top (* max-count 1.15385)
        ]
   {:data {:values counts-by-year-month-type}
    :encoding {:x {:field "year-month" :type "temporal"}
               :y {:field "activity-count"
                   #_:scale #_{:domain [0 y-axis-top]}}
               :color {:field "type" :type "nominal"}}
    :mark "bar"
    :width 1200
    :height 700}))

(comment

  (do
    (def bar-plot (activities-by-type-bar-plot activities))
    (oz/v! bar-plot))

  )
