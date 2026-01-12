---
title: "runningGeometricMean()"
linkTitle: "runningGeometricMean()"
show_in_table: true
category: "Mathematical Operations"
description: Calculate the running geometric mean of a `Stream<BigDecimal>`.

---

### Implementation Notes
This implementation is suitable for `Stream<BigDecimal>`. For a version that takes a user-specified mapping function, see [`runningGeometricMeanBy()`](/gatherers4j/gatherers/mathematical/runninggeometricmeanby/).
By default, nulls are ignored and play no part in calculations, see `treatNullAs()` and `treatNullAsOne()` below for ways to change this behavior. The default `MathContext`
for all calculations is {{< jdklink linkName="MathContext.DECIMAL64" package="java.base/java/math/MathContext.html#DECIMAL64" >}}, but this can be overridden (see `withMathContext()`, below).


**Signatures**

`runningGeometricMean()`

**Additional Methods**

| Method                                     | Purpose                                                                                                                                                                                                                                                                                                              |
|--------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `treatNullAsOne()`                         | When encountering a `null` value in a stream, treat it as `BigDecimal.ONE` instead. [See example.](#treating-null-as-one)                                                                                                                                                                                            |
| `treatNullAs(BigDecimal replacement)`      | When encountering a `null` value in a stream, treat it as the given `replacement` value instead. [See example.](#replacing-null-with-another-bigdecimal)                                                                                                                                                             |
| `withMathContext(MathContext mathContext)` | Replace the `MathContext` used for all mathematical operations performed by this gatherer. [See example.](#specifying-a-new-mathcontext)                                                                                                                                                                             |
| `withOriginal()`                           | Include the original input value from the stream in addition to the calculated value in a [`WithOriginal`](https://github.com/tginsberg/gatherers4j/blob/main/src/main/java/com/ginsberg/gatherers4j/dto/WithOriginal.java) record. [See example.](#emitting-a-record-containing-the-original-and-calculated-values) |

Note: `treatNullAsZero()` is also a valid method on this gatherer, but it only makes sense in a very narrow set of circumstances.


### Examples

#### Running geometric mean

```java
Stream
    .of("2.0", "8.0")
    .map(BigDecimal::new)
    .gather(Gatherers4j.runningGeometricMean())
    .toList();

// [ 
//   BigDecimal("2.0"), 
//   BigDecimal("4.0")
// ]
```

#### Showing nulls are ignored by default

```java
Stream
    .of(BigDecimal.valueOf(2), null, BigDecimal.valueOf(8))
    .gather(Gatherers4j.runningGeometricMean())
    .toList();

// [ 
//   BigDecimal("2.0"),
//   BigDecimal("4.0")
// ]
```

#### Treating null as one

```java
Stream
    .of(BigDecimal.valueOf(2), null, BigDecimal.valueOf(8))
    .gather(Gatherers4j.runningGeometricMean().treatNullAsOne())
    .toList();

// [ 
//   BigDecimal("2"),
//   BigDecimal("1.414213562373095"),
//   BigDecimal("2.519842099789746")
// ]
```

#### Replacing null with another `BigDecimal`

```java
Stream
    .of(BigDecimal.valueOf(2), null, BigDecimal.valueOf(50))
    .gather(Gatherers4j.runningGeometricMean().treatNullAs(BigDecimal.valueOf(8)))
    .toList();

// [  
//   BigDecimal("2"),  
//   BigDecimal("4.0"),  
//   BigDecimal("20.0") 
// ]
```

#### Specifying a new `MathContext`

```java
Stream
    .of("2.0")
    .map(BigDecimal::new)
    .gather(Gatherers4j
        .runningGeometricMean()
        .withMathContext(new MathContext(2))
    )
    .toList();

// [ 
//   BigDecimal("2.0") 
// ]
```

#### Emitting a record containing the original and calculated values

```java
Stream
    .of("2.0", "8.0")
    .map(BigDecimal::new)
    .gather(Gatherers4j.runningGeometricMean().withOriginal())
    .toList();

// [ 
//   WithOriginal[original=2.0, calculated=2.0],
//   WithOriginal[original=8.0, calculated=4.0]
// ]
```
