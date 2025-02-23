---
title: "ensureOrdered()"
linkTitle: "ensureOrdered()"
show_in_table: true
category: Validation and Constraints
description: Ensure that the `Comparable` elements in the input stream are in the order specified, and fail exceptionally if they are not.
---

### Implementation Notes
This is suitable for streams whose elements implement `Comparable`. For a version that takes a `Comparator`, see [`ensureOrderedBy()`](/gatherers4j/gatherers/validation-and-constraints/ensureorderedby/).
This gatherer is all-or-nothing, meaning it will not emit any intermediate results to its downstream in the event that it detects a failure case.

**Signature**

`ensureOrdered(Order order)`
* `order` - A non-null Order which elements must be in. Values are `Equal`, `Ascending`, `Descending`, `AscendingOrEqual`, and `DescendingOrEqual`

### Examples

#### Ensure that all elements are descending - success path

```java
Stream
    .of(3, 2, 1)
    .gather(Gatherers4j.ensureOrdred(Order.Descending))
    .toList();

// [3, 2, 1]
```

#### Ensure that all elements are descending - failure path

```java
Stream
    .of(3, 2, 2)
    .gather(Gatherers4j.ensureOrdred(Order.Descending))
    .toList();

// java.lang.IllegalStateException: ensureDecreasing detected non-decreasing element
```
