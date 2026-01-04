---
title: "distinctBy()"
linkTitle: "distinctBy()"
show_in_table: true
category: Filtering and Selection
description: Filter a stream such that it only contains distinct elements as measured by the given `mappingFunction`.
---


### Implementation Notes

This gatherer uses `Object::equals()` to measure equality of objects returned from the `mappingFunction`. Encounter
order is preserved, so the first instance of a non-distinct element is the one that will be emitted to the output stream.

For a version of this function that measures distinctiveness based on `Object::equals()` alone, see the {{< jdklink linkName="Stream::distinct()" package="java.base/java/util/stream/Stream.html#distinct()" >}} in the JDK.
See also [`uniquelyOccurring()`](/gatherers4j/gatherers/filtering-and-selection/uniquelyoccurring/) which emits elements that only exist once in the input stream.

**Signature**

`distinctBy(Function<INPUT, Object> mappingFunction)`

* `mappingFunction` - A non-null function to map `INPUT` types to an arbitrary `Object` to use for comparison

### Examples

#### Filter objects distinctly by a specific property

```java
record Person(String firstName, String lastName) {}

Stream
    .of(
        new Person("Todd", "Ginsberg"),
        new Person("Emma", "Ginsberg"),
        new Person("Todd", "Smith")
    )
    .gather(Gatherers4j.distinctBy(Person::firstName))
    .toList();

// [Person("Todd", "Ginsberg"), Person("Emma", "Ginsberg")]
```

