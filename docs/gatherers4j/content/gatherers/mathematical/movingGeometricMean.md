---
title: "movingGeometricMean()"
linkTitle: "movingGeometricMean()"
show_in_table: true
category: "Mathematical Operations"
description: Calculate the moving geometric mean of a `Stream<BigDecimal>` looking back `windowSize` elements.

---

### Implementation Notes
This implementation is suitable for `Stream<BigDecimal>`, for a version that takes a user-specified mapping function see [`movingGeometricMeanBy()`](/gatherers4j/gatherers/mathematical/movinggeometricmeanby/).
By default, nulls are ignored and play no part in calculations, see `treatNullAs()` and `treatNullAsOne()` below for ways to change this behavior. The default `MathContext`
for all calculations is {{< jdklink linkName="MathContext.DECIMAL64" package="java.base/java/math/MathContext.html#DECIMAL64" >}}, but this can be overridden (see `withMathContext()`, below).


**Signatures**

`movingGeometricMean(int windowSize)`
* `windowSize` - How many trailing elements to calculate the geometric mean over

**Additional Methods**

| Method                                     | Purpose                                                                                                                                                                                                                                                                                                              |
|--------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `excludePartialValues()`                   | When calculating the moving geometric mean, and the full size of the window has not yet been reached, the gatherer should suppress emitting values until the lookback window is full. [See example.](#excluding-partial-values)                                                                                      |
| `treatNullAsOne()`                         | When encountering a `null` value in a stream, treat it as `BigDecimal.ONE` instead. [See example.](#treating-null-as-one)                                                                                                                                                                                            |
| `treatNullAs(BigDecimal replacement)`      | When encountering a `null` value in a stream, treat it as the given `replacement` value instead. [See example.](#replacing-null-with-another-bigdecimal)                                                                                                                                                             |
| `withMathContext(MathContext mathContext)` | Replace the `MathContext` used for all mathematical operations performed by this gatherer. [See example.](#specifying-a-new-mathcontext)                                                                                                                                                                             |
| `withOriginal()`                           | Include the original input value from the stream in addition to the calculated value in a [`WithOriginal`](https://github.com/tginsberg/gatherers4j/blob/main/src/main/java/com/ginsberg/gatherers4j/dto/WithOriginal.java) record. [See example.](#emitting-a-record-containing-the-original-and-calculated-values) |

Note: `treatNullAsZero()` is also a valid method on this gatherer, but it only makes sense in a very narrow set of circumstances.

### Examples

#### Moving geometric mean of window size 3

```java
Stream
    .of("1", "8", "64", "1")
    .map(BigDecimal::new)
    .gather(Gatherers4j.movingGeometricMean(3))
    .toList();

// [ 
//   BigDecimal("1"), 
//   BigDecimal("2.828427124746190"), 
//   BigDecimal("8"), 
//   BigDecimal("8") 
// ]
```

#### Excluding partial values

Showing that in-process moving geometric mean values are not emitted for each element until the lookback window has been filled.

```java
Stream
    .of("1", "8", "64", "1")
    .map(BigDecimal::new)
    .gather(Gatherers4j.movingGeometricMean(3).excludePartialValues())
    .toList();

// [ 
//   BigDecimal("8"), 
//   BigDecimal("8") 
// ]
```


#### Showing nulls are ignored by default

```java
Stream
    .of(null, null, new BigDecimal("1"), new BigDecimal("8"), new BigDecimal("64"))
    .gather(Gatherers4j.movingGeometricMean(3))
    .toList();

// [ 
//   BigDecimal("1"),
//   BigDecimal("2.828427124746190"),
//   BigDecimal("8")
// ]
```

#### Treating null as one

```java
Stream
    .of(null, null, new BigDecimal("8"), new BigDecimal("64"))
    .gather(Gatherers4j.movingGeometricMean(3).treatNullAsOne())
    .toList();

// [ 
//   BigDecimal("1"), 
//   BigDecimal("1"), 
//   BigDecimal("2"), 
//   BigDecimal("8") 
// ]
```

#### Replacing null with another `BigDecimal`

```java
Stream
    .of(null, null, new BigDecimal("8"), new BigDecimal("64"))
    .gather(Gatherers4j.movingGeometricMean(3).treatNullAs(new BigDecimal("27")))
    .toList();

// [ 
//   BigDecimal("27"), 
//   BigDecimal("27"), 
//   BigDecimal("18"), 
//   BigDecimal("12") 
// ]
```

#### Specifying a new `MathContext`

```java
Stream
    .of("1", "8", "64")
    .map(BigDecimal::new)
    .gather(Gatherers4j
        .movingGeometricMean(3)
        .withMathContext(new MathContext(3))
    )
    .toList();

// [ 
//   BigDecimal("1.00"), 
//   BigDecimal("2.83"), 
//   BigDecimal("8.00") 
// ]
```

#### Emitting a record containing the original and calculated values

```java
Stream
    .of("1", "8", "64")
    .map(BigDecimal::new)
    .gather(Gatherers4j.movingGeometricMean(3).withOriginal())
    .toList();

// [
//   WithOriginal[original=1, calculated=1], 
//   WithOriginal[original=8, calculated=2.828427124746190], 
//   WithOriginal[original=64, calculated=8]
// ]

```
