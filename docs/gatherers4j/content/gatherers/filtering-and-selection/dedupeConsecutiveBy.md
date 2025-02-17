---
title: "dedupeConsecutiveBy()"
linkTitle: "dedupeConsecutiveBy()"
show_in_table: true
category: Filtering and Selection
description: Remove consecutive duplicates, where equality is measured by a given function
---

### Implementation Notes

This function removes consecutive duplicate elements as measured by `Object.equals()`. For the purposes of this gatherer,
nulls are equal to each other but not equal to anything else.

For a version of `dedupeConsecutiveBy` that measures equality via `Object::equals`, see the [`dedupeConsecutive()`](/gatherers/filtering-and-selection/dedupeconsecutive/)Gatherer.


**Signature**

`dedupeConsecutiveBy(Function<INPUT, Object> mappingFunction)`

* `mappingFunction` - A non-null `Function<INPUT, Object>` to compare elements of a `Stream<INPUT>`

### Diagram

{{< dual-mode-image light="../img/dedupe_consecutive_by-light.png" dark="../img/dedupe_consecutive_by-dark.png" alt="Marble diagram" >}}


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
