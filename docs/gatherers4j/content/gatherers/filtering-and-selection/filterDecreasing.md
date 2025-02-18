---
title: "filterDecreasing()"
linkTitle: "filterDecreasing()"
show_in_table: true
category: Filtering and Selection
description: Filter the input stream so that it contains `Comparable<INPUT>` elements in a strictly decreasing order.

---

### Implementation Notes

This is suitable for streams whose elements implement `Comparable`. For a version that takes a `Comparator`, see [`filterDecreasingBy()`](/gatherers/filtering-and-selection/filterdecreasingby/).

**Signature**

`filterDecreasing()`

### Examples

#### Filter the stream such that it is strictly decreasing

```java
Stream
    .of(3, 2, 2, 3, 1)
    .gather(Gatherers4j.filterDecreasing())
    .toList();

// [3, 2, 1]
```
