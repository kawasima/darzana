(ns darzana.oauth1)

(def secure-random (java.security.SecureRandom/getInstance "SHA1PRNG"))

(defn rand-str
  "Random string for OAuth requests."
  [length]
  (. (new BigInteger (int (* 5 length)) ^java.util.Random secure-random) toString 32))


