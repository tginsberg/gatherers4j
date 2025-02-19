---
title: "orderByFrequencyDescending()"
linkTitle: "orderByFrequencyDescending()"
show_in_table: true
category: Sequence Operations
description: Emit elements in the input stream ordered by frequency from most frequently occurring to least frequently occurring.

---

### Implementation Notes

This implementation reads the entire stream before emitting any results making it inappropriate for infinite streams. Since this Gatherer
is not order dependent it has a parallel-capable implementation. All results are wrapped in a [`WithCount`](https://github.com/tginsberg/gatherers4j/blob/main/src/main/java/com/ginsberg/gatherers4j/WithCount.java)record.
Object equality is measured with `Object::equals()`. See [`orderByFrequencyAscending()`](/gatherers/sequence-operations/orderbyfrequencyascending/)for a
version of this Gatherer that sorts in the opposite direction.

**Signature**

`orderByFrequencyDescending()`


### Examples

#### Order elements by frequency, ascending

```java
Stream
    .of("A", "A", "A", "B", "B", "B", "B", "C", "C");
    .gather(Gatherers4j.orderByFrequencyDescending())
    .toList();

// [ WithCount("B", 4), WithCount("A", 3), WithCount("C", 2) ]
```

