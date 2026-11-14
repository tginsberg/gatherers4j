---
title: "takeWhileIndexed()"
linkTitle: "takeWhileIndexed()"
show_in_table: true
category: Filtering and Selection
description: Take elements while the `predicate`, which takes both the element and its zero-based index, holds.
---

### Implementation Notes

This is an index-aware version of {{< jdklink linkName="Stream::takeWhile()" package="java.base/java/util/stream/Stream.html#takeWhile(java.util.function.Predicate)" >}}
from the JDK. It stops reading the input stream at the first element that fails the `predicate`, and does not emit that element.
For an inclusive version without the index, see [`takeUntil()`](/gatherers4j/gatherers/filtering-and-selection/takeuntil/).

**Signature**

`takeWhileIndexed(BiPredicate<? super Long, ? super INPUT> predicate)`

* `predicate` - A non-null `BiPredicate<Long,INPUT>` where the `Long` is the zero-based index of the element being tested, and the `INPUT` is the element itself.

### Examples

#### Take elements while each one equals its index

```java
Stream
    .of(0, 1, 2, 4, 5)
    .gather(Gatherers4j.takeWhileIndexed((index, element) -> index == element.longValue()))
    .toList();

// [0, 1, 2]
```
