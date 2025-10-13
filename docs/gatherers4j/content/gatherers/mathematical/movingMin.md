---
title: "movingMin()"
linkTitle: "movingMin()"
show_in_table: true
category: "Mathematical Operations"
description: Create a Stream that represents the moving minimum of a `Stream<T>` looking back `windowSize` number of elements

---

### Implementation Notes
This implementation is suitable for streams whose elements implement `Comparable`, for a version that takes user-specified `Comparator` see [`movingMinBy()`](/gatherers4j/gatherers/mathematical/movingminby/).
Nulls are ignored and play no part in calculations.

**Signatures**

`movingMin(int windowSize)`
* `windowSize` - How many trailing elements to calculate the minimum value over

**Additional Methods**

| Method                   | Purpose                                                                                                                                                                                                                         |
|--------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `excludePartialValues()` | When calculating the moving minimum values, and the full size of the window has not yet been reached, the gatherer should suppress emitting values until the lookback window is full. [See example.](#excluding-partial-values) |
| `withOriginal()`         | Emit both the original stream element and its calculated moving minimum value wrapped in a `WithOriginal` object. [See example.](#emit-original-value-and-calculated-value)                                                     |

### Examples

#### Moving minimum over a window size 3

```java
Stream
    .of(3, 2, 1, 3, 4)
    .gather(Gatherers4j.movingMin(3))
    .toList();

// [ 3, 2, 1, 1, 1 ]
```


#### Excluding partial values

Showing that in-process moving minimum values are not emitted for each element until the lookback window has been filled.

```java
Stream
    .of(3, 2, 1, 3, 4)
    .gather(Gatherers4j.movingMin(3).excludePartialValues())
    .toList();

// [ 1, 1, 1 ]
```

#### Showing nulls are ignored by default

```java
Stream
    .of(null, 3, null, 2, 1, 3, 4)
    .gather(Gatherers4j.movingMin(3))
    .toList();

// [ 3, 2, 1, 1, 1 ]
```

#### Emit original value and calculated value

Note that this call may need a type witness due to generic type erasure in Java.

```java
Stream
    .of(3, 2, 1, 3, 4)
    .gather(Gatherers4j.<Integer>movingMin(3).withOriginal())
    .toList();

// [
//   WithOriginal[original=3, calculated=3], 
//   WithOriginal[original=2, calculated=2], 
//   WithOriginal[original=1, calculated=1], 
//   WithOriginal[original=3, calculated=1], 
//   WithOriginal[original=4, calculated=1]
// ]

```