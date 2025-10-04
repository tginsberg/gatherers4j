---
title: "uniquelyOccurringBy()"
linkTitle: "uniquelyOccurringBy()"
show_in_table: true
category: Filtering and Selection
description: Emit only those elements that occur in the input stream a single time, as measured by the given function.

---

### Implementation Notes

The implementation does not emit anything until the full input stream has been exhausted, so it is not suitable for infinite streams. 
Encounter order of uniquely occurring elements is preserved. 

**Signature**

`uniquelyOccurringBy(Function<INPUT, MAPPED> mappingFunction)`

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
