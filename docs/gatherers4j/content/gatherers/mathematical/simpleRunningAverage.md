---
title: "simpleRunningAverage()"
linkTitle: "simpleRunningAverage()"
show_in_table: true
category: "Mathematical Operations"
description: Calculate the simple running average of a `Stream<BigDecimal>`.

---

### Implementation Notes
This implementation is suitable for `Stream<BigDecimal>`, for a version that takes user-specified mapping function see [`simpleRunningAverageBy()`](/gatherers4j/gatherers/mathematical/simplerunningaverageby/).
By default, nulls are ignored and play no part in calculations, see `treatNullAs()` and `treatNullAsZero()` below for ways to change this behavior. The default `MathContext`
for all calculations is {{< jdklink linkName="MathContext.DECIMAL64" package="java.base/java/math/MathContext.html#DECIMAL64" >}}, but this can be overridden (see `withMathContext()`, below).


**Signatures**

`simpleRunningAverage()`

**Additional Methods**

| Method                                     | Purpose                                                                                                                                                                                                                                                |
|--------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `treatNullAsZero()`                        | When encountering a `null` value in a stream, treat it as `BigDecimal.ZERO` instead. [See example.](#treating-null-as-zero)                                                                                                                                             |
| `treatNullAs(BigDecimal replacement)`      | When encountering a `null` value in a stream, treat it as the given `replacement` value instead. [See example.](#replacing-null-with-another-bigdecimal)                                                                                                                                 |
| `withMathContext(MathContext mathContext)` | Replace the `MathContext` used for all mathematical operations performed by this gatherer. [See example.](#specifying-a-new-mathcontext)                                                                                                                                       |
| `withOriginal()`                           | Include the original input value from the stream in addition to the calculated value in a [`WithOriginal`](https://github.com/tginsberg/gatherers4j/blob/main/src/main/java/com/ginsberg/gatherers4j/dto/WithOriginal.java)record. [See example.](#emitting-a-record-containing-the-original-and-calculated-values) |

### Examples

#### Simple running average

```java
Stream
    .of("1.0", "2.0", "3.0")
    .map(BigDecimal::new)
    .gather(Gatherers4j.simpleRunningAverage())
    .toList();

// [ 
//   BigDecimal("1.0"), 
//   BigDecimal("1.5"), 
//   BigDecimal("2.0")
// ]
```

#### Showing nulls are ignored by default

```java
Stream
    .of(null, null, new BigDecimal("10.0"), new BigDecimal("2.0"), new BigDecimal("1.0"))
    .gather(Gatherers4j.simpleRunningAverage()))
    .toList();

// [ 
//   BigDecimal("10.0"),
//   BigDecimal("6.0"),
//   BigDecimal("4.333333333333333")
// ]
```

#### Treating null as zero

```java
Stream
    .of(null, null, new BigDecimal("10.0"), new BigDecimal("2.0"))
    .gather(Gatherers4j.simpleRunningAverage().treatNullAsZero())
    .toList();

// [ 
//   BigDecimal("0"),
//   BigDecimal("0"),
//   BigDecimal("3.333333333333333"), 
//   BigDecimal("2.9999999999999998"), 
//   BigDecimal("2.5999999999999998") 
// ]
```

#### Replacing null with another `BigDecimal`

```java
Stream
    .of(null, null, new BigDecimal("10.0"), new BigDecimal("2.0"))
    .gather(Gatherers4j.simpleRunningAverage().treatNullAs(new BigDecimal("3.5")))
    .toList();

// [  
//   BigDecimal("3.5"),  
//   BigDecimal("3.5"),  
//   BigDecimal("5.666666666666667"), 
//   BigDecimal("4.7500000000000002"), 
//   BigDecimal("4.0000000000000002") 
// ]
```

#### Specifying a new `MathContext`

```java
Stream
    .of("1.0", "2.0", "10.0", "2.0")
    .map(BigDecimal::new)
    .gather(Gatherers4j
        .simpleRunningAverage()
        .withMathContext(new MathContext(3, RoundingMode.DOWN))
    )
    .toList();

// [ 
//   BigDecimal("1.0"), 
//   BigDecimal("1.5"), 
//   BigDecimal("4.33"), 
//   BigDecimal("3.748") 
// ]
```

#### Emitting a record containing the original and calculated values

```java
Stream
    .of("1.0", "2.0", "10.0", "2.0")
    .map(BigDecimal::new)
    .gather(Gatherers4j.simpleRunningAverage().withOriginal())
    .toList();

// [ 
//   WithOriginal[original=1.0, calculated=1.0]
//   WithOriginal[original=2.0, calculated=1.5]
//   WithOriginal[original=10.0, calculated=4.333333333333333]
//   WithOriginal[original=2.0, calculated=3.7499999999999998]
// ]
```