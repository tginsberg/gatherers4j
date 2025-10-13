---
title: "movingMinBy()"
linkTitle: "movingMinBy()"
show_in_table: true
category: "Mathematical Operations"
description: Create a Stream that represents the moving minimum of a `Stream<T>` looking back `windowSize` number of elements, as calculated by the given `Comparator`

---

### Implementation Notes
This implementation is suitable for comparing an arbitrary `Stream<T>` with a `Comparator`; for a version that operates directly on a `Stream<Comparable<T>>`, see [`movingMin()`](/gatherers4j/gatherers/mathematical/movingmin/).
Nulls are ignored and play no part in calculations.

**Signatures**

`movingMinBy(int windowSize, Comparator<INPUT> comparator)`
* `windowSize` - How many trailing elements to calculate the minimum value from at any given point in the stream
* `comparator` - A non-null `Comparator<INPUT>` to use for comparing elements

**Additional Methods**

| Method                   | Purpose                                                                                                                                                                                                                         |
|--------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `excludePartialValues()` | When calculating the moving minimum values, and the full size of the window has not yet been reached, the gatherer should suppress emitting values until the lookback window is full. [See example.](#excluding-partial-values) |
| `withOriginal()`         | Emit both the original stream element and its calculated moving minimum value wrapped in a `WithOriginal` object. [See example.](#emit-original-value-and-calculated-value)                                                     |

### Examples

#### Moving minimum value of window size 3, mapped from an object

```java
Stream
    .of("3", "2", "1", "3", "4")
    .gather(Gatherers4j.movingMinBy(3, comparing(Integer::valueOf)))
    .toList();

// [ "3", "2", "1", "1", "1" ]
```

#### Excluding partial values

Showing that in-process moving minimum values are not emitted for each element until the lookback window has been filled.

Note that due to type erasure, a type witness may be required in this case.

```java
Stream
    .of("3", "2", "1", "3", "4")
    .gather(Gatherers4j.<String>movingMinBy(3, comparing(Integer::valueOf)).excludePartialValues())
    .toList();

// [ "1", "1", "1" ]
```


#### Showing nulls are ignored by default

```java
Stream
    .of(null, "3", null, "2", "1", "3", "4")
    .gather(Gatherers4j.movingMinBy(3, comparing(Integer::valueOf)))
    .toList();

// [ "3", "2", "1", "1", "1" ]
```

#### Emit original value and calculated value

Note that this call may need a type witness due to generic type erasure in Java.

```java
Stream
    .of("3", "2", "1", "3", "4")
    .gather(Gatherers4j.<String>movingMinBy(3, comparing(Integer::valueOf)).withOriginal())
    .toList();

// [
//   WithOriginal[original=3, calculated=3], 
//   WithOriginal[original=2, calculated=2], 
//   WithOriginal[original=1, calculated=1], 
//   WithOriginal[original=3, calculated=1], 
//   WithOriginal[original=4, calculated=1]
// ]

```