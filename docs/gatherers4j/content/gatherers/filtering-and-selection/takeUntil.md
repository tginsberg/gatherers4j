---
title: "takeUntil()"
linkTitle: "takeUntil()"
show_in_table: true
category: Filtering and Selection
description: Take elements from the input stream until the `predicate` is met, including the first element that matches the `predicate`.

---

### Implementation Notes

This is an inclusive version (keeps the first matching element) of {{< jdklink linkName="Stream::takeWhile()" package="java.base/java/util/stream/Stream.html#takeWhile()" >}}
from the JDK, which is exclusive (does not include the first matching element).

**Signature**

`takeUntil(Predicate<INPUT> predicate)`

* `predicate` - A non-null predicate function which should return `true` when `takeUntil` should stop including elements from the input stream (inclusive).

### Examples

#### Take elements from the stream until one of them is "C", inclusive

```java
Stream
    .of("A", "B", "C", "D", "E", "F", "G")
    .gather(Gatherers4j.takeUntil(it -> it.equals("C")))
    .toList()
    
// ["A", "B", "C"]
```
