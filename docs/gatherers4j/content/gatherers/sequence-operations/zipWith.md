---
title: "zipWith()"
linkTitle: "zipWith()"
show_in_table: true
category: Sequence Operations
description: Creates a stream of `Pair<FIRST,SECOND>` objects whose values come from the input stream and the source of arguments provided 

---

### Implementation Notes

This gatherer pairs elements from the input stream together with elements from some other source to the output stream. The default implementation assumes that
both input stream and argument source are of the same length. By default, the gatherer stops emitting elements when it exhausts either the source or argument. Additional methods to alter
this behavior are available, see "Additional Methods" below.

**Signatures**

Note there are three possible types for the `other` of interleaved elements.

1. `zipWith(Iterator<SECOND> other)`
2. `zipWith(Iterable<SECOND> other)`
3. `zipWith(Stream<SECOND> other)`
4. `zipWith(SECOND... other)`
* `other` Must be non-null

**Additional Methods**

| Method                               | Purpose                                                                                                                                                                                                                                                                                              |
|--------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `nullArgumentWhenSourceLonger()`     | When the argument `Iterable`, `Iterator`, `Stream` or varargs runs out of elements before the source stream does, use `null` for the remaining `SECOND` elements of each `Pair` until the source is exhausted. [See example.](#pair-the-input-stream-with-null-if-the-argument-source-is-shorter)                          |
| `nullSourceWhenArgumentLonger()`     | When the source stream runs out of elements before the argument `Iterable`, `Iterator`, `Stream`, or varargs does, use `null` for the remaining `FIRST` elements of each `Pair` until the argument is exhausted. [See example.](#pair-the-argument-stream-with-null-if-the-input-source-is-shorter)                                  |
| `argumentWhenSourceLonger(function)` | When the argument `Iterable`, `Iterator`, `Stream`, or varargs runs out of elements before the source stream does, use the result of the `function` provided for the remaining `SECOND` elements of each `Pair`until the source is exhausted. [See example.](#pair-the-input-stream-with-the-result-of-a-function-if-the-argument-source-is-shorter)   |
| `sourceWhenArgumentLonger(function)` | When the source stream runs out of elements before the argument `Iterable`, `Iterator`,  `Stream` or varargs does, use the result of the `function` provided for the remaining `FIRST` elements of each `Pair` until the argument is exhausted. [See example.](#pair-the-input-stream-with-the-result-of-a-function-if-the-argument-source-is-shorter) |

### Examples

#### Pair the input stream with an `Iterator`


```java
final Stream<String> left = Stream.of("A", "B", "C");
final Iterator<Integer> right = List.of(1, 2, 3).iterator();

left
    .gather(Gatherers4j.zipWith(right))
    .toList();

//  [ Pair("A", 1), Pair("B", 2), Pair("C", 3) ]
```


#### Pair the input stream with an `Iterable`

```java
final Stream<String> left = Stream.of("A", "B", "C");
final Iterable<Integer> right = List.of(1, 2, 3);

left
    .gather(Gatherers4j.zipWith(right))
    .toList();

//  [ Pair("A", 1), Pair("B", 2), Pair("C", 3) ]
```

#### Pair the input stream with another `Stream`


```java
final Stream<String> left = Stream.of("A", "B", "C");
final Stream<Integer> right = Stream.of(1, 2, 3);

left
        .gather(Gatherers4j.zipWith(right))
        .toList();

//  [ Pair("A", 1), Pair("B", 2), Pair("C", 3) ]
```

#### Pair the input stream with elements provided as varargs


```java
final Stream<String> left = Stream.of("A", "B", "C");

left
        .gather(Gatherers4j.zipWith(1, 2, 3))
        .toList();

//  [ Pair("A", 1), Pair("B", 2), Pair("C", 3) ]
```

#### Pair the input stream with null if the argument source is shorter

Note you may have to use a type witness here due to how Java generics work in this case.

```java
final Stream<String> left = Stream.of("A", "B", "C");
final Stream<Integer> right = Stream.of(1);

left
    .gather(
        Gatherers4j.<String, Integer>zipWith(right).nullArgumentWhenSourceLonger()
    )
    .toList();

//  [ Pair("A", 1), Pair("B", null), Pair("C", null) ]
```

#### Pair the argument stream with null if the input source is shorter

Note you may have to use a type witness here due to how Java generics work in this case.

```java
final Stream<String> left = Stream.of("A");
final Stream<Integer> right = Stream.of(1, 2, 3);

left
    .gather(
        Gatherers4j.<String, Integer>zipWith(right).nullSourceWhenArgumentLonger()
    )
    .toList();

//  [ Pair("A", 1), Pair(null, 2), Pair(null, 3) ]
```

#### Pair the input stream with the result of a function if the argument source is shorter

Note you may have to use a type witness here due to how Java generics work in this case.

When the input source is shorter than the argument source, use the String version of the input element instead.

```java
final Stream<String> left = Stream.of("A");
final Stream<Integer> right = Stream.of(1, 2, 3, 4);

left
    .gather(
        Gatherers4j.<String, Integer>zipWith(right).sourceWhenArgumentLonger(String::valueOf)
    )
    .toList();

// [ Pair("A", 1), Pair("2", 2), Pair("3", 3), Pair("4", 4) ]
```

#### Pair the argument stream with the result of a function if the input source is shorter

Note you may have to use a type witness here due to how Java generics work in this case. 

When the argument source is shorter than the input source, use the length of the input element instead.

```java
final Stream<String> left = Stream.of("A", "Bb", "Ccc", "Dddd");
final Stream<Integer> right = Stream.of(1);


left
    .gather(
        Gatherers4j.<String, Integer>zipWith(right).argumentWhenSourceLonger(String::length)
    )
    .toList();

// [ Pair("A", 1), Pair("Bb", 2), Pair("Ccc", 3), Pair("Dddd", 4) ]
```