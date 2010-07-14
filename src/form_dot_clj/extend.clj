(ns form-dot-clj.extend)

(defmulti show-html
  "Generates the HTML required for a control"
  (fn [control params] (control :Control)))

