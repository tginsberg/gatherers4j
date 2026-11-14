---
title: "dropWhileIndexed()"
linkTitle: "dropWhileIndexed()"
show_in_table: true
category: Filtering and Selection
description: Drop elements while the `predicate`, which takes both the element and its zero-based index, holds, then emit the rest.
---

### Implementation Notes

This is an index-aware version of {{< jdklink linkName="Stream::dropWhile()" package="java.base/java/util/stream/Stream.html#dropWhile(java.util.function.Predicate)" >}}
from the JDK. Once the `predicate` fails, it is not called again, and that element and every element after it are emitted.

**Signature**

`dropWhileIndexed(BiPredicate<? super Long, ? super INPUT> predicate)`

* `predicate` - A non-null `BiPredicate<Long,INPUT>` where the `Long` is the zero-based index of the element being tested, and the `INPUT` is the element itself.

### Examples

#### Drop a header of up to three lines starting with "#"

```java
Stream
    .of("# title", "# author", "A", "# not a header", "B")
    .gather(Gatherers4j.dropWhileIndexed((index, element) -> index < 3 && element.startsWith("#")))
    .toList();

// ["A", "# not a header", "B"]
```
