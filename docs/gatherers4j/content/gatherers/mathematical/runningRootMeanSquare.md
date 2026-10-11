---
title: "runningRootMeanSquare()"
linkTitle: "runningRootMeanSquare()"
show_in_table: true
category: "Mathematical Operations"
description: Calculate the running root mean square (quadratic mean) of a `Stream<BigDecimal>`.

---

### Implementation Notes
This implementation is suitable for `Stream<BigDecimal>`. For a version that takes a user-specified mapping function, see [`runningRootMeanSquareBy()`](/gatherers4j/gatherers/mathematical/runningrootmeansquareby/).
By default, nulls are ignored and play no part in calculations, see `treatNullAs()` and `treatNullAsOne()` below for ways to change this behavior. The default `MathContext`
for all calculations is {{< jdklink linkName="MathContext.DECIMAL64" package="java.base/java/math/MathContext.html#DECIMAL64" >}}, but this can be overridden (see `withMathContext()`, below).

**Signatures**

`runningRootMeanSquare()`

**Additional Methods**

| Method                                     | Purpose                                                                                                                                                                                                                                                                                                              |
|--------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `treatNullAsZero()`                        | When encountering a `null` value in a stream, treat it as `BigDecimal.ZERO` instead. [See example.](#treating-null-as-zero)                                                                                                                                                                                          |
| `treatNullAs(BigDecimal replacement)`      | When encountering a `null` value in a stream, treat it as the given `replacement` value instead. [See example.](#replacing-null-with-another-bigdecimal)                                                                                                                                                             |
| `treatNullAsOne()`                         | When encountering a `null` value in a stream, treat it as `BigDecimal.ONE` instead. [See example.](#treating-null-as-one)                                                                                                                                                                                            |
| `withMathContext(MathContext mathContext)` | Replace the `MathContext` used for all mathematical operations performed by this gatherer. [See example.](#specifying-a-new-mathcontext)                                                                                                                                                                             |
| `withOriginal()`                           | Include the original input value from the stream in addition to the calculated value in a [`WithOriginal`](https://github.com/tginsberg/gatherers4j/blob/main/src/main/java/com/ginsberg/gatherers4j/dto/WithOriginal.java) record. [See example.](#emitting-a-record-containing-the-original-and-calculated-values) |


### Examples

#### Running root mean square

```java
Stream
    .of("1", "2", "4")
    .map(BigDecimal::new)
    .gather(Gatherers4j.runningRootMeanSquare())
    .toList();

// [ 
//   BigDecimal("1"), 
//   BigDecimal("1.58113883008419"),
//   BigDecimal("2.645751311064591")
// ]
```

#### Showing nulls are ignored by default

```java
Stream
    .of(BigDecimal.ONE, null, BigDecimal.TWO)
    .gather(Gatherers4j.runningRootMeanSquare())
    .toList();

// [ 
//   BigDecimal("1"),
//   BigDecimal("1.58113883008419")
// ]
```

#### Treating null as zero

```java
Stream
    .of(null, BigDecimal.ONE, null, BigDecimal.ONE)
    .gather(Gatherers4j.runningRootMeanSquare().treatNullAsZero())
    .toList();

// [  
//   BigDecimal("0"),  
//   BigDecimal("0.7071067811865475"),  
//   BigDecimal("0.5773502691896257"),
//   BigDecimal("0.7071067811865475")
// ]
```

#### Treating null as one

```java
Stream
    .of(BigDecimal.valueOf(2), null, BigDecimal.valueOf(8))
    .gather(Gatherers4j.runningRootMeanSquare().treatNullAsOne())
    .toList();

// [ 
//   BigDecimal("2"),
//   BigDecimal("1.58113883008419"),
//   BigDecimal("4.79583152331272")
// ]
```

#### Replacing null with another `BigDecimal`

```java
Stream
    .of(null, BigDecimal.ONE, null, BigDecimal.ONE)
    .gather(Gatherers4j.runningRootMeanSquare().treatNullAs(BigDecimal.TEN))
    .toList();

// [  
//   BigDecimal("10"),  
//   BigDecimal("7.106335201775948"),  
//   BigDecimal("8.18535277187245"),
//   BigDecimal("7.106335201775948")
// ]
```

#### Specifying a new `MathContext`

```java
Stream
    .of("1", "2", "4")
    .map(BigDecimal::new)
    .gather(Gatherers4j
        .runningRootMeanSquare()
        .withMathContext(new MathContext(3))
    )
    .toList();

// [ 
//   BigDecimal("1"), 
//   BigDecimal("1.58"),
//   BigDecimal("2.65")
// ]
```

#### Emitting a record containing the original and calculated values

```java
Stream
    .of("1", "2", "4")
    .map(BigDecimal::new)
    .gather(Gatherers4j.runningRootMeanSquare().withOriginal())
    .toList();

// [ 
//   WithOriginal[original=1, calculated=1],
//   WithOriginal[original=2, calculated=1.58113883008419],
//   WithOriginal[original=4, calculated=2.645751311064591]
// ]
```
