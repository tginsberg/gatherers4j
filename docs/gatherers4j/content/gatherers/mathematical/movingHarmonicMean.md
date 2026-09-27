---
title: "movingHarmonicMean()"
linkTitle: "movingHarmonicMean()"
show_in_table: true
category: "Mathematical Operations"
description: Calculate the moving harmonic mean of a `Stream<BigDecimal>` looking back `windowSize` elements.

---

### Implementation Notes
This implementation is suitable for `Stream<BigDecimal>`, for a version that takes a user-specified mapping function see [`movingHarmonicMeanBy()`](/gatherers4j/gatherers/mathematical/movingharmonicmeanby/).
By default, nulls are ignored and play no part in calculations, see `treatNullAs()` and `treatNullAsOne()` below for ways to change this behavior. The default `MathContext`
for all calculations is {{< jdklink linkName="MathContext.DECIMAL64" package="java.base/java/math/MathContext.html#DECIMAL64" >}}, but this can be overridden (see `withMathContext()`, below).

A `0` anywhere in the lookback window is undefined for a harmonic mean and will throw an `ArithmeticException`.

**Signatures**

`movingHarmonicMean(int windowSize)`
* `windowSize` - How many trailing elements to calculate the harmonic mean over

**Additional Methods**

| Method                                     | Purpose                                                                                                                                                                                                                                                                                                              |
|--------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `excludePartialValues()`                   | When calculating the moving harmonic mean, and the full size of the window has not yet been reached, the gatherer should suppress emitting values until the lookback window is full. [See example.](#excluding-partial-values)                                                                                       |
| `treatNullAsOne()`                         | When encountering a `null` value in a stream, treat it as `BigDecimal.ONE` instead. [See example.](#treating-null-as-one)                                                                                                                                                                                            |
| `treatNullAs(BigDecimal replacement)`      | When encountering a `null` value in a stream, treat it as the given `replacement` value instead. [See example.](#replacing-null-with-another-bigdecimal)                                                                                                                                                             |
| `withMathContext(MathContext mathContext)` | Replace the `MathContext` used for all mathematical operations performed by this gatherer. [See example.](#specifying-a-new-mathcontext)                                                                                                                                                                             |
| `withOriginal()`                           | Include the original input value from the stream in addition to the calculated value in a [`WithOriginal`](https://github.com/tginsberg/gatherers4j/blob/main/src/main/java/com/ginsberg/gatherers4j/dto/WithOriginal.java) record. [See example.](#emitting-a-record-containing-the-original-and-calculated-values) |

Note: `treatNullAsZero()` is also a valid method on this gatherer, but it only makes sense in a very narrow set of circumstances.

### Examples

#### Moving harmonic mean of window size 3

```java
Stream
    .of("1", "2", "4", "8")
    .map(BigDecimal::new)
    .gather(Gatherers4j.movingHarmonicMean(3))
    .toList();

// [ 
//   BigDecimal("1"), 
//   BigDecimal("1.333333333333333"), 
//   BigDecimal("1.714285714285714"), 
//   BigDecimal("3.428571428571429") 
// ]
```

#### Excluding partial values

Showing that in-process moving harmonic mean values are not emitted for each element until the lookback window has been filled.

```java
Stream
    .of("1", "2", "4", "8")
    .map(BigDecimal::new)
    .gather(Gatherers4j.movingHarmonicMean(3).excludePartialValues())
    .toList();

// [ 
//   BigDecimal("1.714285714285714"), 
//   BigDecimal("3.428571428571429") 
// ]
```


#### Showing nulls are ignored by default

```java
Stream
    .of(null, null, new BigDecimal("1"), new BigDecimal("2"), new BigDecimal("4"), new BigDecimal("8"))
    .gather(Gatherers4j.movingHarmonicMean(3))
    .toList();

// [ 
//   BigDecimal("1"),
//   BigDecimal("1.333333333333333"),
//   BigDecimal("1.714285714285714"),
//   BigDecimal("3.428571428571429")
// ]
```

#### Treating null as one

```java
Stream
    .of(null, null, new BigDecimal("2"), new BigDecimal("8"))
    .gather(Gatherers4j.movingHarmonicMean(3).treatNullAsOne())
    .toList();

// [ 
//   BigDecimal("1"), 
//   BigDecimal("1"), 
//   BigDecimal("1.2"), 
//   BigDecimal("1.846153846153846") 
// ]
```

#### Replacing null with another `BigDecimal`

```java
Stream
    .of(null, null, new BigDecimal("2"), new BigDecimal("8"))
    .gather(Gatherers4j.movingHarmonicMean(3).treatNullAs(new BigDecimal("16")))
    .toList();

// [ 
//   BigDecimal("16"), 
//   BigDecimal("16"), 
//   BigDecimal("4.8"), 
//   BigDecimal("4.363636363636364") 
// ]
```

#### Specifying a new `MathContext`

```java
Stream
    .of("1", "2", "4")
    .map(BigDecimal::new)
    .gather(Gatherers4j
        .movingHarmonicMean(3)
        .withMathContext(new MathContext(3))
    )
    .toList();

// [ 
//   BigDecimal("1"), 
//   BigDecimal("1.33"), 
//   BigDecimal("1.71") 
// ]
```

#### Emitting a record containing the original and calculated values

```java
Stream
    .of("1", "2", "4")
    .map(BigDecimal::new)
    .gather(Gatherers4j.movingHarmonicMean(3).withOriginal())
    .toList();

// [
//   WithOriginal[original=1, calculated=1], 
//   WithOriginal[original=2, calculated=1.333333333333333], 
//   WithOriginal[original=4, calculated=1.714285714285714]
// ]

```
