---
title: "uniquelyOccurring()"
linkTitle: "uniquelyOccurring()"
show_in_table: true
category: Filtering and Selection
description: Emit only those elements that occur in the input stream a single time.

---

### Implementation Notes

The implementation does not emit anything until the full input stream has been exhausted, so it is not suitable for infinite streams. 
Encounter order of uniquely occurring elements is preserved. 

Note that `uniquelyOccurring()` is semantically different from the JDK's {{< jdklink linkName="Stream::distinct()" package="java.base/java/util/stream/Stream.html#distinct()" >}}or 
Gatherer4j's [`distinctBy()`](/gatherers/filtering-and-selection/distinctby/)because only elements that are already distinct are emitted, rather than emitting all elements only once.

**Signature**

`uniquelyOccurring()`

### Examples

#### Limit stream to elements that occur a single time

```java
Stream
    .of("A", "B", "C", "A")
    .gather(Gatherers4j.uniquelyOccurring())
    .toList();

// ["B", "C"]
```
