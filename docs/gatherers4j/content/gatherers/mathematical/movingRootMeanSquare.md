---
title: "movingRootMeanSquare()"
linkTitle: "movingRootMeanSquare()"
show_in_table: true
category: "Mathematical Operations"
description: Calculate the moving root mean square (quadratic mean) of a `Stream<BigDecimal>` looking back `windowSize` elements.

---

### Implementation Notes
This implementation is suitable for `Stream<BigDecimal>`, for a version that takes a user-specified mapping function see [`movingRootMeanSquareBy()`](/gatherers4j/gatherers/mathematical/movingrootmeansquareby/).
By default, nulls are ignored and play no part in calculations, see `treatNullAs()` and `treatNullAsOne()` below for ways to change this behavior. The default `MathContext`
for all calculations is {{< jdklink linkName="MathContext.DECIMAL64" package="java.base/java/math/MathContext.html#DECIMAL64" >}}, but this can be overridden (see `withMathContext()`, below).

**Signatures**

`movingRootMeanSquare(int windowSize)`
* `windowSize` - How many trailing elements to calculate the root mean square over

**Additional Methods**

| Method                                     | Purpose                                                                                                                                                                                                                                                                                                              |
|--------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `excludePartialValues()`                   | When calculating the moving root mean square, and the full size of the window has not yet been reached, the gatherer should suppress emitting values until the lookback window is full. [See example.](#excluding-partial-values)                                                                                    |
| `treatNullAsZero()`                        | When encountering a `null` value in a stream, treat it as `BigDecimal.ZERO` instead. [See example.](#treating-null-as-zero)                                                                                                                                                                                          |
| `treatNullAs(BigDecimal replacement)`      | When encountering a `null` value in a stream, treat it as the given `replacement` value instead. [See example.](#replacing-null-with-another-bigdecimal)                                                                                                                                                             |
| `treatNullAsOne()`                         | When encountering a `null` value in a stream, treat it as `BigDecimal.ONE` instead. [See example.](#treating-null-as-one)                                                                                                                                                                                            |
| `withMathContext(MathContext mathContext)` | Replace the `MathContext` used for all mathematical operations performed by this gatherer. [See example.](#specifying-a-new-mathcontext)                                                                                                                                                                             |
| `withOriginal()`                           | Include the original input value from the stream in addition to the calculated value in a [`WithOriginal`](https://github.com/tginsberg/gatherers4j/blob/main/src/main/java/com/ginsberg/gatherers4j/dto/WithOriginal.java) record. [See example.](#emitting-a-record-containing-the-original-and-calculated-values) |

### Examples

#### Moving root mean square of window size 2

```java
Stream
    .of("1", "2", "4", "8")
    .map(BigDecimal::new)
    .gather(Gatherers4j.movingRootMeanSquare(2))
    .toList();

// [ 
//   BigDecimal("1"), 
//   BigDecimal("1.58113883008419"), 
//   BigDecimal("3.162277660168379"), 
//   BigDecimal("6.324555320336759") 
// ]
```

#### Excluding partial values

Showing that in-process moving root mean square values are not emitted for each element until the lookback window has been filled.

```java
Stream
    .of("1", "2", "4", "8")
    .map(BigDecimal::new)
    .gather(Gatherers4j.movingRootMeanSquare(2).excludePartialValues())
    .toList();

// [ 
//   BigDecimal("1.58113883008419"), 
//   BigDecimal("3.162277660168379"), 
//   BigDecimal("6.324555320336759") 
// ]
```


#### Showing nulls are ignored by default

```java
Stream
    .of(null, new BigDecimal("1"), new BigDecimal("2"), new BigDecimal("4"))
    .gather(Gatherers4j.movingRootMeanSquare(2))
    .toList();

// [ 
//   BigDecimal("1"),
//   BigDecimal("1.58113883008419"),
//   BigDecimal("3.162277660168379")
// ]
```

#### Treating null as zero

```java
Stream
    .of(null, null, new BigDecimal("2"), new BigDecimal("8"))
    .gather(Gatherers4j.movingRootMeanSquare(2).treatNullAsZero())
    .toList();

// [ 
//   BigDecimal("0"), 
//   BigDecimal("0"), 
//   BigDecimal("1.414213562373095"), 
//   BigDecimal("5.8309518948453") 
// ]
```

#### Treating null as one

```java
Stream
    .of(new BigDecimal("2"), null, new BigDecimal("8"))
    .gather(Gatherers4j.movingRootMeanSquare(2).treatNullAsOne())
    .toList();

// [ 
//   BigDecimal("2"), 
//   BigDecimal("1.58113883008419"), 
//   BigDecimal("5.70087712549569") 
// ]
```

#### Replacing null with another `BigDecimal`

```java
Stream
    .of(null, null, new BigDecimal("2"), new BigDecimal("8"))
    .gather(Gatherers4j.movingRootMeanSquare(2).treatNullAs(new BigDecimal("16")))
    .toList();

// [ 
//   BigDecimal("16"), 
//   BigDecimal("16"), 
//   BigDecimal("11.40175425099138"), 
//   BigDecimal("5.8309518948453") 
// ]
```

#### Specifying a new `MathContext`

```java
Stream
    .of("1", "2", "4")
    .map(BigDecimal::new)
    .gather(Gatherers4j
        .movingRootMeanSquare(2)
        .withMathContext(new MathContext(3))
    )
    .toList();

// [ 
//   BigDecimal("1"), 
//   BigDecimal("1.58"), 
//   BigDecimal("3.16") 
// ]
```

#### Emitting a record containing the original and calculated values

```java
Stream
    .of("1", "2", "4")
    .map(BigDecimal::new)
    .gather(Gatherers4j.movingRootMeanSquare(2).withOriginal())
    .toList();

// [
//   WithOriginal[original=1, calculated=1], 
//   WithOriginal[original=2, calculated=1.58113883008419], 
//   WithOriginal[original=4, calculated=3.162277660168379]
// ]

```
