---
title: "ensureAllMatch()"
linkTitle: "ensureAllMatch()"
show_in_table: true
category: Validation and Constraints
description: Ensure that every element matches the predicate, and fail otherwise.
infinite_streams: ensure-match
parallel: true
---

### Implementation Notes
This gatherer tests every element with the given non-null `Predicate`, including `null` elements. For the opposite
check, see [`ensureNoneMatch()`](/gatherers4j/gatherers/validation-and-constraints/ensurenonematch/). To require only one
matching element, see [`ensureAnyMatch()`](/gatherers4j/gatherers/validation-and-constraints/ensureanymatch/).

This is the `ensure` counterpart of the JDK's
{{< jdklink linkName="Stream::allMatch()" package="java.base/java/util/stream/Stream.html#allMatch(java.util.function.Predicate)" >}}.
The JDK method is terminal and returns a `boolean`, while this gatherer passes the elements through when they all match.

This gatherer is all-or-nothing, meaning it will not emit any intermediate results to its downstream in the event that
it detects a failure case. It stops reading the stream at the first element that does not match, and fails with an
`IllegalStateException` (`Not all elements match the predicate`) unless `orElse()` or `orElseEmpty()` supplies a
replacement. An empty stream passes and emits nothing.

**Signature**

`ensureAllMatch(Predicate<? super INPUT> predicate)`

* `predicate` - A non-null `Predicate` that every element must match

**Additional Methods**

| Method             | Purpose                                                                                                                                                                                                                   |
|--------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `orElseEmpty()`    | When the check fails, return an empty stream instead of throwing an exception. [See example.](#emit-an-empty-stream-when-the-check-fails)                                                                                |
| `orElse(supplier)` | When the check fails, emit the elements returned from the given non-null `Supplier<? extends Stream<? extends INPUT>>` instead of throwing an exception. [See example.](#emit-replacement-stream-when-the-check-fails) |

### Examples

#### Ensure that all elements match - success path

```java
Stream
    .of("A1", "A2", "A3")
    .gather(Gatherers4j.ensureAllMatch(s -> s.startsWith("A")))
    .toList();

// ["A1", "A2", "A3"]
```

#### Ensure that all elements match - failure path

```java
Stream
    .of("A1", "B1", "A2")
    .gather(Gatherers4j.ensureAllMatch(s -> s.startsWith("A")))
    .toList();

// java.lang.IllegalStateException: Not all elements match the predicate
```

#### Emit an empty stream when the check fails

```java
Stream
    .of("A1", "B1", "A2")
    .gather(Gatherers4j.<String>ensureAllMatch(s -> s.startsWith("A")).orElseEmpty())
    .toList();

// []
```

#### Emit replacement stream when the check fails

```java
Stream
    .of("A1", "B1", "A2")
    .gather(Gatherers4j
        .<String>ensureAllMatch(s -> s.startsWith("A"))
        .orElse(() -> Stream.of("Replacement", "Elements"))
    )
    .toList();

// ["Replacement", "Elements"]
```
