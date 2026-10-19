---
title: "uniquelyOccurringBy()"
linkTitle: "uniquelyOccurringBy()"
show_in_table: true
category: Filtering and Selection
description: Emit only those elements that occur in the input stream a single time, as measured by the given function.
infinite_streams: distinct
parallel: true

---

### Implementation Notes

Encounter order of uniquely occurring elements is preserved. 

**Signature**

`uniquelyOccurringBy(Function<? super INPUT, ?> mappingFunction)`

* `mappingFunction` - A non-null function to map `INPUT` types to an arbitrary `MAPPED` Object to use for comparison


### Examples

#### Limit stream to elements that occur a single time, as measured by the given function

```java
Stream
    .of("A", "BB", "CCC", "A")
    .gather(Gatherers4j.uniquelyOccurringBy(String::length))
    .toList();

// ["BB", "CCC"]
```
