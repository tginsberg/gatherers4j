---
title: "dropEveryNth()"
linkTitle: "dropEveryNth()"
show_in_table: true
category: Filtering and Selection
description: Drop every `n`th element of the stream.
---


### Implementation Notes

This is syntactic sugar on top of [`filterIndexed()`](/gatherers4j/gatherers/filtering-and-selection/filterindexed). For a version
of this gatherer that keeps every n{{< sup "th" >}} element instead of dropping them, see the [`takeEveryNth()`](/gatherers4j/gatherers/filtering-and-selection/takeeverynth/) Gatherer.

**Signature**

`dropEveryNth(int count)`

* `count` - The periodic number of the elements to keep, must be at least 2

### Examples

#### Drop every 3rd element

```java
Stream
    .of("A", "B", "C", "D", "E", "F", "G")
    .gather(Gatherers4j.dropEveryNth(3))
    .toList();
    
// ["B", "C", "E", "F"]
```
