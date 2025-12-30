---
title: "groupOrderedBy()"
linkTitle: "groupOrderedBy()"
show_in_table: true
category: Grouping and Windowing
description: Convert the input stream of objects into lists of ordered objects, as measured by the given `Comparator`.
---

### Implementation Notes

This is suitable for streams whose elements do not implement `Comparable`. For a version that uses the natural order of elements that implement `Comparable`, see [`groupOrdered()`](/gatherers4j/gatherers/grouping-and-windowing/groupordered/).
The lists emitted from this Gatherer are unmodifiable.

**Signature**

`groupOrderedBy(Order order, Comparator<INPUT> comparator)`
* `order` - A non-null Order in which to group elements. Values are `Equal`, `Ascending`, `Descending`, `AscendingOrEqual`, and `DescendingOrEqual`
* `comparator` - A non-null `Comparator` to compare stream elements

### Examples

#### Group elements of the stream into lists that are strictly descending, according to the given `Comparator`

```java
Stream
    .of("AAA", "AA", "A", "AA", "AA", "A")
    .gather(Gatherers4j
        .groupOrderedBy(
            Order.Descending, 
            Comparator.comparingInt(String::length)
        )
    )
    .toList();

// [ ["AAA", "AA", "A"], ["AA"], ["AA", "A"] ]
```

#### Group elements of the stream into lists that are strictly ascending, according to the given `Comparator`

```java
Stream
    .of("A", "AA", "AAA", "A", "AA", "AA")
    .gather(Gatherers4j
        .groupOrderedBy(
            Order.Ascending, 
            Comparator.comparingInt(String::length)
        )
    )
    .toList();

// [ ["A", "AA", "AAA"], ["A", "AA"] ["AA"] ]
```
