---
title: "ensureAnyMatch()"
linkTitle: "ensureAnyMatch()"
show_in_table: true
category: Validation and Constraints
description: Ensure that at least one element matches the predicate, and fail otherwise.
infinite_streams: ensure-any-match
---

### Implementation Notes
This gatherer tests elements with the given non-null `Predicate`, including `null` elements, until one matches. To
require every element to match, see
[`ensureAllMatch()`](/gatherers4j/gatherers/validation-and-constraints/ensureallmatch/). To require that no element
matches, see [`ensureNoneMatch()`](/gatherers4j/gatherers/validation-and-constraints/ensurenonematch/).

This is the `ensure` counterpart of the JDK's
{{< jdklink linkName="Stream::anyMatch()" package="java.base/java/util/stream/Stream.html#anyMatch(java.util.function.Predicate)" >}}.
The JDK method is terminal and returns a `boolean`, while this gatherer passes the elements through once one of them
matches.

This gatherer holds elements back until the first match. When an element matches, it emits the held elements and the
match in encounter order, then passes the rest of the stream straight through without testing it. If the stream ends
without a match, it fails with an `IllegalStateException` (`No element matches the predicate`) unless `orElse()` or
`orElseEmpty()` supplies a replacement, and none of the held elements are emitted. Like the JDK's `anyMatch()`, which
returns `false` for an empty stream, an empty stream fails.

**Signature**

`ensureAnyMatch(Predicate<? super INPUT> predicate)`

* `predicate` - A non-null `Predicate` that at least one element must match

**Additional Methods**

| Method             | Purpose                                                                                                                                                                                                                   |
|--------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `orElseEmpty()`    | When the check fails, return an empty stream instead of throwing an exception. [See example.](#emit-an-empty-stream-when-the-check-fails)                                                                                |
| `orElse(supplier)` | When the check fails, emit the elements returned from the given non-null `Supplier<? extends Stream<? extends INPUT>>` instead of throwing an exception. [See example.](#emit-replacement-stream-when-the-check-fails) |

### Examples

#### Ensure that at least one element matches - success path

```java
Stream
    .of("A1", "A2", "B1", "A3")
    .gather(Gatherers4j.ensureAnyMatch(s -> s.startsWith("B")))
    .toList();

// ["A1", "A2", "B1", "A3"]
```

#### Ensure that at least one element matches - failure path

```java
Stream
    .of("A1", "A2", "A3")
    .gather(Gatherers4j.ensureAnyMatch(s -> s.startsWith("B")))
    .toList();

// java.lang.IllegalStateException: No element matches the predicate
```

#### Empty stream fails

```java
Stream
    .<String>empty()
    .gather(Gatherers4j.ensureAnyMatch(s -> s.startsWith("B")))
    .toList();

// java.lang.IllegalStateException: No element matches the predicate
```

#### Passes through after the first match

```java
Stream
    .iterate(0, i -> i + 1)
    .map(i -> i == 2 ? "B" + i : "A" + i)
    .gather(Gatherers4j.ensureAnyMatch(s -> s.startsWith("B")))
    .limit(5)
    .toList();

// ["A0", "A1", "B2", "A3", "A4"]
```

#### Emit an empty stream when the check fails

```java
Stream
    .of("A1", "A2", "A3")
    .gather(Gatherers4j.<String>ensureAnyMatch(s -> s.startsWith("B")).orElseEmpty())
    .toList();

// []
```

#### Emit replacement stream when the check fails

```java
Stream
    .of("A1", "A2", "A3")
    .gather(Gatherers4j
        .<String>ensureAnyMatch(s -> s.startsWith("B"))
        .orElse(() -> Stream.of("Replacement", "Elements"))
    )
    .toList();

// ["Replacement", "Elements"]
```
