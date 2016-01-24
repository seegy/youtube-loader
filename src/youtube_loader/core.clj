(ns youtube-loader.core)

(def ytId "8fxqxweJE9I")

(def response (slurp (format "http://youtube.com/get_video_info?video_id=%s" ytId)))


(def xf (comp
 (map (fn[x] (clojure.string/split x #"=")))
 (map (fn[x] (if (not= (count x) 2) (conj x "") x)))))

(defn decode [s] (java.net.URLDecoder/decode s "UTF-8"))


(defn paramsToMap [paramString] (into {} xf (clojure.string/split paramString #"&")))

(def dataMap (paramsToMap response))

(sort (keys dataMap))

(def title (clojure.string/replace (clojure.string/replace (decode (get dataMap "title")) #" " "_")
                                   #"\||#|\.|:" ""))

(def streamsRaw (get dataMap "url_encoded_fmt_stream_map"))

(def streams (clojure.string/split streamsRaw #"%2C"))

(def decodedStreams (map #(decode %) streams))

(def decodedStreamURLs(map #(decode (get (paramsToMap %) "url")) decodedStreams))


(sort (keys (paramsToMap (second (clojure.string/split (first decodedStreamURLs) #"\?")))))
(get (paramsToMap (second (clojure.string/split (first decodedStreamURLs) #"\?"))) "itag")

(def baseURL (format "resources/%s.mp4" title))

(def buffer-size 1024)

(defn loadStreams
  [streamURLs]
  (clojure.java.io/delete-file baseURL true)
  (with-open [out (clojure.java.io/output-stream baseURL)]
    (let[stream-count (count streamURLs)]
     (time (loop [urls streamURLs]
        (if (empty? urls)
          (do
            (println "fertig"))
          (do
            (clojure.java.io/copy (clojure.java.io/input-stream (first urls)) out :buffer-size buffer-size)
            (println (format "%.2f Prozent geladen..." (double (* (/ (- stream-count (count (rest urls))) stream-count) 100))))
            (recur (rest urls)))))))))

;(loadStreams decodedStreamURLs)






