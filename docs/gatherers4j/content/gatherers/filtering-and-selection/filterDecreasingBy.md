---
title: "filterDecreasingBy()"
linkTitle: "filterDecreasingBy()"
show_in_table: true
category: Filtering and Selection
description: Filter the input stream so that it contains elements in a strictly decreasing order as measured by the given `Comparator`.
---

### Implementation Notes

This is suitable for streams whose elements do not implement `Comparable`. For a version that uses the natural order of elements that implement `Comparable`, see [`filterDecreasing()`](/gatherers/filtering-and-selection/filterdecreasing/).

**Signature**

`filterDecreasingBy()`

* `comparator` - A non-null `Comparator` to compare stream elements

### Examples

#### Filter the stream such that it is strictly decreasing, according to the given `Comparator`

```java
Stream
    .of("AAA", "AA", "AA", "AAA", "A");
    .gather(Gatherers4j.filterDecreasingBy(Comparator.comparingInt(String::length)))
    .toList();

// ["AAA", "AA", "A"];
```
