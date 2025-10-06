---
title: "runningMedian()"
linkTitle: "runningMedian()"
show_in_table: true
category: "Mathematical Operations"
description: Calculate the running median of a `Stream<BigDecimal>`.

---

### Implementation Notes
This implementation is suitable for `Stream<BigDecimal>`, for a version that takes user-specified mapping function see [`runningMedianBy()`](/gatherers4j/gatherers/mathematical/runningmedianby/).
By default, nulls are ignored and play no part in calculations, see `treatNullAs()` and `treatNullAsZero()` below for ways to change this behavior. The default `MathContext`
for all calculations is {{< jdklink linkName="MathContext.DECIMAL64" package="java.base/java/math/MathContext.html#DECIMAL64" >}}, but this can be overridden (see `withMathContext()`, below).


**Signatures**

`runningMedian()`

**Additional Methods**

| Method                                     | Purpose                                                                                                                                                                                                                                                                                                             |
|--------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `treatNullAsZero()`                        | When encountering a `null` value in a stream, treat it as `BigDecimal.ZERO` instead. [See example.](#treating-null-as-zero)                                                                                                                                                                                         |
| `treatNullAs(BigDecimal replacement)`      | When encountering a `null` value in a stream, treat it as the given `replacement` value instead. [See example.](#replacing-null-with-another-bigdecimal)                                                                                                                                                            |
| `withMathContext(MathContext mathContext)` | Replace the `MathContext` used for all mathematical operations performed by this gatherer. [See example.](#specifying-a-new-mathcontext)                                                                                                                                                                            |
| `withOriginal()`                           | Include the original input value from the stream in addition to the calculated value in a [`WithOriginal`](https://github.com/tginsberg/gatherers4j/blob/main/src/main/java/com/ginsberg/gatherers4j/dto/WithOriginal.java)record. [See example.](#emitting-a-record-containing-the-original-and-calculated-values) |

### Examples

#### Running median

```java
Stream
    .of("1.1", "2.2", "3.3")
    .map(BigDecimal::new)
    .gather(Gatherers4j.runningMedian())
    .toList();

// [ 
//   BigDecimal("1.1"), 
//   BigDecimal("1.65"), 
//   BigDecimal("2.2")
// ]
```

#### Showing nulls are ignored by default

```java
Stream
    .of(null, null, new BigDecimal("10.0"), new BigDecimal("2.42"), new BigDecimal("1.234"))
    .gather(Gatherers4j.runningMedian())
    .toList();

// [ 
//   BigDecimal("10.0"),
//   BigDecimal("6.21"),
//   BigDecimal("2.42")
// ]
```

#### Treating null as zero

```java
Stream
    .of(null, null, new BigDecimal("10.0"), new BigDecimal("2.0"))
    .gather(Gatherers4j.runningMedian().treatNullAsZero())
    .toList();

// [ 
//   BigDecimal("0"),
//   BigDecimal("0"),
//   BigDecimal("0"), 
//   BigDecimal("1.0") 
// ]
```

#### Replacing null with another `BigDecimal`

```java
Stream
    .of(null, null, new BigDecimal("1.0"), new BigDecimal("2.0"))
    .gather(Gatherers4j.runningMedian().treatNullAs(new BigDecimal("3.5")))
    .toList();

// [  
//   BigDecimal("3.5"),  
//   BigDecimal("3.5"),  
//   BigDecimal("3.5"), 
//   BigDecimal("2.75")
// ]
```

#### Specifying a new `MathContext`

```java
Stream
    .of("1.1", "2.3", "10.4", "2.5")
    .map(BigDecimal::new)
    .gather(Gatherers4j
        .runningMedian()
        .withMathContext(new MathContext(3, RoundingMode.DOWN))
    )
    .toList();

// [ 
//   BigDecimal("1.1"), 
//   BigDecimal("1.7"), 
//   BigDecimal("2.3"), 
//   BigDecimal("2.4") 
// ]
```

#### Emitting a record containing the original and calculated values

```java
Stream
    .of("1.0", "2.0", "10.0", "2.0")
    .map(BigDecimal::new)
    .gather(Gatherers4j.runningMedian().withOriginal())
    .toList();

// [ 
//   WithOriginal[original=1.0, calculated=1.0], 
//   WithOriginal[original=2.0, calculated=1.5], 
//   WithOriginal[original=10.0, calculated=2.0], 
//   WithOriginal[original=2.0, calculated=2.0]
// ]
```