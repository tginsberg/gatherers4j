---
title: "shuffle()"
linkTitle: "shuffle()"
show_in_table: true
category: Sequence Operations
description: Shuffle the input stream into a random order.

---

### Implementation Notes

This implementation reads the entire stream before emitting any results making it inappropriate for infinite streams. There
are two versions of this Gatherer - one that uses the platform default `RandomGenerator` and one that allows the caller to 
specify a `RandomGenerator`.

**Signatures**

1. `shuffle()`
2. `shuffle(RandomGenerator randomGenerator)`

* `randomGenerator` - _(Optional)_ A {{< jdklink linkName="RandomGenerator" package="java.base/java/util/random/RandomGenerator.html" >}}to use as the source of randomness. 


### Examples

#### Randomly shuffle the input stream specifying the source of random

```java
Stream
    .of("A", "B", "C", "D", "E", "F", "G")
    .gather(Gatherers4j.shuffle(new Random(42)))
    .toList();
    
// [ "B", "D", "F", "A", "E", "G", "C" ]
```

#### Randomly shuffle the input stream with the default source of random

```java
Stream
    .of("A", "B", "C", "D", "E", "F", "G");
    .gather(Gatherers4j.shuffle())
    .toList();
    
// [ "F", "D", "A", "G", "B", "C", "E" ]  <-- Random!
```
