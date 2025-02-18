---
title: "filterNonIncreasing()"
linkTitle: "filterNonIncreasing()"
show_in_table: true
category: Filtering and Selection
description: Filter the input stream so that it contains `Comparable` elements in a non-increasing order.

---

### Implementation Notes

This is suitable for streams whose elements implement `Comparable`. For a version that takes a `Comparator`, see [`filterNonIncreasingBy()`](/gatherers/filtering-and-selection/filternonincreasingby/).

**Signature**

`filterNonIncreasing()`

### Examples

#### Filter the stream such that it is non-increasing

```java
Stream
    .of(3, 2, 2, 1, 3, 2);
    .gather(Gatherers4j.filterNonIncreasing())
    .toList();

// [3, 2, 2, 1];
```
