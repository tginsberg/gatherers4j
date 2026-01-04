---
title: "runningMax()"
linkTitle: "runningMax()"
show_in_table: true
category: "Mathematical Operations"
description: Calculate the running maximum value of a `Stream<Comparable<T>>`.

---

### Implementation Notes
This implementation is suitable for a Stream of `Comparable<T>`, for a version that takes user-specified `Comparator` see [`runningMaxBy()`](/gatherers4j/gatherers/mathematical/runningmaxby/).
Nulls are ignored and play no part in calculations.

**Signatures**

`runningMax()`

**Additional Methods**

| Method                   | Purpose                                                                                                                                                                      |
|--------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `withOriginal()`         | Emit both the original stream element and its calculated running maximum value wrapped in a `WithOriginal` object. [See example.](#emit-original-value-and-calculated-value) |


### Examples

#### Running maximum

```java
Stream
    .of(1, 2, 3, 2, 3)
    .gather(Gatherers4j.runningMax())
    .toList();

// [ 1, 2, 3, 3, 3 ]
```

#### Showing nulls are ignored by default

```java
Stream
    .of(null, 1, null, 2, 3, 2, 3)
    .gather(Gatherers4j.runningMax())
    .toList();

// [ 1, 2, 3, 3, 3 ]
```

#### Emit original value and calculated value

Note that this call may need a type witness due to generic type erasure in Java.

```java
Stream
    .of(1, 2, 3, 2, 3)
    .gather(Gatherers4j.<Integer>runningMax().withOriginal())
    .toList();

// [
//   WithOriginal[original=1, calculated=1],
//   WithOriginal[original=2, calculated=2],
//   WithOriginal[original=3, calculated=3],
//   WithOriginal[original=2, calculated=3],
//   WithOriginal[original=3, calculated=3]
// ]
```