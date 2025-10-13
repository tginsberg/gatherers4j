---
title: "runningMin()"
linkTitle: "runningMin()"
show_in_table: true
category: "Mathematical Operations"
description: Calculate the running minimum value of a `Stream<Comparable<T>>`.

---

### Implementation Notes
This implementation is suitable for a Stream of `Comparable<T>`, for a version that takes user-specified `Comparator` see [`runningMinBy()`](/gatherers4j/gatherers/mathematical/runningminby/).
Nulls are ignored and play no part in calculations.

**Signatures**

`runningMin()`

**Additional Methods**

| Method                   | Purpose                                                                                                                                                                      |
|--------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `withOriginal()`         | Emit both the original stream element and its calculated running minimum value wrapped in a `WithOriginal` object. [See example.](#emit-original-value-and-calculated-value) |

### Examples

#### Running minimum

```java
Stream
    .of(3, 2, 1, 2, 3)
    .gather(Gatherers4j.runningMin())
    .toList();

// [ 3, 2, 1, 1, 1 ]
```

#### Showing nulls are ignored by default

```java
Stream
    .of(null, 3, null, 2, 1, 2, 3)
    .gather(Gatherers4j.runningMin())
    .toList();

// [ 3, 2, 1, 1, 1 ]
```

#### Emit original value and calculated value

Note that this call may need a type witness due to generic type erasure in Java.

```java
Stream
    .of(3, 2, 1, 2, 3)
    .gather(Gatherers4j.<Integer>runningMin().withOriginal())
    .toList();

// [
//   WithOriginal[original=3, calculated=3],
//   WithOriginal[original=2, calculated=2],
//   WithOriginal[original=1, calculated=1],
//   WithOriginal[original=2, calculated=1],
//   WithOriginal[original=3, calculated=1]
// ]
```