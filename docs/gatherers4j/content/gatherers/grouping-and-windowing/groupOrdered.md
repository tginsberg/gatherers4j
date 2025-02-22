---
title: "groupOrdered()"
linkTitle: "groupOrdered()"
show_in_table: true
category: Grouping and Windowing
description: Convert the input stream of `Comparable` objects into lists of ordered objects.

---

### Implementation Notes

This is suitable for streams whose elements implement `Comparable`. For a version that takes a `Comparator`, see [`groupOrderedBy()`](/gatherers/grouping-and-windowing/grouporderedby/).
The lists emitted from this Gatherer are unmodifiable. 

**Signature**

`groupOrdered(Order order)`
* `order` - A non-null Order in which to group elements. Values are `Equal`, `Ascending`, `Descending`, `AscendingOrEqual`, and `DescendingOrEqual`

### Examples

#### Group stream elements into strictly descending lists

```java
Stream
    .of(3, 2, 1, 2, 2, 1);
    .gather(Gatherers4j.groupOrdered(Order.Descending))
    .toList();

// [ [3, 2, 1], [2], [2, 1] ]
```

#### Group stream elements into strictly ascending lists

```java
Stream
    .of(1, 2, 3, 3, 1, 2);
    .gather(Gatherers4j.groupOrdered(Order.Ascending))
    .toList();

// [ [1, 2, 3], [3], [1, 2] ]
```

#### Group stream elements into descending or equal lists

```java
Stream
    .of(3, 2, 1, 2, 2, 1);
    .gather(Gatherers4j.groupOrdered(Order.DescendingOrEqual))
    .toList();

// [ [3, 2, 1], [2, 2, 1] ]
```

#### Group stream elements into ascending or equal lists

```java
Stream
    .of(1, 2, 3, 3, 1, 2);
    .gather(Gatherers4j.groupOrdered(Order.AscendingOrEqual))
    .toList();

// [ [1, 2, 3, 3], [1, 2] ]
```
