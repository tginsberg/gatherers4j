---
title: "ensureSize()"
linkTitle: "ensureSize()"
show_in_table: true
category: Validation and Constraints
description: Ensure the input stream is the proper `length` as specified by `size`.
---

### Implementation Notes
This gatherer is all-or-nothing, meaning it will not emit any intermediate results to its downstream in the event that it detects a failure case.

**Signature**

`ensureSize(Size size, long length)`
* `size` - How to measure the size, valid values are `Equals`, `LessThan`, `LessThanOrEqualTo`, `GreaterThan`, and `GreaterThanOrEqualTo`
* `length` - With relation to `size`, how many elements to compare against
  
**Additional Methods**

| Method             | Purpose                                                                                                                                                                                                                                |
|--------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `orElseEmpty()`    | When the input stream is the wrong size, return an empty stream instead of throwing an exception. [See example.](#emit-an-empty-stream-when-input-stream-size-is-not-correct)                                                          |
| `orElse(supplier)` | When the input stream is the wrong size, emit the elements returned from the given non-null `Supplier<Stream<INPUT>>` instead of throwing an exception. [See example.](#emit-replacement-stream-when-input-stream-size-is-not-correct) |

### Examples

#### Ensure exact size - success case

```java
Stream
    .of("A", "B", "C")
    .gather(Gatherers4j.ensureSize(Size.Equals, 3))
    .toList();

// ["A", "B", "C"]
```

#### Ensure exact size - failure case

```java
Stream
    .of("A", "B", "C")
    .gather(Gatherers4j.ensureSize(Size.Equals, 2))
    .toList();

// java.lang.IllegalStateException: Invalid stream size: wanted Equals 2
```

#### Emit an empty stream when input stream size is not correct

```java
Stream
    .of("A", "B", "C")
    .gather(Gatherers4j.ensureSize(Size.GreaterThanOrEqualTo, 2).orElseEmpty())
    .toList();

// []
```

#### Emit replacement stream when input stream size is not correct

```java
Stream
    .of("A", "B", "C", "D")
    .gather(Gatherers4j
        .ensureSize(Size.LessThan, 4)
        .orElse(() -> Stream.of("Replacement", "Elements"))
    )
    .toList();

// [ "Replacement", "Elements" ]
```
