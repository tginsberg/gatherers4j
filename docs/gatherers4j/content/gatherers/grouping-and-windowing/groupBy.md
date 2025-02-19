---
title: "groupBy()"
linkTitle: "groupBy()"
show_in_table: true
category: Grouping and Windowing
description: Turn a `Stream<INPUT>` into a `Stream<List<INPUT>>` where consecutive equal elements are in the same `List` and equality is measured by the given `mappingFunction`.
---


### Implementation Notes

This function groups adjacent equal elements as measured by the provided `mappingFunction`, into lists. 
The lists returned from this Gatherer are unmodifiable.
For the purposes of this gatherer, nulls are equal to each other but not equal to anything else. 

For a version of `groupBy()` that measures equality with `Object::equals()`, see the [`group()`](/gatherers/grouping-and-windowing/group/)Gatherer.

**Signature**

`groupBy(Function<INPUT, Object> mappingFunction)`
* `mappingFunction` - A non-null function to map `INPUT` types to an arbitrary `Object` to use for comparison

### Examples

#### Group consecutive equal elements

```java
Stream
    .of("A", "A", "A", "B", "B", "C",  "D", "D")
    .gather(Gatherers4j.group())
    .toList();

// [ ["A", "A", "A"] ["B", "B"], ["C"], ["D", "D"] ]
```


#### Group consecutive equal elements, showing treatment of `null`

```java
Stream
    .of(null, null, "A", "A")
    .gather(Gatherers4j.group())
    .toList();

// [ [ null, null], ["A", "A"] ]
```