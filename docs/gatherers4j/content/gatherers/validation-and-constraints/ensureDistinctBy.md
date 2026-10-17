---
title: "ensureDistinctBy()"
linkTitle: "ensureDistinctBy()"
show_in_table: true
category: Validation and Constraints
description: Ensure that every element in the input stream is distinct as measured by the given `mappingFunction`, and fail exceptionally if a mapped value repeats.
---

### Implementation Notes
This gatherer uses `Object::equals()` to measure equality of objects returned from the `mappingFunction`, and emits the
original elements. A single `null` mapped value is allowed, but a second one is a repeat. For a version that measures
distinctness by the elements themselves, see
[`ensureDistinct()`](/gatherers4j/gatherers/validation-and-constraints/ensuredistinct/). To drop repeated elements
instead of failing, see [`distinctBy()`](/gatherers4j/gatherers/filtering-and-selection/distinctby/).

This gatherer is all-or-nothing, meaning it will not emit any intermediate results to its downstream in the event that
it detects a failure case. It stops reading the stream as soon as a mapped value repeats, and fails with an
`IllegalStateException` unless `orElse()` or `orElseEmpty()` supplies a replacement. This implementation reads the entire stream before emitting any results, making it
inappropriate for infinite streams.

**Signature**

`ensureDistinctBy(Function<INPUT, Object> mappingFunction)`

* `mappingFunction` - A non-null function to map `INPUT` types to an arbitrary `Object` to use for comparison

**Additional Methods**

| Method             | Purpose                                                                                                                                                                                                  |
|--------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `orElseEmpty()`    | When a mapped value repeats, return an empty stream instead of throwing an exception. [See example.](#emit-an-empty-stream-when-elements-repeat)                                                    |
| `orElse(supplier)` | When a mapped value repeats, emit the elements returned from the given non-null `Supplier<Stream<INPUT>>` instead of throwing an exception. [See example.](#emit-replacement-stream-when-elements-repeat) |

### Examples

#### Ensure that no two people share an email address - success path

```java
record Person(String name, String email) {}

Stream
    .of(
        new Person("Todd", "todd@example.com"),
        new Person("Emma", "emma@example.com")
    )
    .gather(Gatherers4j.ensureDistinctBy(Person::email))
    .toList();

// [Person("Todd", "todd@example.com"), Person("Emma", "emma@example.com")]
```

#### Ensure that no two people share an email address - failure path

```java
record Person(String name, String email) {}

Stream
    .of(
        new Person("Todd", "todd@example.com"),
        new Person("Emma", "emma@example.com"),
        new Person("T. Ginsberg", "todd@example.com")
    )
    .gather(Gatherers4j.ensureDistinctBy(Person::email))
    .toList();

// java.lang.IllegalStateException: Elements are not distinct
```

#### Emit an empty stream when elements repeat

```java
Stream
    .of("A", "BB", "C")
    .gather(Gatherers4j.<String>ensureDistinctBy(String::length).orElseEmpty())
    .toList();

// []
```

#### Emit replacement stream when elements repeat

```java
Stream
    .of("A", "BB", "C")
    .gather(Gatherers4j
        .<String>ensureDistinctBy(String::length)
        .orElse(() -> Stream.of("Replacement", "Elements"))
    )
    .toList();

// ["Replacement", "Elements"]
```
