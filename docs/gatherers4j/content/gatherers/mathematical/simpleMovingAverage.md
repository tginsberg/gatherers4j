---
title: "simpleMovingAverage()"
linkTitle: "simpleMovingAverage()"
show_in_table: true
category: "Mathematical Operations"
description: Calculate the simple moving average of `BigDecimal` values over the previous `windowSize` number of values.

---

### Implementation Notes
This implementation is suitable for `Stream<BigDecimal>`, for a version that takes user-specified mapping function see [`simpleMovingAverageBy()`](/gatherers4j/gatherers/mathematical/simplemovingaverageby/).
By default, nulls are ignored and play no part in calculations, see `treatNullAs()` and `treatNullAsZero()` below for ways to change this behavior. The default `MathContext`
for all calculations is {{< jdklink linkName="MathContext.DECIMAL64" package="java.base/java/math/MathContext.html#DECIMAL64" >}}, but this can be overridden (see `withMathContext()`, below).


**Signatures**

`simpleMovingAverage(int windowSize)`
* `windowSize` - How many trailing elements to average over at any given point in the stream

**Additional Methods**

| Method                                     | Purpose                                                                                                                                                                                                                                                                                                           |
|--------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `includePartialValues()`                   | When calculating the moving average and the full size of the window has not yet been reached, the gatherer should emit averages for what it has. [See example.](#including-partial-values)                                                                                                                        |
| `treatNullAsZero()`                        | When encountering a `null` value in a stream, treat it as `BigDecimal.ZERO` instead. [See example.](#treating-null-as-zero)                                                                                                                                                                                       |
| `treatNullAs(BigDecimal replacement)`      | When encountering a `null` value in a stream, treat it as the given `replacement` value instead. [See example.](#replacing-null-with-another-bigdecimal)                                                                                                                                                          |
| `withMathContext(MathContext mathContext)` | Replace the `MathContext` used for all mathematical operations performed by this gatherer. [See example.](#specifying-a-new-mathcontext)                                                                                                                                                                          |
| `withOriginal()`                           | Include the original input value from the stream in addition to the calculated value in a [`WithOriginal`](https://github.com/tginsberg/gatherers4j/blob/main/src/main/java/com/ginsberg/gatherers4j/dto/WithOriginal.java)record. [See example.](#emitting-a-record-containing-the-original-and-calculated-values) |

### Examples

#### Simple moving average of window size 3

```java
Stream
    .of("1.0", "2.0", "10.0", "2.0")
    .map(BigDecimal::new)
    .gather(Gatherers4j.simpleMovingAverage(3))
    .toList();

// [ 
//   BigDecimal("4.333333333333333"), 
//   BigDecimal("4.666666666666667") 
// ]
```

#### Including partial values

Showing that an in-process average is emitted for each element, even if there aren't 3 elements from which to calculate an average yet.

```java
Stream
    .of("1.0", "2.0", "10.0", "2.0")
    .map(BigDecimal::new)
    .gather(Gatherers4j.simpleMovingAverage(3).includePartialValues())
    .toList();

// [ 
//   BigDecimal("1.0"), 
//   BigDecimal("1.5"), 
//   BigDecimal("4.333333333333333"), 
//   BigDecimal("4.666666666666667") 
// ]
```


#### Showing nulls are ignored by default

```java
Stream
    .of(null, null, new BigDecimal("10.0"), new BigDecimal("2.0"), new BigDecimal("1.0"))
    .gather(Gatherers4j.simpleMovingAverage(3)))
    .toList();

// [ 
//   BigDecimal("4.333333333333333")
// ]
```

#### Treating null as zero

```java
Stream
    .of(null, null, new BigDecimal("10.0"), new BigDecimal("2.0"))
    .gather(Gatherers4j.simpleMovingAverage(3).treatNullAsZero())
    .toList();

// [ 
//   BigDecimal("3.333333333333333"), 
//   BigDecimal("4.0") 
// ]
```

#### Replacing null with another `BigDecimal`

```java
Stream
    .of(null, null, new BigDecimal("10.0"), new BigDecimal("2.0"))
    .gather(Gatherers4j.simpleMovingAverage(3).treatNullAs(new BigDecimal("3.5")))
    .toList();

// [ 
//   BigDecimal("5.666666666666667"), 
//   BigDecimal("5.166666666666667") 
// ]
```

#### Specifying a new `MathContext`

```java
Stream
    .of("1.0", "2.0", "10.0", "2.0")
    .map(BigDecimal::new)
    .gather(Gatherers4j
        .simpleMovingAverage(3)
        .withMathContext(new MathContext(3, RoundingMode.DOWN))
    )
    .toList();

// [ 
//   BigDecimal("4.33"), 
//   BigDecimal("4.66") 
// ]
```

#### Emitting a record containing the original and calculated values

```java
Stream
    .of("1.0", "2.0", "10.0", "2.0")
    .map(BigDecimal::new)
    .gather(Gatherers4j.simpleMovingAverage(3).withOriginal())
    .toList();

// [ 
//   WithOriginal[original=10.0, calculated=4.333333333333333]
//   WithOriginal[original=2.0, calculated=4.666666666666667]
// ]
```