(ns server.generators
  (:require
   [clojure.string :as str]
   [clojure.test.check.generators :as gen]))

(def email
  (->> (gen/tuple
        (gen/such-that #(not= "" %) gen/string-alphanumeric)
        (gen/such-that #(not= "" %) gen/string-alphanumeric)
        (gen/such-that #(not= "" %) gen/string-alphanumeric))
       (gen/fmap (fn [[a b c]] (str a "@" b "." c)))))

(def zip-code
  (->> (gen/tuple (gen/choose 100 999)
                  (gen/choose 10 99))
       (gen/fmap
        (fn [[x y]] (str x " " y)))))

(defn luhn
  "If x is a valid luhn string return it, else nil."
  [x]
  (let [parse-int #?(:clj  #(Character/digit % 10)
                     :cljs #(js/parseInt %))]
    (when
        (and (string? x)
             (some->> x
                      (re-find #"^\d{10}$")
                      (take 9)
                      (map #(parse-int %))
                      (map #(* %1 %2) (cycle [2 1]))
                      (map #(+ (quot % 10) (mod % 10)))
                      (reduce +)
                      (+ (parse-int (last x)))
                      (str)
                      (last)
                      (= \0)))
      x)))

(defn luhn-checksum
  "Returns the checksum char of a luhn string or nil."
  [x]
  (let [parse-int #?(:clj  #(Character/digit % 10)
                     :cljs #(js/parseInt %))]
    (when (string? x)
      (some->> x
               (re-find #"^\d*$")
               (seq)
               (map #(parse-int %))
               (reverse)
               (map #(* %1 %2) (cycle [2 1]))
               (map #(+ (quot % 10) (mod % 10)))
               (reduce +)
               (* 9)
               (str)
               (last)))))

(def luhn-gen
  (gen/bind (gen/vector (gen/choose 0 9) 9)
            #(let [seed (str/join %)]
               (gen/return (str seed (luhn-checksum seed))))))
