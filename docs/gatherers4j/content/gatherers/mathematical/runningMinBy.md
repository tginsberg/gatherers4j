---
title: "runningMinBy()"
linkTitle: "runningMinBy()"
show_in_table: true
category: "Mathematical Operations"
description: Calculate the running minimum value of a `Stream<T>`, as calculated by the given `Comparator`.
---

### Implementation Notes
This implementation is suitable for calculating the running minimum value of a `Stream<INPUT>` via a given `Comparator`; for a version that operates directly on a `Stream<Comparable<INPUT>>`, see [`runningMin()`](/gatherers4j/gatherers/mathematical/runningmin/).
Nulls are ignored and play no part in calculations.

**Signatures**

`runningMinBy(Comparator<INPUT> comparator)`
* `comparator` - A non-null `Comparator<INPUT>` to use for comparing elements

**Additional Methods**

| Method                   | Purpose                                                                                                                                                                      |
|--------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `withOriginal()`         | Emit both the original stream element and its calculated running minimum value wrapped in a `WithOriginal` object. [See example.](#emit-original-value-and-calculated-value) |


### Examples

#### Running minimum value

```java
Stream
    .of("3", "2", "1", "2", "3")
    .gather(Gatherers4j.runningMinBy(comparing(Integer::valueOf)))
    .toList();

// [ "3", "2", "1", "1", "1" ]
```


#### Showing nulls are ignored by default

```java
Stream
    .of(null, "3", null, "2", "1", "2", "3")
    .gather(Gatherers4j.runningMinBy(comparing(Integer::valueOf)))
    .toList();

// [ "3", "2", "1", "1", "1" ]
```

#### Emit original value and calculated value

Note that this call may need a type witness due to generic type erasure in Java.

```java
Stream
    .of("3", "2", "1", "2", "3")
    .gather(Gatherers4j.<String>runningMinBy(comparing(Integer::valueOf)).withOriginal())
    .toList();

// [
//   WithOriginal[original=3, calculated=3], 
//   WithOriginal[original=2, calculated=2], 
//   WithOriginal[original=1, calculated=1], 
//   WithOriginal[original=2, calculated=1], 
//   WithOriginal[original=3, calculated=1]
// ]

```