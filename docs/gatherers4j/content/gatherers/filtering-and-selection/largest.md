---
title: "largest()"
linkTitle: "largest()"
show_in_table: true
category: Filtering and Selection
description: Emit the `count` largest `Comparable` elements of the stream, largest first.
infinite_streams: emits-at-end
parallel: true
---

### Implementation Notes

Holds at most `count` elements in a bounded heap and emits them once the input stream is fully exhausted, so it works on streams too large to sort. The result is the same as `sorted(Comparator.reverseOrder()).limit(count)`: elements that compare as equal are emitted in encounter order, and when they tie for the last place the earliest is kept. If there are fewer than `count` elements, all of them are emitted. Null elements are ignored. For the smallest elements instead, see [`smallest()`](/gatherers4j/gatherers/filtering-and-selection/smallest/).

A `count` of 0 emits nothing and stops reading the input stream immediately, so it works on infinite streams.

**Signature**

`largest(int count)`

* `count` - The number of elements to emit, must not be negative

### Examples

#### Keep the three largest numbers

```java
Stream
    .of(3, 1, 4, 1, 5, 9, 2, 6)
    .gather(Gatherers4j.largest(3))
    .toList();

// [9, 6, 5]
```

#### Null elements are ignored

```java
Stream
    .of(3, null, 1, 4)
    .gather(Gatherers4j.largest(2))
    .toList();

// [4, 3]
```
