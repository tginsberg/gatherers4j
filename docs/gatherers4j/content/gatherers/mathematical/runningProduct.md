---
title: "runningProduct()"
linkTitle: "runningProduct()"
show_in_table: true
category: "Mathematical Operations"
description: Calculate the running product of a `Stream<BigDecimal>`.

---

### Implementation Notes
This implementation is suitable for `Stream<BigDecimal>`, for a version that takes user-specified mapping function see [`runningProductBy()`](/gatherers4j/gatherers/mathematical/runningproductby/).
By default, nulls are ignored and play no part in calculations, see `treatNullAs()` and `treatNullAsOne()` below for ways to change this behavior. The default `MathContext`
for all calculations is {{< jdklink linkName="MathContext.DECIMAL64" package="java.base/java/math/MathContext.html#DECIMAL64" >}}, but this can be overridden (see `withMathContext()`, below).


**Signatures**

`runningProduct()`

**Additional Methods**

| Method                                     | Purpose                                                                                                                                                                                                                                                                                                           |
|--------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `treatNullAsOne()`                         | When encountering a `null` value in a stream, treat it as `BigDecimal.ONE` instead. [See example.](#treating-null-as-one)                                                                                                                                                                                         |
| `treatNullAs(BigDecimal replacement)`      | When encountering a `null` value in a stream, treat it as the given `replacement` value instead. [See example.](#replacing-null-with-another-bigdecimal)                                                                                                                                                          |
| `withMathContext(MathContext mathContext)` | Replace the `MathContext` used for all mathematical operations performed by this gatherer. [See example.](#specifying-a-new-mathcontext)                                                                                                                                                                          |
| `withOriginal()`                           | Include the original input value from the stream in addition to the calculated value in a [`WithOriginal`](https://github.com/tginsberg/gatherers4j/blob/main/src/main/java/com/ginsberg/gatherers4j/dto/WithOriginal.java)record. [See example.](#emitting-a-record-containing-the-original-and-calculated-values) |

Note: `treatNullAsZero()` is also a valid method on this gatherer, but it only makes sense in a very narrow set of circumstances.

### Examples

#### Running product

```java
Stream
    .of("1.1", "2.2", "3.3")
    .map(BigDecimal::new)
    .gather(Gatherers4j.runningProduct())
    .toList();

// [ 
//   BigDecimal("1.1"), 
//   BigDecimal("2.42"), 
//   BigDecimal("7.986")
// ]
```

#### Showing nulls are ignored by default

```java
Stream
    .of(null, null, new BigDecimal("10.0"), new BigDecimal("2.42"), new BigDecimal("1.234")
    .gather(Gatherers4j.runningProduct())
    .toList();

// [ 
//   BigDecimal("10.0"),
//   BigDecimal("24.200"),
//   BigDecimal("29.862800")
// ]
```

#### Treating null as one

```java
Stream
    .of(null, null, new BigDecimal("10.0"), new BigDecimal("2.0"))
    .gather(Gatherers4j.runningProduct().treatNullAsOne())
    .toList();

// [ 
//   BigDecimal("1"),
//   BigDecimal("1"),
//   BigDecimal("10.0"), 
//   BigDecimal("20.00") 
// ]
```

#### Replacing null with another `BigDecimal`

```java
Stream
    .of(null, null, new BigDecimal("10.0"), new BigDecimal("2.0"))
    .gather(Gatherers4j.runningProduct().treatNullAs(new BigDecimal("3.5")))
    .toList();

// [  
//   BigDecimal("3.5"),  
//   BigDecimal("12.25"),  
//   BigDecimal("122.500"), 
//   BigDecimal("245.0000")
// ]
```

#### Specifying a new `MathContext`

```java
Stream
    .of("1.1", "2.3", "10.4", "2.5")
    .map(BigDecimal::new)
    .gather(Gatherers4j
        .runningProduct()
        .withMathContext(new MathContext(3, RoundingMode.DOWN))
    )
    .toList();

// [ 
//   BigDecimal("1.1"), 
//   BigDecimal("2.53"), 
//   BigDecimal("26.3"), 
//   BigDecimal("65.7") 
// ]
```

#### Emitting a record containing the original and calculated values

```java
Stream
    .of("1.0", "2.0", "10.0", "2.0")
    .map(BigDecimal::new)
    .gather(Gatherers4j.runningProduct().withOriginal())
    .toList();

// [ 
//   WithOriginal[original=1.0, calculated=1.0]
//   WithOriginal[original=2.0, calculated=2.00]
//   WithOriginal[original=10.0, calculated=20.000]
//   WithOriginal[original=2.0, calculated=40.0000]
// ]
```