---
title: "filterIncreasing(comparator)"
linkTitle: "filterIncreasing(comparator)"
show_in_table: true
category: Filtering and Selection
description: Filter the input stream so that it contains elements in a strictly increasing order as measured by the given `Comparator`.
---

### Implementation Notes

This is suitable for streams whose elements do not implement `Comparable`. For a version that uses the natural order of elements that implement `Comparable`, see [`filterIncreasing()`](/gatherers/filtering-and-selection/filterincreasing/).

**Signature**

`filterIncreasing(comparator)`

* `comparator` - A non-null `Comparator` to compare stream elements

### Examples

#### Filter the stream such that it is strictly increasing, according to the given `Comparator`

```java
Stream
    .of("A", "AA", "AA", "A", "AAA");
    .gather(Gatherers4j.filterIncreasing(Comparator.comparingInt(String::length)))
    .toList();

// ["A", "AA", "AAA"];
```
