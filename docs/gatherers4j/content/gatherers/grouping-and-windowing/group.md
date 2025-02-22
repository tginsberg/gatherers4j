---
title: "group()"
linkTitle: "group()"
show_in_table: true
category: Grouping and Windowing
description: Turn a `Stream<INPUT>` into a `Stream<List<INPUT>>` where adjacent equal elements are in the same `List` and equality is measured by `Object::equals()`.
---


### Implementation Notes

This function groups adjacent equal elements as measured by `Object::equals()` into lists. 
The lists returned from this Gatherer are unmodifiable.
For the purposes of this gatherer, nulls are equal to each other but not equal to anything else. 

For a version of `group()` that measures equality with a user-provided function, see the [`groupBy()`](/gatherers/grouping-and-windowing/groupby/)Gatherer.

**Signature**

`group()`

### Examples

#### Group consecutive equal elements according to a function

The function groups consecutive elements of equal length

```java
Stream
    .of("A", "B", "AA", "BB", "CCC", "AAA");
    .gather(Gatherers4j.groupBy(String::length))
    .toList();

// [ ["A", "B"], ["AA", "BB"], ["CCC", "AAA"] ]
```
