
(require 'dispatched.clojure.person)

; Note that deftypes must be explicitly imported (they are true JVM classes) 
(import 'dispatched.clojure.person.Person)

(def resources {
  "person" (Person.)})
