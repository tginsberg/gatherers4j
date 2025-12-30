---
title: "interleaveWith()"
linkTitle: "interleaveWith()"
show_in_table: true
category: Sequence Operations
description: Create a stream of alternating objects from the input stream and the argument source.

---

### Implementation Notes

This gatherer alternates elements from the input stream and some other source into the output stream. The default implementation assumes that
both input stream and argument source are of the same length. If one is longer, it is not emitted to the output. Additional methods to alter
this behavior (append source if longer, append argument if longer, append either if longer) are available as well.

**Signatures**

Note there are several possible types for the `other` of interleaved elements.

1. `interleaveWith(Iterator<CROSS> other)`
2. `interleaveWith(Iterable<CROSS> other)`
3. `interleaveWith(Stream<CROSS> other)`
4. `interleaveWith(CROSS... other)`
* `other` - Must be non-null

**Additional Methods**

| Method                     | Purpose                                                                                                                                                                                                                                                     |
|----------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `appendLonger()`           | If the source stream and the argument stream/iterator/iterable/varargs provide a different number of elements, append all the remaining elements from either one to the output stream. [See example.](#interleave-the-longer-of-source-or-argument)         |
| `appendSourceIfLonger()`   | If the source stream provides more elements than the argument stream/iterator/iterable/varargs, append all the remaining elements to the output stream. [See example.](#append-the-source-if-it-is-longer)                                                  |
| `appendArgumentIfLonger()` | If the argument stream/iterator/iterable/varargs provides more elements than the source stream, append all remaining elements from the argument stream/iterator/iterable/varargs to the output stream. [See example.](#append-the-argument-if-it-is-longer) |

### Examples

#### Interleave the input stream with an `Iterator`

```java
final Stream<String> left = Stream.of("A", "B", "C");
final Iterator<String> right = List.of("D", "E", "F").iterator();

// Act
left
    .gather(Gatherers4j.interleaveWith(right))
    .toList();

// "A", "D", "B", "E", "C", "F"
```


#### Interleave the input stream with an `Iterable`

```java
final Stream<String> left = Stream.of("A", "B", "C");
final Iterable<String> right = List.of("D", "E", "F");

// Act
left
    .gather(Gatherers4j.interleaveWith(right))
    .toList();

// "A", "D", "B", "E", "C", "F"
```

#### Interleave the input stream with another `Stream`

```java
final Stream<String> left = Stream.of("A", "B", "C");
final Stream<String> right = Stream.of("D", "E", "F");

// Act
left
    .gather(Gatherers4j.interleaveWith(right))
    .toList();

// "A", "D", "B", "E", "C", "F"
```

#### Interleave the input stream with elements provided as varargs

```java
final Stream<String> left = Stream.of("A", "B", "C");

// Act
left
    .gather(Gatherers4j.interleaveWith("D", "E", "F"))
    .toList();

// "A", "D", "B", "E", "C", "F"
```

#### Interleave the longer of source or argument

Appends the longer of either the source or argument if one is longer than the other.

In this case, the argument is longer.

```java
final Stream<String> left = Stream.of("A", "B", "C");
final Stream<String> right = Stream.of("D", "E", "F", "G", "H");

left
    .gather(Gatherers4j.interleaveWith(right).appendLonger())
    .toList();

// "A", "D", "B", "E", "C", "F", "G", "H"
```

And in this case, the source is longer.
```java
final Stream<String> left = Stream.of("A", "B", "C", "D", "E");
final Stream<String> right = Stream.of("F", "G", "H");

left
    .gather(Gatherers4j.interleaveWith(right).appendLonger())
    .toList();

// "A", "F", "B", "G", "C", "H", "D", "E"
```

#### Append the source if it is longer

Appends the source elements only if they are longer, but not the argument elements if they are longer.

```java
final Stream<String> left = Stream.of("A", "B", "C", "D", "E");
final Stream<String> right = Stream.of("F", "G", "H");

left
    .gather(Gatherers4j.interleaveWith(right).appendSourceIfLonger())
    .toList();

// "A", "F", "B", "G", "C", "H", "D", "E"
```

#### Append the argument if it is longer

Appends the argument elements only if they are longer, but not the source elements if they are longer.

```java
final Stream<String> left = Stream.of("A", "B", "C");
final Stream<String> right = Stream.of("D", "E", "F", "G", "H");

left
    .gather(Gatherers4j.interleaveWith(right).appendArgumentIfLonger())
    .toList();

// "A", "D", "B", "E", "C", "F", "G", "H"
```
