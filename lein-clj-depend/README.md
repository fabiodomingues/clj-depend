[![Clojars Project](http://clojars.org/com.fabiodomingues/lein-clj-depend/latest-version.svg)](http://clojars.org/com.fabiodomingues/lein-clj-depend)

# lein-clj-depend

A Leiningen plugin to run [clj-depend](https://github.com/fabiodomingues/clj-depend).

## Installation

Add the plugin to your `project.clj`:

```clojure
:plugins [[com.fabiodomingues/lein-clj-depend "0.10.0"]]
```

## Usage

```
$ lein clj-depend
```

## Exit codes

- 0: no violations were found
- 1: one or more violations were found
- 2: error during analysis
