---
title: "zipWithNext()"
linkTitle: "zipWithNext()"
show_in_table: true
category: Sequence Operations
description: Creates a stream of `List` objects which contain each two adjacent elements in the input stream.

---

### Implementation Notes

This is syntactic sugar on [`window()`](/gatherers4j/gatherers/grouping-and-windowing/window/). By definition, each `List` is two elements long.
The lists returned from this Gatherer are unmodifiable.

**Signature**

`zipWithNext()`

### Examples

#### Return adjacent elements in `List<INPUT>`


```java
Stream
    .of("A", "B", "C", "D");
    .gather(Gatherers4j.zipWithNext())
    .toList();
    
// [ ["A", "B"], ["B", "C"], ["C", "D"] ]
```

