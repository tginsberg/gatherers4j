---
title: "orderByFrequencyAscending()"
linkTitle: "orderByFrequencyAscending()"
show_in_table: true
category: Sequence Operations
description: Emit elements in the input stream ordered by frequency from least frequently occurring to most frequently occurring.

---

### Implementation Notes

This implementation reads the entire stream before emitting any results making it inappropriate for infinite streams. Since this Gatherer
is not order dependent it has a parallel-capable implementation. All results are wrapped in 
a [`WithCount`](https://github.com/tginsberg/gatherers4j/blob/main/src/main/java/com/ginsberg/gatherers4j/WithCount.java)record.
Object equality is measured with `Object::equals()`. See [`orderByFrequencyDescending()`](/gatherers/sequence-operations/orderbyfrequencydescending/)for a 
version of this Gatherer that sorts in the opposite direction. 

**Signature**

`orderByFrequencyAscending()`


### Examples

#### Order elements by frequency, ascending

```java
Stream
    .of("A", "A", "A", "B", "B", "B", "B", "C", "C");
    .gather(Gatherers4j.orderByFrequencyAscending())
    .toList();
    
// [ WithCount("C", 2), WithCount("A", 3), WithCount("B", 4) ]
```

