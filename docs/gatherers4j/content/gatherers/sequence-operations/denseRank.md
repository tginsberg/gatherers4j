---
title: "denseRank()"
linkTitle: "denseRank()"
show_in_table: true
category: Sequence Operations
description: Emit each element of an already sorted stream along with its dense rank, with no gaps after ties.

---

### Implementation Notes

Each element of the input stream is mapped to a [`WithRank`](https://github.com/tginsberg/gatherers4j/blob/main/src/main/java/com/ginsberg/gatherers4j/dto/WithRank.java) record holding its one-based rank and the element itself. Two adjacent elements tie when `compareTo()` returns `0` for them.

Ranks start at 1 and have no gaps: after a tie, the next distinct element gets the next rank. For `10, 20, 20, 30` the ranks are `1, 2, 2, 3`. For a version that leaves a gap after a tie, see [`rank()`](/gatherers4j/gatherers/sequence-operations/rank/).

**The input stream must already be sorted**, in either direction. Only adjacent elements are compared, and sortedness is not checked, so input that is not sorted produces ranks that are not meaningful. For example, `10, 20, 10` is ranked `1, 2, 3`: the two `10`s are not adjacent, so they do not tie.

To fail on unsorted input instead, put [`ensureOrdered()`](/gatherers4j/gatherers/validation-and-constraints/ensureordered/) in front with `andThen()`. It holds the whole stream before emitting anything, so the combination does not work on infinite streams.

Only the previous element and two counters are kept, and each rank is emitted as soon as its element arrives, so this works on infinite streams.

A `null` element fails with a `NullPointerException` once it is compared with its predecessor, as `ensureOrdered()` does. Use `denseRankBy()` with `Comparator.nullsFirst()` or `Comparator.nullsLast()` to rank streams that contain `null`.

**Signature**

`denseRank()`

### Examples

#### Rank a sorted stream without gaps

```java
Stream
    .of(10, 20, 20, 30)
    .gather(Gatherers4j.denseRank())
    .toList();

// [ WithRank(1, 10), WithRank(2, 20), WithRank(2, 20), WithRank(3, 30) ]
```

#### Input that is not sorted is not detected

```java
Stream
    .of(10, 20, 10)
    .gather(Gatherers4j.denseRank())
    .toList();

// [ WithRank(1, 10), WithRank(2, 20), WithRank(3, 10) ]
```
