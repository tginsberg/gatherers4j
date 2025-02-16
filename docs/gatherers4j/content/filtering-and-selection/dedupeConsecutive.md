---
title: "dedupeConsecutive()"
linkTitle: "dedupeConsecutive()"
show_in_table: true
infinite: true
description: Remove consecutive duplicate elements from a stream where equality is measured by `Object.equals(Object)`
---


### Implementation Notes

This function removes consecutive duplicate elements as measured by `Object.equals()`. For the purposes of this gatherer, 
nulls are equal to each other but not equal to anything else.

### Additional Functions

None


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