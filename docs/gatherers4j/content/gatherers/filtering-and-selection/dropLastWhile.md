---
title: "dropLastWhile()"
linkTitle: "dropLastWhile()"
show_in_table: true
category: Filtering and Selection
description: Drop the run of elements at the end of the stream that match the `predicate`, and keep everything before it.
infinite_streams: holds-matching-run
---

### Implementation Notes

This is the suffix version of {{< jdklink linkName="Stream::dropWhile()" package="java.base/java/util/stream/Stream.html#dropWhile(java.util.function.Predicate)" >}}
from the JDK: it drops the longest run of matching elements that ends the stream, and keeps everything before it in
its original order. Matching elements in the middle of the stream are kept. For a version that keeps only this run,
see [`takeLastWhile()`](/gatherers4j/gatherers/filtering-and-selection/takelastwhile/).

Elements are emitted as they arrive, except for a run of matching elements, which is held until a non-matching element
shows that the run is not at the end of the stream. The `predicate` is called once for each element, in encounter
order.

**Signature**

`dropLastWhile(Predicate<? super INPUT> predicate)`

* `predicate` - A non-null predicate, which should return `true` for elements that belong to the run at the end of the stream

### Examples

#### Drop the trailing elements greater than 4

```java
Stream
    .of(1, 5, 2, 8, 9)
    .gather(Gatherers4j.dropLastWhile(it -> it > 4))
    .toList();

// [1, 5, 2]
```

#### Remove trailing blank lines

```java
Stream
    .of("First", "", "Second", "", "")
    .gather(Gatherers4j.dropLastWhile(String::isBlank))
    .toList();

// ["First", "", "Second"]
```
