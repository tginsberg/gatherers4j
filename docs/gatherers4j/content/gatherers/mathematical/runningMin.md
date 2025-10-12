---
title: "runninMin()"
linkTitle: "runninMin()"
show_in_table: true
category: "Mathematical Operations"
description: Calculate the running minimum value of a `Stream<Comparable<T>>`.

---

### Implementation Notes
This implementation is suitable for a Stream of `Comparable<T>`, for a version that takes user-specified `Comparator` see [`runningMinBy()`](/gatherers4j/gatherers/mathematical/runningminby/).
Nulls are ignored and play no part in calculations.

**Signatures**

`runningMin()`

### Examples

#### Running minimum

```java
Stream
    .of(3, 2, 1, 2, 3)
    .boxed()
    .gather(Gatherers4j.runningMin())
    .toList();

// [ 3, 2, 1, 1, 1 ]
```

#### Showing nulls are ignored by default

```java
Stream
    .of(null, 3, null, 2, 1, 2, 3)
    .boxed()
    .gather(Gatherers4j.runningMin())
    .toList();

// [ 3, 2, 1, 1, 1 ]
```
