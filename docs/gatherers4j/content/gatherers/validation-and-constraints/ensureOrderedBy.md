---
title: "ensureOrderedBy()"
linkTitle: "ensureOrderedBy()"
show_in_table: true
category: Validation and Constraints
description: Ensure that the elements in the input stream are in the order specified, as measured by the given `Comparator`, and fail exceptionally if they are not.
---

### Implementation Notes
This is suitable for streams whose elements do not implement `Comparable`. For a version that uses the natural order of elements that implement `Comparable`, see [`ensureOrdered()`](/gatherers4j/gatherers/validation-and-constraints/ensureordered/).
This gatherer is all-or-nothing, meaning it will not emit any intermediate results to its downstream in the event that it detects a failure case.

**Signature**

`ensureOrderedBy(Order order, Comparator<INPUT> coparator)`
* `order` -  A non-null Order which elements must be in. Values are `Equal`, `Ascending`, `Descending`, `AscendingOrEqual`, and `DescendingOrEqual`
* `comparator` - A non-null `Comparator` to compare stream elements

### Examples

#### Ensure that all elements are descending as mapped by the given function - success path

As measured by the _length_ of the input, not its lexicographical order.

```java
Stream
    .of("AAA", "BB", "C")
    .gather(Gatherers4j.ensureOrderedBy(Order.Descending, String::length))
    .toList();

// ["AAA", "BB", "C"]
```

#### Ensure that all elements are descending as mapped by the given function - failure path

As measured by the _length_ of the input, not its lexicographical order.

```java
Stream
    .of("AAA", "AA", "AA")
    .gather(Gatherers4j.ensureOrderedBy(Order.Descending, String::length))
    .toList();

// java.lang.IllegalStateException: ensureDecreasingBy detected non-decreasing element
```
