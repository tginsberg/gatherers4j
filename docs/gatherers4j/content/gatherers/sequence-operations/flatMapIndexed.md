---
title: "flatMapIndexed()"
linkTitle: "flatMapIndexed()"
show_in_table: true
category: Sequence Operations
description: Replace each element with the elements of a `Stream` produced from the element and its zero-based index.
---

### Implementation Notes

This is an index-aware version of {{< jdklink linkName="Stream::flatMap()" package="java.base/java/util/stream/Stream.html#flatMap(java.util.function.Function)" >}}
from the JDK, and follows the same rules: a `null` result is treated as an empty stream, and each mapped stream is
closed once its elements have been emitted. The index advances for every input element, including those that map to
an empty or `null` stream.

**Signature**

`flatMapIndexed(BiFunction<? super Long, ? super INPUT, ? extends Stream<? extends OUTPUT>> mappingFunction)`

* `mappingFunction` - A non-null function that maps each element and its zero-based index to a `Stream` of output elements. It may return `null`, which emits nothing for that element.

### Examples

#### Repeat each element one more time than its index

```java
Stream
    .of("A", "B", "C")
    .gather(
        Gatherers4j.flatMapIndexed(
            (index, element) -> Stream.generate(() -> element).limit(index + 1)
        )
    )
    .toList();

// ["A", "B", "B", "C", "C", "C"]
```

#### Find every match in a list of lines, with its line number

Each line can produce any number of matches, including none.

```java
record Match(long line, int column) {}

final Pattern error = Pattern.compile("ERROR");

Stream
    .of("ERROR: disk full", "INFO: retrying", "WARN: slow, ERROR: timeout")
    .gather(
        Gatherers4j.flatMapIndexed(
            (index, line) -> error.matcher(line).results().map(m -> new Match(index + 1, m.start()))
        )
    )
    .toList();

// [Match[line=1, column=0], Match[line=3, column=12]]
```

#### Insert a separator after every third element

```java
Stream
    .of("A", "B", "C", "D", "E", "F", "G")
    .gather(
        Gatherers4j.flatMapIndexed(
            (index, element) -> index > 0 && index % 3 == 0 ? Stream.of("-", element) : Stream.of(element)
        )
    )
    .toList();

// ["A", "B", "C", "-", "D", "E", "F", "-", "G"]
```
