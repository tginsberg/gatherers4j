---
title: "ensureDistinct()"
linkTitle: "ensureDistinct()"
show_in_table: true
category: Validation and Constraints
description: Ensure that every element in the input stream is distinct, and fail exceptionally if one repeats.
infinite_streams: ensure-distinct
parallel: true
---

### Implementation Notes
This gatherer uses `Object::equals()` to measure equality. A single `null` element is allowed, but a second one is a
repeat. For a version that measures distinctness by a mapped value, see
[`ensureDistinctBy()`](/gatherers4j/gatherers/validation-and-constraints/ensuredistinctby/). To drop repeated elements
instead of failing, see [`distinctBy()`](/gatherers4j/gatherers/filtering-and-selection/distinctby/).

This gatherer is all-or-nothing, meaning it will not emit any intermediate results to its downstream in the event that
it detects a failure case. It stops reading the stream as soon as an element repeats, and fails with an
`IllegalStateException` unless `orElse()` or `orElseEmpty()` supplies a replacement.

**Signature**

`ensureDistinct()`

**Additional Methods**

| Method             | Purpose                                                                                                                                                                                                  |
|--------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `orElseEmpty()`    | When an element repeats, return an empty stream instead of throwing an exception. [See example.](#emit-an-empty-stream-when-elements-repeat)                                                    |
| `orElse(supplier)` | When an element repeats, emit the elements returned from the given non-null `Supplier<? extends Stream<? extends INPUT>>` instead of throwing an exception. [See example.](#emit-replacement-stream-when-elements-repeat) |

### Examples

#### Ensure that all elements are distinct - success path

```java
Stream
    .of("A", "B", "C")
    .gather(Gatherers4j.ensureDistinct())
    .toList();

// ["A", "B", "C"]
```

#### Ensure that all elements are distinct - failure path

```java
Stream
    .of("A", "B", "A")
    .gather(Gatherers4j.ensureDistinct())
    .toList();

// java.lang.IllegalStateException: Elements are not distinct
```

#### Emit an empty stream when elements repeat

```java
Stream
    .of("A", "B", "A")
    .gather(Gatherers4j.<String>ensureDistinct().orElseEmpty())
    .toList();

// []
```

#### Emit replacement stream when elements repeat

```java
Stream
    .of("A", "B", "A")
    .gather(Gatherers4j
        .<String>ensureDistinct()
        .orElse(() -> Stream.of("Replacement", "Elements"))
    )
    .toList();

// ["Replacement", "Elements"]
```
