---
title: "movingMax()"
linkTitle: "movingMax()"
show_in_table: true
category: "Mathematical Operations"
description: Create a Stream that represents the moving maximum of a `Stream<T>` looking back `windowSize` elements

---

### Implementation Notes
This implementation is suitable for streams whose elements implement `Comparable`, for a version that takes user-specified `Comparator` see [`movingMaxBy()`](/gatherers4j/gatherers/mathematical/movingmaxby/).
Nulls are ignored and play no part in calculations.

**Signatures**

`movingMax(int windowSize)`
* `windowSize` - How many trailing elements to calculate the maximum value over

**Additional Methods**

| Method                   | Purpose                                                                                                                                                                                                                          |
|--------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `excludePartialValues()` | When calculating the moving maximum values, and the full size of the window has not yet been reached, the gatherer should suppress emitting values until the lookback window is full. [See example.](#excluding-partial-values) |
| `withOriginal()`         | Emit both the original stream element and its calculated moving maximum value wrapped in a `WithOriginal` object. [See example.](#emit-original-value-and-calculated-value)                                                      |

### Examples

#### Moving maximum over a window size 3

```java
Stream
    .of(3, 2, 1, 3, 4)
    .gather(Gatherers4j.movingMax(3))
    .toList();

// [ 3, 3, 3, 3, 4 ]
```


#### Excluding partial values

Showing that in-process moving maximum values are not emitted for each element until the lookback window has been filled.

```java
Stream
    .of(3, 2, 1, 3, 4)
    .gather(Gatherers4j.movingMax(3).excludePartialValues())
    .toList();

// [ 3, 3, 4 ]
```

#### Showing nulls are ignored by default

```java
Stream
    .of(null, 3, null, 2, 1, 3, 4)
    .gather(Gatherers4j.movingMax(3))
    .toList();

// [ 3, 3, 3, 3, 4 ]
```

#### Emit original value and calculated value

Note that this call may need a type witness due to generic type erasure in Java.

```java
Stream
    .of(3, 2, 1, 3, 4)
    .gather(Gatherers4j.<Integer>movingMax(3).withOriginal())
    .toList();

// [
//   WithOriginal[original=3, calculated=3], 
//   WithOriginal[original=2, calculated=3], 
//   WithOriginal[original=1, calculated=3], 
//   WithOriginal[original=3, calculated=3], 
//   WithOriginal[original=4, calculated=4]
// ]

```