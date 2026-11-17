---
title: "rank()"
linkTitle: "rank()"
show_in_table: true
category: Sequence Operations
description: Emit each element of an already sorted stream along with its rank, skipping ranks after ties.

---

### Implementation Notes

Each element of the input stream is mapped to a [`WithRank`](https://github.com/tginsberg/gatherers4j/blob/main/src/main/java/com/ginsberg/gatherers4j/dto/WithRank.java) record holding its one-based rank and the element itself. Two adjacent elements tie when `compareTo()` returns `0` for them.

Ranks start at 1, and after a tie the next rank skips by the size of the tie. For `10, 20, 20, 30` the ranks are `1, 2, 2, 4`: the two `20`s share rank 2, and `30` is the fourth element, so it gets rank 4. For a version that never skips a rank, see [`denseRank()`](/gatherers4j/gatherers/sequence-operations/denserank/).

**The input stream must already be sorted**, in either direction. Only adjacent elements are compared, and sortedness is not checked, so input that is not sorted produces ranks that are not meaningful. For example, `10, 20, 10` is ranked `1, 2, 3`: the two `10`s are not adjacent, so they do not tie.

To fail on unsorted input instead, put [`ensureOrdered()`](/gatherers4j/gatherers/validation-and-constraints/ensureordered/) in front with `andThen()`. It holds the whole stream before emitting anything, so the combination does not work on infinite streams.

Only the previous element and two counters are kept, and each rank is emitted as soon as its element arrives, so this works on infinite streams.

A `null` element fails with a `NullPointerException` once it is compared with its predecessor, as `ensureOrdered()` does. Use `rankBy()` with `Comparator.nullsFirst()` or `Comparator.nullsLast()` to rank streams that contain `null`.

**Signature**

`rank()`

### Examples

#### Rank a sorted stream

```java
Stream
    .of(10, 20, 20, 30)
    .gather(Gatherers4j.rank())
    .toList();

// [ WithRank(1, 10), WithRank(2, 20), WithRank(2, 20), WithRank(4, 30) ]
```

#### Rank an infinite stream

```java
Stream
    .iterate(1, i -> i + 1)
    .flatMap(i -> Stream.of(i, i))
    .gather(Gatherers4j.rank())
    .limit(6)
    .toList();

// [ WithRank(1, 1), WithRank(1, 1), WithRank(3, 2), WithRank(3, 2), WithRank(5, 3), WithRank(5, 3) ]
```

#### Fail on input that is not sorted

```java
Stream
    .of(10, 20, 10)
    .gather(
        Gatherers4j.<Integer>ensureOrdered(Order.AscendingOrEqual)
            .andThen(Gatherers4j.rank())
    )
    .toList();

// throws IllegalStateException: Elements not in proper order: AscendingOrEqual
```
