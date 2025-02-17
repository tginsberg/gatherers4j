---
title: "filterNonIncreasing(comparator)"
linkTitle: "filterNonIncreasing(comparator)"
show_in_table: true
category: Filtering and Selection
description: Filter the input stream so that it contains elements in a non-increasing order as measured by the given `Comparator`
---

### Implementation Notes

This is suitable for streams whose elements do not implement `Comparable`. For a version that uses the natural order of elements that implement `Comparable`, see [`filterNonIncreasing()`](/gatherers/filtering-and-selection/filternonincreasing/).

**Signature**

`filterNonIncreasing(comparator)`

* `comparator` - A non-null `Comparator` to compare stream elements

### Examples

#### Filter the stream such that it is non-increasing, according to the given `Comparator`

```java
Stream
    .of("AAA", "AA", "AA", "A", "AA");
    .gather(Gatherers4j.filterNonIncreasing(Comparator.comparingInt(String::length)))
    .toList();

// ["AAA", "AA", "AA", "A"]
```
