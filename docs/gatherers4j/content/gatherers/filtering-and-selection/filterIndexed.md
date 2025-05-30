---
title: "filterIndexed()"
linkTitle: "filterIndexed()"
show_in_table: true
category: Filtering and Selection
description: Filter a stream according to a given `predicate`, which takes both the item being examined and its index.
---

### Implementation Notes

**Signature**

`filterIndexed(BiPredicate<Integer, INPUT> predicate)`

* `predicate` - A non-null `BiPredicate<Integer,INPUT>` where the `Integer` is the zero-based index of the element being filtered, and the `INPUT` is the element itself.

### Examples

#### Keep the even-numbered elements, and anything that equals "T"

```java
Stream
    .of("A", "B", "C", "T")
    .gather(Gatherers4j.filterIndexed((index, element) -> index % 2 == 0 || element.equals("T")))
    .toList();

// ["A", "C", "T"]
```
