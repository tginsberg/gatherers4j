---
title: "orderByFrequency()"
linkTitle: "orderByFrequency()"
show_in_table: true
category: Sequence Operations
description: Emit elements in the input stream ordered by the specified frequency.

---

### Implementation Notes

This implementation reads the entire stream before emitting any results, making it inappropriate for infinite streams. Since this Gatherer
is not order-dependent, it has a parallel-capable implementation. All results are wrapped in a [`WithCount`](https://github.com/tginsberg/gatherers4j/blob/main/src/main/java/com/ginsberg/gatherers4j/dto/WithCount.java) record.

**Signature**

`orderByFrequency(Frequency order)`
* `order` - Either `Ascending` or `Descending`


### Examples

#### Order elements by frequency, ascending

```java
Stream
    .of("A", "A", "A", "B", "B", "B", "B", "C", "C")
    .gather(Gatherers4j.orderByFrequency(Frequency.Ascending))
    .toList();
    
// [ WithCount("C", 2), WithCount("A", 3), WithCount("B", 4) ]
```

#### Order elements by frequency, descending

```java
Stream
    .of("A", "A", "A", "B", "B", "B", "B", "C", "C")
    .gather(Gatherers4j.orderByFrequency(Frequency.Descending))
    .toList();
    
// [ WithCount("B", 4), WithCount("A", 3), WithCount("C", 2) ]
```

