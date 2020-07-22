# bxb

bidirectional transformation & smart diff/patch library


# Motivation

1. Smart diff/patch library 

diff with schema - patch without schema (incorporate CRDT ideas)

2. Declarative bidirectional mapping with diff support:

```
target = xget(schema, mapping, src)

new-src = xput(mapping, new-target, old-src)
diff = xdiff(mapping, new-target, old-src)
```
