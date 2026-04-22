---
title: "filterNotNull()"
linkTitle: "filterNotNull()"
show_in_table: true
category: Filtering and Selection
description: Remove null elements from a stream, narrowing the output type to non-null.
---

### Implementation Notes

This gatherer removes null elements from the stream. The input type is `@Nullable TYPE` and the output type is `TYPE`,
making it useful for bridging a nullable stream into a null-safe pipeline without an explicit cast.

**Signature**

`filterNotNull()`

### Examples

#### Remove nulls from a mixed stream

```java
Stream
    .of("A", null, "B", null, "C")
    .gather(Gatherers4j.filterNotNull())
    .toList();

// ["A", "B", "C"]
```

#### A stream of all nulls produces an empty list

```java
Stream
    .of(null, null, null)
    .gather(Gatherers4j.filterNotNull())
    .toList();

// []
```