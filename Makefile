.PHONY: test test-clj

test: test-clj

test-clj:
	clojure -Atest-clj
