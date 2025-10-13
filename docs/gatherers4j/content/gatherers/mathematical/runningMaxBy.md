---
title: "runningMaxBy()"
linkTitle: "runningMaxBy()"
show_in_table: true
category: "Mathematical Operations"
description: Calculate the running maximum value of a `Stream<T>`, as calculated by the given `Comparator`.
---

### Implementation Notes
This implementation is suitable for calculating the running maximum value of a `Stream<INPUT>` via a given `Comparator`; for a version that operates directly on a `Stream<Comparable<INPUT>>`, see [`runningMax()`](/gatherers4j/gatherers/mathematical/runningmax/).
Nulls are ignored and play no part in calculations.

**Signatures**

`runningMaxBy(Comparator<INPUT> comparator)`
* `comparator` - A non-null `Comparator<INPUT>` to use for comparing elements

**Additional Methods**

| Method                   | Purpose                                                                                                                                                                      |
|--------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `withOriginal()`         | Emit both the original stream element and its calculated running maximum value wrapped in a `WithOriginal` object. [See example.](#emit-original-value-and-calculated-value) |

### Examples

#### Running maximum value

```java
Stream
    .of("1", "2", "3", "2", "3")
    .gather(Gatherers4j.runningMaxBy(comparing(Integer::valueOf)))
    .toList();

// [ "1", "2", "3", "3", "3" ]
```


#### Showing nulls are ignored by default

```java
Stream
    .of(null, "1", null, "2", "3", "2", "3")
    .gather(Gatherers4j.runningMaxBy(comparing(Integer::valueOf)))
    .toList();

// [ "1", "2", "3", "3", "3" ]
```

#### Emit original value and calculated value

Note that this call may need a type witness due to generic type erasure in Java.

```java
Stream
    .of("1", "2", "3", "2", "3")
    .gather(Gatherers4j.<String>runningMaxBy(comparing(Integer::valueOf)).withOriginal())
    .toList();

// [
//   WithOriginal[original=1, calculated=1], 
//   WithOriginal[original=2, calculated=2], 
//   WithOriginal[original=3, calculated=3], 
//   WithOriginal[original=2, calculated=3], 
//   WithOriginal[original=3, calculated=3]
// ]
```