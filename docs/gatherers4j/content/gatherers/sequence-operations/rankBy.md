---
title: "rankBy()"
linkTitle: "rankBy()"
show_in_table: true
category: Sequence Operations
description: Emit each element of an already sorted stream along with its rank, skipping ranks after ties.

---

### Implementation Notes

Each element of the input stream is mapped to a [`WithRank`](https://github.com/tginsberg/gatherers4j/blob/main/src/main/java/com/ginsberg/gatherers4j/dto/WithRank.java) record holding its one-based rank and the element itself. Two adjacent elements tie when the `comparator` returns `0` for them.

Ranks start at 1, and after a tie the next rank skips by the size of the tie. For `10, 20, 20, 30` the ranks are `1, 2, 2, 4`: the two `20`s share rank 2, and `30` is the fourth element, so it gets rank 4. For a version that never skips a rank, see [`denseRankBy()`](/gatherers4j/gatherers/sequence-operations/denserankby/).

**The input stream must already be sorted**, in either direction. Only adjacent elements are compared, and sortedness is not checked, so input that is not sorted produces ranks that are not meaningful. For example, `10, 20, 10` is ranked `1, 2, 3`: the two `10`s are not adjacent, so they do not tie.

To fail on unsorted input instead, put [`ensureOrderedBy()`](/gatherers4j/gatherers/validation-and-constraints/ensureorderedby/) in front with `andThen()`. It holds the whole stream before emitting anything, so the combination does not work on infinite streams.

Only the previous element and two counters are kept, and each rank is emitted as soon as its element arrives, so this works on infinite streams.

`null` elements are ranked like any other element, as long as the `comparator` can compare them, for example `Comparator.nullsFirst()`.

**Signature**

`rankBy(Comparator<? super INPUT> comparator)`

* `comparator` - A non-null comparator used to decide whether two adjacent elements tie. Descending input works as long as the `comparator` agrees with the order of the input, for example `Comparator.reverseOrder()`.

### Examples

#### Rank by the length of a string

```java
Stream
    .of("a", "bb", "cc", "ddd")
    .gather(Gatherers4j.rankBy(Comparator.comparingInt(String::length)))
    .toList();

// [ WithRank(1, "a"), WithRank(2, "bb"), WithRank(2, "cc"), WithRank(4, "ddd") ]
```

#### Rank a stream sorted in descending order

```java
Stream
    .of(90, 85, 85, 70)
    .gather(Gatherers4j.rankBy(Comparator.<Integer>reverseOrder()))
    .toList();

// [ WithRank(1, 90), WithRank(2, 85), WithRank(2, 85), WithRank(4, 70) ]
```
