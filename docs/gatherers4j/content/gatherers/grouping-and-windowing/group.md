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

#### Group adjacent equal elements

```java
Stream
    .of("A", "A", "BB", "BB", "CCC", "A");
    .gather(Gatherers4j.group())
    .toList();

// [ ["A", "A"], ["BB", "BB"], ["CCC"], ["A"] ]
```
