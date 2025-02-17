---
title: "filterNonDecreasing()"
linkTitle: "filterNonDecreasing()"
show_in_table: true
category: Filtering and Selection
description: Filter the input stream so that it contains `Comparable` elements in a non-decreasing order.

---

### Implementation Notes

This is suitable for streams whose elements implement `Comparable`. For a version that takes a `Comparator`, see [`filterNonDecreasing(comparator)`](/gatherers/filtering-and-selection/filternondecreasingcomparator/).

**Signature**

`filterNonDecreasing()`

### Examples

#### Filter the stream such that it is non-decreasing

```java
Stream
    .of(1, 2, 2, 3, 2);
    .gather(Gatherers4j.filterNonDecreasing())
    .toList();

// [1, 2, 2, 3];
```
