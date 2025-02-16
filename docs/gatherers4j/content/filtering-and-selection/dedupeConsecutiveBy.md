---
title: "dedupeConsecutiveBy()"
linkTitle: "dedupeConsecutiveBy()"
show_in_table: true
description: Remove consecutive duplicates, where equality is measured by a given function
---

### Implementation Notes

This function removes consecutive duplicate elements as measured by `Object.equals()`. For the purposes of this gatherer,
nulls are equal to each other but not equal to anything else.

### Additional Functions

None


### Examples

#### Remove consecutive duplicates using a function reference

```java
record Person(String firstName, String lastName) {}

Stream
    .of(
        new Person("Todd", "Ginsberg"),
        new Person("Todd", "Smith"),
        new Person("Emma", "Ginsberg")
    )
    .gather(Gatherers4j.dedupeConsecutiveBy(Person::firstName))
    .toList();

// [Person("Todd", "Ginsberg"), Person("Emma", "Ginsberg")]
```
