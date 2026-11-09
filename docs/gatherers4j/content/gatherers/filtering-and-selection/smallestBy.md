---
title: "smallestBy()"
linkTitle: "smallestBy()"
show_in_table: true
category: Filtering and Selection
description: Emit the `count` smallest elements of the stream, as measured by the given `Comparator`, smallest first.
infinite_streams: emits-at-end
parallel: true
---

### Implementation Notes

Holds at most `count` elements in a bounded heap and emits them once the input stream is fully exhausted, so it works on streams too large to sort. The result is the same as `sorted(comparator).limit(count)`: elements that compare as equal are emitted in encounter order, and when they tie for the last place the earliest is kept. If there are fewer than `count` elements, all of them are emitted. Null elements are passed to the `comparator`, so use {{< jdklink linkName="Comparator.nullsFirst()" package="java.base/java/util/Comparator.html#nullsFirst(java.util.Comparator)" >}} or {{< jdklink linkName="Comparator.nullsLast()" package="java.base/java/util/Comparator.html#nullsLast(java.util.Comparator)" >}} on a stream that has them. For the largest elements instead, see [`largestBy()`](/gatherers4j/gatherers/filtering-and-selection/largestby/).

A `count` of 0 emits nothing and stops reading the input stream immediately, so it works on infinite streams.

**Signature**

`smallestBy(int count, Comparator<? super INPUT> comparator)`

* `count` - The number of elements to emit, must not be negative
* `comparator` - A non-null `Comparator` that orders the elements

### Examples

#### Keep the two shortest strings

```java
Stream
    .of("BBB", "A", "DDDD", "CC")
    .gather(Gatherers4j.smallestBy(2, Comparator.comparingInt(String::length)))
    .toList();

// ["A", "CC"]
```

#### Ties keep their encounter order

```java
Stream
    .of("A", "BB", "C", "D")
    .gather(Gatherers4j.smallestBy(2, Comparator.comparingInt(String::length)))
    .toList();

// ["A", "C"]
```
