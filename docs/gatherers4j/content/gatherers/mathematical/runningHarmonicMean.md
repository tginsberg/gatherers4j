---
title: "runningHarmonicMean()"
linkTitle: "runningHarmonicMean()"
show_in_table: true
category: "Mathematical Operations"
description: Calculate the running harmonic mean of a `Stream<BigDecimal>`.

---

### Implementation Notes
This implementation is suitable for `Stream<BigDecimal>`. For a version that takes a user-specified mapping function, see [`runningHarmonicMeanBy()`](/gatherers4j/gatherers/mathematical/runningharmonicmeanby/).
By default, nulls are ignored and play no part in calculations, see `treatNullAs()` and `treatNullAsOne()` below for ways to change this behavior. The default `MathContext`
for all calculations is {{< jdklink linkName="MathContext.DECIMAL64" package="java.base/java/math/MathContext.html#DECIMAL64" >}}, but this can be overridden (see `withMathContext()`, below).

A `0` anywhere in the stream is undefined for a harmonic mean and will throw an `ArithmeticException`.

**Signatures**

`runningHarmonicMean()`

**Additional Methods**

| Method                                     | Purpose                                                                                                                                                                                                                                                                                                              |
|--------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `treatNullAsOne()`                         | When encountering a `null` value in a stream, treat it as `BigDecimal.ONE` instead. [See example.](#treating-null-as-one)                                                                                                                                                                                            |
| `treatNullAs(BigDecimal replacement)`      | When encountering a `null` value in a stream, treat it as the given `replacement` value instead. [See example.](#replacing-null-with-another-bigdecimal)                                                                                                                                                             |
| `withMathContext(MathContext mathContext)` | Replace the `MathContext` used for all mathematical operations performed by this gatherer. [See example.](#specifying-a-new-mathcontext)                                                                                                                                                                             |
| `withOriginal()`                           | Include the original input value from the stream in addition to the calculated value in a [`WithOriginal`](https://github.com/tginsberg/gatherers4j/blob/main/src/main/java/com/ginsberg/gatherers4j/dto/WithOriginal.java) record. [See example.](#emitting-a-record-containing-the-original-and-calculated-values) |

Note: `treatNullAsZero()` is also a valid method on this gatherer, but it only makes sense in a very narrow set of circumstances.


### Examples

#### Running harmonic mean

```java
Stream
    .of("1", "2", "4")
    .map(BigDecimal::new)
    .gather(Gatherers4j.runningHarmonicMean())
    .toList();

// [ 
//   BigDecimal("1"), 
//   BigDecimal("1.333333333333333"),
//   BigDecimal("1.714285714285714")
// ]
```

#### Showing nulls are ignored by default

```java
Stream
    .of(BigDecimal.valueOf(2), null, BigDecimal.valueOf(8))
    .gather(Gatherers4j.runningHarmonicMean())
    .toList();

// [ 
//   BigDecimal("2"),
//   BigDecimal("3.2")
// ]
```

#### Treating null as one

```java
Stream
    .of(BigDecimal.valueOf(2), null, BigDecimal.valueOf(8))
    .gather(Gatherers4j.runningHarmonicMean().treatNullAsOne())
    .toList();

// [ 
//   BigDecimal("2"),
//   BigDecimal("1.333333333333333"),
//   BigDecimal("1.846153846153846")
// ]
```

#### Replacing null with another `BigDecimal`

```java
Stream
    .of(BigDecimal.valueOf(2), null, BigDecimal.valueOf(50))
    .gather(Gatherers4j.runningHarmonicMean().treatNullAs(BigDecimal.valueOf(8)))
    .toList();

// [  
//   BigDecimal("2"),  
//   BigDecimal("3.2"),  
//   BigDecimal("4.651162790697674") 
// ]
```

#### Specifying a new `MathContext`

```java
Stream
    .of("1", "3")
    .map(BigDecimal::new)
    .gather(Gatherers4j
        .runningHarmonicMean()
        .withMathContext(new MathContext(2))
    )
    .toList();

// [ 
//   BigDecimal("1"), 
//   BigDecimal("1.5") 
// ]
```

#### Emitting a record containing the original and calculated values

```java
Stream
    .of("1", "2", "4")
    .map(BigDecimal::new)
    .gather(Gatherers4j.runningHarmonicMean().withOriginal())
    .toList();

// [ 
//   WithOriginal[original=1, calculated=1],
//   WithOriginal[original=2, calculated=1.333333333333333],
//   WithOriginal[original=4, calculated=1.714285714285714]
// ]
```
