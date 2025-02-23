---
title: "filterOrderedBy()"
linkTitle: "filterOrderedBy()"
show_in_table: true
category: Filtering and Selection
description: Filter the input stream so that it contains elements the order specified as measured by the given `Comparator`.
---

### Implementation Notes

This is suitable for streams whose elements do not implement `Comparable`. For a version that uses the natural order of 
elements that implement `Comparable`, see [`filterOrdered()`](/gatherers4j/gatherers/filtering-and-selection/filterordered/).

**Signature**

`filterOrderedBy(Order order, Comparator<INPUT> comparator)`
* `order` - A non-null Order in which to filter elements. Values are `Equal`, `Ascending`, `Descending`, `AscendingOrEqual`, and `DescendingOrEqual`
* `comparator` - A non-null `Comparator` to compare stream elements

### Examples

#### Filter the stream such that it is strictly descending, according to the given `Comparator`

```java
Stream
    .of("AAA", "AA", "AA", "AAA", "A");
    .gather(Gatherers4j.filterOrderedBy(Order.Descending, Comparator.comparingInt(String::length)))
    .toList();

// ["AAA", "AA", "A"];
```
