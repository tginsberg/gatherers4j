---
title: "withIndex()"
linkTitle: "withIndex()"
show_in_table: true
category: Sequence Operations
description: Maps all elements of the stream as-is along with their 0-based index.

---

### Implementation Notes

Each element of the input stream is mapped to a [`WithIndex`](https://github.com/tginsberg/gatherers4j/blob/main/src/main/java/com/ginsberg/gatherers4j/WithIndex.java)record along with its zero-based index. As of now
does not provide a `mapWithIndex()` functionality but that is on the roadmap.

**Signature**

`withIndex()`

### Examples

#### Get each element of a stream with its index

```java
Stream
    .of("A", "B", "C")
    .gather(Gatherers4j.withIndex())
    .toList();

// [ WithIndex(0, "A"), WithIndex(1, "B"), WithIndex(2, "C") ]
```

