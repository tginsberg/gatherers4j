---
title: "takeLastWhile()"
linkTitle: "takeLastWhile()"
show_in_table: true
category: Filtering and Selection
description: Keep only the run of elements at the end of the stream that match the `predicate`.
infinite_streams: emits-at-end
parallel: true
---

### Implementation Notes

This is the suffix version of {{< jdklink linkName="Stream::takeWhile()" package="java.base/java/util/stream/Stream.html#takeWhile(java.util.function.Predicate)" >}}
from the JDK: it keeps the longest run of matching elements that ends the stream, in their original order. If the last
element does not match, nothing is emitted. For a version that drops this run instead, see
[`dropLastWhile()`](/gatherers4j/gatherers/filtering-and-selection/droplastwhile/).

Only the current run of matching elements is held in memory. A non-matching element discards it. Nothing is emitted
until the input stream ends.

**Signature**

`takeLastWhile(Predicate<? super INPUT> predicate)`

* `predicate` - A non-null predicate, which should return `true` for elements that belong to the run at the end of the stream

### Examples

#### Keep the trailing elements greater than 4

```java
Stream
    .of(1, 5, 2, 8, 9)
    .gather(Gatherers4j.takeLastWhile(it -> it > 4))
    .toList();

// [8, 9]
```

#### Keep the last paragraph of a list of lines

```java
Stream
    .of("First paragraph", "", "Second paragraph", "continues here")
    .gather(Gatherers4j.takeLastWhile(it -> !it.isEmpty()))
    .toList();

// ["Second paragraph", "continues here"]
```
