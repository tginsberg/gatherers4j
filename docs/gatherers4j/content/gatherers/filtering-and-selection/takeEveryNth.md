---
title: "takeEveryNth()"
linkTitle: "takeEveryNth()"
show_in_table: true
description: Take every `n`th element of the stream.
---


### Implementation Notes

This is syntactic sugar on top of [`filterIndexed()`](/gatherers/filtering-and-selection/filterindexed). For a version
of this gatherer that drops every n{{< sup "th" >}} element instead of taking them, see the [`dropEveryNth()`](/gatherers/filtering-and-selection/dropeverynth/)Gatherer.

**Signature**

`takeEveryNth(int count)`

* `count` - The periodic number of the elements to drop, must be at least 2

### Examples

#### Take every 3rd element

```java
Stream
    .of("A", "B", "C", "D", "E", "F", "G")
    .gather(Gatherers4j.takeEveryNth(3))
    .toList();
    
// ["A", "D", "G"]
```

