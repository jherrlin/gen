(ns server.specs
  (:require
   [clojure.spec.alpha :as s]
   [clojure.string :as str]
   [clojure.java.io :as io]
   [clojure.test.check.generators :as gen]
   [server.generators :as generators]))


(s/def ::non-blank-string (s/and string? (complement str/blank?)))
(s/def ::uuid (s/with-gen uuid? (fn [] gen/uuid)))
(s/def ::datetime inst?)
(s/def ::email
  (s/with-gen
    #(re-matches #".+@.+\..+" %)
    (fn [] generators/email)))
(s/def ::zip-code
  (s/with-gen
    (s/nilable string?)
    (fn [] generators/zip-code)))
(s/def ::luhn
  (s/with-gen
    (s/nilable (s/or :pon (s/and string? generators/luhn)
                     :s string?))
    (fn [] generators/luhn-gen)))

(s/def ::m  ;; generates a hashmap with random key/values
  (s/with-gen
    map?
    #(gen/fmap
      (fn [coll]
        (->> coll (flatten) (apply hash-map)))
      (s/gen (s/coll-of (s/cat :a keyword? :b (s/nilable string?)))))))

(def first-names
  (line-seq (clojure.java.io/reader (io/resource "first-names.txt"))))

(def last-names
  (line-seq (clojure.java.io/reader (io/resource "last-names.txt"))))

(def email-providers
  (line-seq (clojure.java.io/reader (io/resource "email-providers.txt"))))

(def swedish-cities
  (line-seq (clojure.java.io/reader (io/resource "swedish-cities.txt"))))

(def street-addresses
  (line-seq (clojure.java.io/reader (io/resource "swedish-addresses.txt"))))

(s/def ::first-name
  (s/with-gen
    ::non-blank-string
    #(gen/frequency [[1 (s/gen ::non-blank-string)]
                     [9 (gen/elements first-names)]])))

(s/def ::last-name
  (s/with-gen
    ::non-blank-string
    #(gen/frequency [[1 (s/gen ::non-blank-string)]
                     [9 (gen/elements last-names)]])))

(s/def ::city
  (s/with-gen
    ::non-blank-string
    #(gen/frequency [[1 (s/gen ::non-blank-string)]
                     [9 (gen/elements swedish-cities)]])))

(s/def ::person-nr ::luhn)

(s/def ::street-address
  (s/with-gen
    ::non-blank-string
    #(gen/frequency [[1 (s/gen ::non-blank-string)]
                     [9 (gen/elements street-addresses)]])))

(s/def ::street-number pos-int?)

(s/def ::address
  (s/keys :req-un [::zip-code
                   ::street-address
                   ::street-number
                   ::city]))

(s/def ::id ::uuid)

(s/def ::person
  (s/keys :req-un [::id
                   ::first-name
                   ::last-name
                   ::address
                   ::person-nr
                   ::email]))

(defn gen-person
  "Generate a single person."
  []
  (gen/generate (s/gen ::person)))

(s/def ::persons
  (s/coll-of ::person))

(defn gen-persons
  "Generate a collection of persons, random lenght."
  []
  (gen/generate (s/gen ::persons)))

(comment
  (gen/generate (s/gen ::m))
  (gen/generate (s/gen ::email))
  (gen/generate (s/gen ::luhn))

  )
