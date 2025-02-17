---
title: "filterNonDecreasing(comparator)"
linkTitle: "filterNonDecreasing(comparator)"
show_in_table: true
category: Filtering and Selection
description: Filter the input stream so that it contains elements in a non-decreasing order as measured by the given `Comparator`
---

### Implementation Notes

This is suitable for streams whose elements do not implement `Comparable`. For a version that uses the natural order of elements that implement `Comparable`, see [`filterNonDecreasing()`](/gatherers/filtering-and-selection/filternondecreasing/).

**Signature**

`filterNonDecreasing(comparator)`

* `comparator` - A non-null `Comparator` to compare stream elements

### Examples

#### Filter the stream such that it is non-decreasing, according to the given `Comparator`

```java
Stream
    .of("A", "AA", "AA", "AAA", "AA");
    .gather(Gatherers4j.filterNonDecreasing(Comparator.comparingInt(String::length)))
    .toList();

// ["A", "AA", "AA", "AAA"]
```
