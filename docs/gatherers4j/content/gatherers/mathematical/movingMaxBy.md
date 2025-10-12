---
title: "movingMaxBy()"
linkTitle: "movingMaxBy()"
show_in_table: true
category: "Mathematical Operations"
description: Create a Stream that represents the moving maximum of a `Stream<T>` looking back `windowSize` number of elements, as calculated by the given `Comparator`

---

### Implementation Notes
This implementation is suitable comparing an arbitrary `Stream<T>` with a `Comparator`; for a version that operates directly on a `Stream<Comparable<T>>`, see [`movingMax()`](/gatherers4j/gatherers/mathematical/movingmax/).
Nulls are ignored and play no part in calculations.

**Signatures**

`movingMaxBy(int windowSize, Comparator<INPUT> comparator)`
* `windowSize` - How many trailing elements to calculate the maximum value from at any given point in the stream
* `comparator` - A non-null `Comparator<INPUT>` to use for comparing elements

**Additional Methods**

| Method                                     | Purpose                                                                                                                                                                                                                                                                                                             |
|--------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `excludePartialValues()`                   | When calculating the moving maximum values, and the full size of the window has not yet been reached, the gatherer should supress emitting values until the lookback window is full. [See example.](#excluding-partial-values) |

### Examples

#### Moving maximum value of window size 3, mapped from an object

```java
Stream
    .of("3", "2", "1", "3", "4")
    .gather(Gatherers4j.movingMaxBy(3, comparing(Integer::valueOf)))
    .toList();

// [ "3", "3", "3", "3", "4" ]
```

#### Excluding partial values

Showing that in-process moving maximum values are not emitted for each element until the lookback window has been filled.

Note that due to type erasure, a type witness may be required in this case.

```java
Stream
    .of("3", "2", "1", "3", "4")
    .gather(Gatherers4j.<String>movingMaxBy(3, comparing(Integer::valueOf)).excludePartialValues())
    .toList();

// [ "3", "3", "4" ]
```


#### Showing nulls are ignored by default

```java
Stream
    .of(null, "3", null, "2", "1", "3", "4")
    .gather(Gatherers4j.movingMaxBy(3, comparing(Integer::valueOf)))
    .toList();

// [ "3", "3", "3", "3", "4" ]
```
