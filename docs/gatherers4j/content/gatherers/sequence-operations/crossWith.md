---
title: "crossWith()"
linkTitle: "crossWith()"
show_in_table: true
category: Sequence Operations
description: Cross every element of the input stream with every element of the given source, emitting them to the output stream as a `Pair<INPUT, CROSS>`.
---

### Implementation Notes

This gather pairs up each element of the input stream with some source of other elements, which do not have to be of the same type. While it is possible
to have the `source` of elements be empty, the resulting output stream would be too so it is not recommended (this condition is not checked). It is also possible 
for the `source` to be infinite, which would pair a single element from the input with whatever the `source` produces, which may make sense in a narrow set of cases
(the implementation does not check for this condition).

**Signatures**

Note there are four possible types for the `source` of crossing elements.

1. `crossWith(Iterator<CROSS> source)`
2. `crossWith(Iterable<CROSS> source)`
3. `crossWith(Stream<CROSS> source)`
4. `crossWith(CROSS... source)`
* `source` Must be non-null



### Examples

#### Cross the input stream with an `Iterator`

```java
final Stream<String> input = Stream.of("A", "B", "C");
final Iterator<Integer> cross = List.of(1, 2, 3).iterator();

input
    .gather(Gatherers4j.crossWith(cross))
    .toList();

// [
//    Pair("A", 1), Pair("A", 2), Pair("A", 3),
//    Pair("B", 1), Pair("B", 2), Pair("B", 3),
//    Pair("C", 1), Pair("C", 2), Pair("C", 3)
// ]
```


#### Cross the input stream with an `Iterable`

```java
final Stream<String> input = Stream.of("A", "B", "C");
final Iterable<Integer> cross = List.of(1, 2, 3);   // `List` implements `Iterable`

input
    .gather(Gatherers4j.crossWith(cross))
    .toList();

// [
//    Pair("A", 1), Pair("A", 2), Pair("A", 3),
//    Pair("B", 1), Pair("B", 2), Pair("B", 3),
//    Pair("C", 1), Pair("C", 2), Pair("C", 3)
// ]
```

#### Cross the input stream with another `Stream`

```java
final Stream<String> input = Stream.of("A", "B", "C");
final Stream<Integer> cross = Stream.of(1, 2, 3);

input
    .gather(Gatherers4j.crossWith(cross))
    .toList();

// [
//    Pair("A", 1), Pair("A", 2), Pair("A", 3),
//    Pair("B", 1), Pair("B", 2), Pair("B", 3),
//    Pair("C", 1), Pair("C", 2), Pair("C", 3)
// ]
```

#### Cross the input stream with a source provided as varargs

```java
final Stream<String> input = Stream.of("A", "B", "C");

input
    .gather(Gatherers4j.crossWith(1, 2, 3))
    .toList();

// [
//    Pair("A", 1), Pair("A", 2), Pair("A", 3),
//    Pair("B", 1), Pair("B", 2), Pair("B", 3),
//    Pair("C", 1), Pair("C", 2), Pair("C", 3)
// ]
```
