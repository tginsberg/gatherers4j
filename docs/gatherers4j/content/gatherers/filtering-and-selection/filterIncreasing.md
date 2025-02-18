---
title: "filterIncreasing()"
linkTitle: "filterIncreasing()"
show_in_table: true
category: Filtering and Selection
description: Filter the input stream so that it contains `Comparable<INPUT>` elements in a strictly increasing order.

---

### Implementation Notes

This is suitable for streams whose elements implement `Comparable`. For a version that takes a `Comparator`, see [`filterIncreasingBy()`](/gatherers/filtering-and-selection/filterincreasingby/).

**Signature**

`filterIncreasing()`

### Examples

#### Filter the stream such that it is strictly increasing


```java
Stream
    .of(1, 2, 2, 1, 3)
    .gather(Gatherers4j.filterIncreasing())
    .toList();

// [1, 2, 3]
```
