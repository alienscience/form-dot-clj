
(ns form-dot-clj.js-dot-clj
  "Playing with javascript generation"
  (:use clojure.walk))

(comment
  [:.ready :$document
   [:function []
    [:.validate "$(#myform)"]]])

;; States
(declare start first-arg rest-args
         method-call function-def
         block block-start finish)

(defn- lazy-fsm
  ([todo]
     (lazy-fsm start {:nesting 1} todo))
  ([state-fn state todo]
     (lazy-seq
       (state-fn state todo))))

(defn- convert-keyword [kw]
  (let [word (name kw)
        one (first word)
        two (.substring word 0 2)]
    (cond
      (= "$:" two)     (str "$(\":" (.substring word 2) "\")")
      (= "$#" two)     (str "$(\"#" (.substring word 2) "\")")
      (= \$ one)       (str "$(" (.substring word 1) ")")
      :else            word)))

(defn- convert [form state]
  (cond
    (keyword? form)       (convert-keyword form)
    (vector? form)        (lazy-fsm start
                                    (update-in state [:nesting] inc)
                                    form)
    :else form))

(defn- start [state todo]
  (if (empty? todo)
    nil
    (let [item (convert (first todo) state)]
      (cond
       (= \. (first item)) (lazy-fsm method-call
                                     (assoc state :method item)
                                     (rest todo))
       (= "function" item) (cons item
                                 (lazy-fsm function-def
                                           state
                                           (rest todo)))
       :else               (cons item
                                 (lazy-fsm first-arg
                                           state
                                           (rest todo)))))))

(defn- first-arg [state todo]
  (if (empty? todo)
    (cons "()" (lazy-fsm finish state nil))
    (let [item (convert (first todo) state)]
      (cons "(" (cons item (lazy-fsm rest-args state (rest todo)))))))

(defn- rest-args [state todo]
  (if (empty? todo)
    (cons ")" (lazy-fsm finish state nil))
    (let [item (convert (first todo) state)]
      (cons "," (cons item (lazy-fsm rest-args state (rest todo)))))))

(defn- method-call [state todo]
  (if (empty? todo)
    "<error>"
    (let [item (convert (first todo) state)
          method (state :method)]
      (cons item
            (cons method
                  (lazy-fsm first-arg state (rest todo)))))))

(defn- function-def [state todo]
  (if (empty? todo)
    "<error>"
    (let [args (first todo)
          arg-seq (lazy-fsm first-arg
                            (update-in state [:nesting] inc)
                            args)]
      (cons arg-seq
            (lazy-fsm block-start state (rest todo))))))

(defn- block-start [state todo]
  (if (empty? todo)
     "{}"
     (let [block-state (assoc state :nesting 0)
           item (convert (first todo) block-state)]
       (cons "{"
             (cons item
                   (lazy-fsm block block-state (rest todo)))))))

(defn- block [state todo]
  (if (empty? todo)
    "}"
    (let [item (convert (first todo) state)]
      (cons item
            (lazy-fsm block state (rest todo))))))

(defn- finish [state todo]
  (let [nesting (state :nesting)]
    (if (<= nesting 1)
      ";")))


;;======== Flatten strings ============

(defn- cat-strings [acc b]
  (if (and (seq? b) (not (list? b)))
    (reduce cat-strings acc b)
    (let [a (peek acc)]
      (if (and (string? a)
               (or (char? b) (string? b)))
        (conj (pop acc) (str a b))
        (conj acc b)))))

(defn- optimise-strings [js-seq]
  (reduce cat-strings [] js-seq))

(defn- js-sequence [form]
  (optimise-strings (lazy-fsm form)))

(defmacro js [form]
  (let [j (js-sequence form)]
    `(str ~@j)))

(defn id [value]
  (str "$(\"#" value "\")"))
