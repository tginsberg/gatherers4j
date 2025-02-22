---
title: "dedupeConsecutive()"
linkTitle: "dedupeConsecutive()"
show_in_table: true
category: Filtering and Selection
description: Remove consecutive duplicate elements from a stream where equality is measured by `Object::equals()`
---


### Implementation Notes

This function removes consecutive duplicate elements as measured by `Object::equals()`. For the purposes of this gatherer, 
nulls are equal to each other but not equal to anything else. 

For a version of `dedupeConsecutive()` that measures equality with a user-provided function, see the [`dedupeConsecutiveBy()`](/gatherers/filtering-and-selection/dedupeconsecutiveby/)Gatherer.

**Signature**

`dedupeConsecutive()`

### Diagram

{{< dual-mode-image light="../img/dedupe_consecutive-light.png" dark="../img/dedupe_consecutive-dark.png" alt="Marble diagram" >}}


### Examples

#### Remove consecutive duplicates

```java
Stream
    .of("A", "A", "A", "B", "B", "C", "C", "D", "A", "B", "C")
    .gather(Gatherers4j.dedupeConsecutive())
    .toList();

// ["A", "B", "C", "D", "A", "B", "C"]
```


#### Remove consecutive duplicates, showing treatment of `null`

```java
Stream
    .of(null, null, "A", "A", null)
    .gather(Gatherers4j.dedupeConsecutive())
    .toList();

// [null, "A", null]
```