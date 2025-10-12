---
title: "runninMax()"
linkTitle: "runninMax()"
show_in_table: true
category: "Mathematical Operations"
description: Calculate the running maximum value of a `Stream<Comparable<T>>`.

---

### Implementation Notes
This implementation is suitable for a Stream of `Comparable<T>`, for a version that takes user-specified `Comparator` see [`runningMinBy()`](/gatherers4j/gatherers/mathematical/runningminby/).
Nulls are ignored and play no part in calculations.

**Signatures**

`runninMax()`

### Examples

#### Running maximum

```java
Stream
    .of(1, 2, 3, 2, 3)
    .boxed()
    .gather(Gatherers4j.runningMax())
    .toList();

// [ 1, 2, 3, 3, 3 ]
```

#### Showing nulls are ignored by default

```java
Stream
    .of(null, 1, null, 2, 3, 2, 3)
    .boxed()
    .gather(Gatherers4j.runningMax())
    .toList();

// [ 1, 2, 3, 3, 3 ]
```
