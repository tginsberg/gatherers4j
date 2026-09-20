---
title: "runningStandardDeviation()"
linkTitle: "runningStandardDeviation()"
show_in_table: true
category: "Mathematical Operations"
description: Calculate the running standard deviation of a `Stream<BigDecimal>`, as either a population or a sample.
aliases:
  - /gatherers/mathematical/runningpopulationstandarddeviation/
  - /gatherers/mathematical/runningsamplestandarddeviation/

---

### Implementation Notes
This implementation is suitable for `Stream<BigDecimal>`, for a version that takes a user-specified mapping function see [`runningStandardDeviationBy()`](/gatherers4j/gatherers/mathematical/runningstandarddeviationby/).
By default, nulls are ignored and play no part in calculations, see `treatNullAs()` and `treatNullAsZero()` below for ways to change this behavior. The default `MathContext`
for all calculations is {{< jdklink linkName="MathContext.DECIMAL64" package="java.base/java/math/MathContext.html#DECIMAL64" >}}, but this can be overridden (see `withMathContext()`, below).

Use `Dataset.Population` when the elements are the entire dataset (the variance is divided by *n*), and `Dataset.Sample` when they are a sample of a larger population (the variance is divided by *n - 1*). The sample standard deviation of a single value is `0`.


**Signatures**

`runningStandardDeviation(Dataset dataset)`
* `dataset` - Either `Population` or `Sample`

**Additional Methods**

| Method                                     | Purpose                                                                                                                                                                                                                                                                                                              |
|--------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `treatNullAsZero()`                        | When encountering a `null` value in a stream, treat it as `BigDecimal.ZERO` instead. [See example.](#treating-null-as-zero)                                                                                                                                                                                          |
| `treatNullAs(BigDecimal replacement)`      | When encountering a `null` value in a stream, treat it as the given `replacement` value instead. [See example.](#replacing-null-with-another-bigdecimal)                                                                                                                                                             |
| `treatNullAsOne()`                         | When encountering a `null` value in a stream, treat it as `BigDecimal.ONE` instead.                                                                                                                                                                                                                                  |
| `withMathContext(MathContext mathContext)` | Replace the `MathContext` used for all mathematical operations performed by this gatherer. [See example.](#specifying-a-new-mathcontext)                                                                                                                                                                             |
| `withOriginal()`                           | Include the original input value from the stream in addition to the calculated value in a [`WithOriginal`](https://github.com/tginsberg/gatherers4j/blob/main/src/main/java/com/ginsberg/gatherers4j/dto/WithOriginal.java) record. [See example.](#emitting-a-record-containing-the-original-and-calculated-values) |

### Examples

#### Running Standard Deviation (Population)

```java
Stream
    .of("1.0", "2.0", "10.0")
    .map(BigDecimal::new)
    .gather(Gatherers4j.runningStandardDeviation(Dataset.Population))
    .toList();

// [ 
//   BigDecimal("0.0"), 
//   BigDecimal("0.5"), 
//   BigDecimal("4.02768199119819")
// ]
```

#### Running Standard Deviation (Sample)

```java
Stream
    .of("1.0", "2.0", "10.0")
    .map(BigDecimal::new)
    .gather(Gatherers4j.runningStandardDeviation(Dataset.Sample))
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
    .gather(Gatherers4j.runningStandardDeviation(Dataset.Population))
    .toList();

// [ 
//   BigDecimal("0.0"), 
//   BigDecimal("0.5"), 
//   BigDecimal("4.02768199119819")
// ]
```

#### Treating null as zero

```java
Stream
    .of(null, null, new BigDecimal("10.0"), new BigDecimal("2.0"))
    .gather(Gatherers4j.runningStandardDeviation(Dataset.Population).treatNullAsZero())
    .toList();

// [ 
//   BigDecimal("0"),
//   BigDecimal("0"),
//   BigDecimal("4.714045207910317"), 
//   BigDecimal("4.123105625617661") 
// ]
```

#### Replacing null with another `BigDecimal`

```java
Stream
    .of(null, null, new BigDecimal("10.0"), new BigDecimal("2.0"))
    .gather(Gatherers4j.runningStandardDeviation(Dataset.Population).treatNullAs(new BigDecimal("3.5")))
    .toList();

// [  
//   BigDecimal("3.5"),  
//   BigDecimal("3.5"),  
//   BigDecimal("3.064129385141706"), 
//   BigDecimal("3.092329219213245") 
// ]
```

#### Specifying a new `MathContext`

```java
Stream
    .of("1.0", "2.0", "10.0", "2.0")
    .map(BigDecimal::new)
    .gather(Gatherers4j
        .runningStandardDeviation(Dataset.Population)
        .withMathContext(new MathContext(3, RoundingMode.DOWN))
    )
    .toList();

// [ 
//   BigDecimal("0.0"), 
//   BigDecimal("0.5"), 
//   BigDecimal("4.02"), 
//   BigDecimal("3.61") 
// ]
```

#### Emitting a record containing the original and calculated values

```java
Stream
    .of("1.0", "2.0", "10.0", "2.0")
    .map(BigDecimal::new)
    .gather(Gatherers4j.runningStandardDeviation(Dataset.Population).withOriginal())
    .toList();

// [ 
//   WithOriginal[original=1.0, calculated=0.0]
//   WithOriginal[original=2.0, calculated=0.5]
//   WithOriginal[original=10.0, calculated=4.02768199119819]
//   WithOriginal[original=2.0, calculated=3.631459761583488]
// ]
```
