# ADR 1: Use tools.namespace

Date: 2022-03-21

Status: Accepted

## Context

To have the dependencies of each namespace in a project it is necessary to parse these declarations from source file, and for that we look at three options:

- Use `java.io.PushbackReader` combined with the [read](https://clojuredocs.org/clojure.core/read) function to read clojure file, find `ns` form and then parse its contents (how [ns-graph](https://github.com/alexander-yakushev/ns-graph) works). 
- Use [analysis data](https://cljdoc.org/d/clj-kondo/clj-kondo/2022.04.25/doc/analysis-data) provided by [clj-kondo](https://github.com/clj-kondo/clj-kondo) which serves just to enable writing tools and linters that are not yet in clj-kondo itself.
- Use [tools.namespace](https://github.com/clojure/tools.namespace) that was created and is maintained by the clojure team for the purpose of being a toolset for managing namespaces in Clojure.

## Decision

We will use [tools.namespace](https://github.com/clojure/tools.namespace) because it is a set of specific tools for our need.

## Consequences

- `tool.namespace` also has a dependency graph data structure that is useful for dependency analysis.
- We don't need to maintain our own parsing algorithm. 
- We don't force our users to use `clj-kondo` if they don't want to.
- Keep the design as simple and performant as possible.