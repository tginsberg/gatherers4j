---
title: "filterOrdered()"
linkTitle: "filterOrdered()"
show_in_table: true
category: Filtering and Selection
description: Filter the input stream so that it contains `Comparable<INPUT>` elements in the order provided.

---

### Implementation Notes

This is suitable for streams whose elements implement `Comparable`. For a version that takes a `Comparator`, 
see [`filterOrderedBy()`](/gatherers4j/gatherers/filtering-and-selection/filterorderedby/).

**Signature**

`filterOrdered(Order order)`
* `order` - A non-null Order in which to filter elements. Values are `Equal`, `Ascending`, `Descending`, `AscendingOrEqual`, and `DescendingOrEqual`


### Examples

#### Filter the stream such that it is strictly descending

```java
Stream
    .of(3, 2, 2, 3, 1)
    .gather(Gatherers4j.filterOrdered(Order.Descending))
    .toList();

// [3, 2, 1]
```

#### Filter the stream such that it is strictly ascending

```java
Stream
    .of(1, 2, 2, 3, 3)
    .gather(Gatherers4j.filterOrdered(Order.Ascending))
    .toList();

// [1, 2, 3]
```

#### Filter the stream such that it is ascending or equal

```java
Stream
    .of(1, 2, 2, 3, 2)
    .gather(Gatherers4j.filterOrdered(Order.AscendingOrEqual))
    .toList();

// [1, 2, 2, 3]
```

#### Filter the stream such that it is decending or equal

```java
Stream
    .of(3, 2, 2, 1, 2)
    .gather(Gatherers4j.filterOrdered(Order.DescendingOrEqual))
    .toList();

// [3, 2, 2, 1]
```
