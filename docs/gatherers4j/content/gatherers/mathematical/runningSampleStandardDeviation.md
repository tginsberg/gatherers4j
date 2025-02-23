---
title: "runningSampleStandardDeviation()"
linkTitle: "runningSampleStandardDeviation()"
show_in_table: true
category: "Mathematical Operations"
description: Calculate the running sample standard deviation of a `Stream<BigDecimal>`.

---

### Implementation Notes
This implementation is suitable for `Stream<BigDecimal>`, for a version that takes user-specified mapping function see [`runningSampleStandardDeviationBy()`](/gatherers4j/gatherers/mathematical/runningsamplestandarddeviationby/).
By default, nulls are ignored and play no part in calculations, see `treatNullAs()` and `treatNullAsZero()` below for ways to change this behavior. The default `MathContext`
for all calculations is {{< jdklink linkName="MathContext.DECIMAL64" package="java.base/java/math/MathContext.html#DECIMAL64" >}}, but this can be overridden (see `withMathContext()`, below).


**Signatures**

`runningSampleStandardDeviation()`

**Additional Methods**

| Method                                     | Purpose                                                                                                                                                                                                                                                |
|--------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `treatNullAsZero()`                        | When encountering a `null` value in a stream, treat it as `BigDecimal.ZERO` instead. [See example.](#treating-null-as-zero)                                                                                                                                             |
| `treatNullAs(BigDecimal replacement)`      | When encountering a `null` value in a stream, treat it as the given `replacement` value instead. [See example.](#replacing-null-with-another-bigdecimal)                                                                                                                                 |
| `withMathContext(MathContext mathContext)` | Replace the `MathContext` used for all mathematical operations performed by this gatherer. [See example.](#specifying-a-new-mathcontext)                                                                                                                                       |
| `withOriginal()`                           | Include the original input value from the stream in addition to the calculated value in a [`WithOriginal`](https://github.com/tginsberg/gatherers4j/blob/main/src/main/java/com/ginsberg/gatherers4j/WithOriginal.java)record. [See example.](#emitting-a-record-containing-the-original-and-calculated-values) |

### Examples

#### Running Standard Deviation (Sample)

```java
Stream
    .of("1.0", "2.0", "10.0")
    .map(BigDecimal::new)
    .gather(Gatherers4j.runningSampleStandardDeviation())
    .toList();

// [ 
//   BigDecimal("0.0"), 
//   BigDecimal("0.7071067811865475"), 
//   BigDecimal("4.932882862316247")
// ]
```

#### Showing nulls are ignored by default

```java
Stream
    .of(null, null, new BigDecimal("10.0"), new BigDecimal("2.0"), new BigDecimal("1.0"))
    .gather(Gatherers4j.runningSampleStandardDeviation())
    .toList();

// [ 
//   BigDecimal("0.0"), 
//   BigDecimal("5.65685424949238"), 
//   BigDecimal("4.932882862316247")
// ]
```

#### Treating null as zero

```java
Stream
    .of(null, null, new BigDecimal("10.0"), new BigDecimal("2.0"))
    .gather(Gatherers4j.runningSampleStandardDeviation().treatNullAsZero())
    .toList();

// [ 
//   BigDecimal("0"),
//   BigDecimal("0"),
//   BigDecimal("5.773502691896258"), 
//   BigDecimal("4.760952285695234") 
// ]
```

#### Replacing null with another `BigDecimal`

```java
Stream
    .of(null, null, new BigDecimal("10.0"), new BigDecimal("2.0"))
    .gather(Gatherers4j.runningSampleStandardDeviation().treatNullAs(new BigDecimal("3.5")))
    .toList();

// [  
//   BigDecimal("0"),  
//   BigDecimal("0.0"),  
//   BigDecimal("3.752776749732567"), 
//   BigDecimal("3.570714214271425") 
// ]
```

#### Specifying a new `MathContext`

```java
Stream
    .of("1.0", "2.0", "10.0", "2.0")
    .map(BigDecimal::new)
    .gather(Gatherers4j
        .runningSampleStandardDeviation()
        .withMathContext(new MathContext(3, RoundingMode.DOWN))
    )
    .toList();

// [ 
//   BigDecimal("0"), 
//   BigDecimal("0.707), 
//   BigDecimal("4.92"), 
//   BigDecimal("4.18") 
// ]
```

#### Emitting a record containing the original and calculated values

```java
Stream
    .of("1.0", "2.0", "10.0", "2.0")
    .map(BigDecimal::new)
    .gather(Gatherers4j.runningSampleStandardDeviation().withOriginal())
    .toList();

// [ 
//   WithOriginal[original=1.0, calculated=0]
//   WithOriginal[original=2.0, calculated=0.7071067811865475]
//   WithOriginal[original=10.0, calculated=4.932882862316247]
//   WithOriginal[original=2.0, calculated=4.193248541803041]
// ]
```