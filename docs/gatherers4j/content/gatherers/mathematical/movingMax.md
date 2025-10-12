---
title: "movingMax()"
linkTitle: "movingMax()"
show_in_table: true
category: "Mathematical Operations"
description: Create a Stream that represents the moving maximum of a `Stream<T>` looking back `windowSize` number of elements

---

### Implementation Notes
This implementation is suitable for streams whose elements implement `Comparable`, for a version that takes user-specified `Comparator` see [`movingMaxBy()`](/gatherers4j/gatherers/mathematical/movingmaxby/).
Nulls are ignored and play no part in calculations.

**Signatures**

`movingMax(int windowSize)`
* `windowSize` - How many trailing elements to calculate the maximum value over

**Additional Methods**

| Method                                     | Purpose                                                                                                                                                                                                                        |
|--------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `excludePartialValues()`                   | When calculating the moving maximum values, and the full size of the window has not yet been reached, the gatherer should supress emitting values until the lookback window is full. [See example.](#excluding-partial-values) |

### Examples

#### Moving maximum over a window size 3

```java
Stream
    .of(3, 2, 1, 3, 4)
    .boxed()
    .gather(Gatherers4j.movingMax(3))
    .toList();

// [ 3, 3, 3, 3, 4 ]
```


#### Excluding partial values

Showing that in-process moving minimum values are not emitted for each element until the lookback window has been filled.

```java
Stream
    .of(3, 2, 1, 3, 4)
    .boxed()
    .gather(Gatherers4j.movingMax(3).excludePartialValues())
    .toList();

// [ 3, 3, 4 ]
```

#### Showing nulls are ignored by default

```java
Stream
    .of(null, 3, null, 2, 1, 3, 4)
    .boxed()
    .gather(Gatherers4j.movingMax(3))
    .toList();

// [ 3, 3, 3, 3, 4 ]
```
