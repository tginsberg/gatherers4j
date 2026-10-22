---
title: "distinctLatestBy()"
linkTitle: "distinctLatestBy()"
show_in_table: true
category: Filtering and Selection
description: Filter a stream such that it only contains the latest of each distinct element as measured by the given `mappingFunction`.
infinite_streams: distinct
parallel: true
---


### Implementation Notes

This gatherer uses `Object::equals()` to measure equality of objects returned from the `mappingFunction`. When more
than one element maps to the same value, the last one encountered is emitted. Each emitted element appears in the
position where its mapped value was first encountered.

To keep the first instance of each distinct element instead, and emit it as soon as it is encountered, see
[`distinctBy()`](/gatherers4j/gatherers/filtering-and-selection/distinctby/).

**Signature**

`distinctLatestBy(Function<? super INPUT, ? extends @Nullable Object> mappingFunction)`

* `mappingFunction` - A non-null function to map `INPUT` types to an arbitrary `Object` to use for comparison. It may return `null`.

### Examples

#### Filter objects distinctly by a specific property, keeping the latest

```java
record Person(String firstName, String lastName) {}

Stream
    .of(
        new Person("Todd", "Ginsberg"),
        new Person("Emma", "Ginsberg"),
        new Person("Todd", "Smith")
    )
    .gather(Gatherers4j.distinctLatestBy(Person::firstName))
    .toList();

// [Person("Todd", "Smith"), Person("Emma", "Ginsberg")]
```

#### Keep the most recent update for each id

```java
record StatusUpdate(int orderId, String status) {}

Stream
    .of(
        new StatusUpdate(1, "Created"),
        new StatusUpdate(2, "Created"),
        new StatusUpdate(1, "Paid"),
        new StatusUpdate(1, "Shipped")
    )
    .gather(Gatherers4j.distinctLatestBy(StatusUpdate::orderId))
    .toList();

// [StatusUpdate(1, "Shipped"), StatusUpdate(2, "Created")]
```
