---
title: "movingSum()"
linkTitle: "movingSum()"
show_in_table: true
category: "Mathematical Operations"
description: Calculate the moving sum of a `Stream<BigDecimal>` looking back `windowSize` number of elements.

---

### Implementation Notes
This implementation is suitable for `Stream<BigDecimal>`, for a version that takes user-specified mapping function see [`movingSumBy()`](/gatherers4j/gatherers/mathematical/movingsumby/).
By default, nulls are ignored and play no part in calculations, see `treatNullAs()` and `treatNullAsZero()` below for ways to change this behavior. The default `MathContext`
for all calculations is {{< jdklink linkName="MathContext.DECIMAL64" package="java.base/java/math/MathContext.html#DECIMAL64" >}}, but this can be overridden (see `withMathContext()`, below).


**Signatures**

`movingSum(int windowSize)`
* `windowSize` - How many trailing elements to calculate the sum over

**Additional Methods**

| Method                                     | Purpose                                                                                                                                                                                                                                                                                                         |
|--------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `includePartialValues()`                   | When calculating the moving sum and the full size of the window has not yet been reached, the gatherer should emit values for what it has. [See example.](#including-partial-values)                                                                                                                            |
| `treatNullAsZero()`                        | When encountering a `null` value in a stream, treat it as `BigDecimal.ONE` instead. [See example.](#treating-null-as-zero)                                                                                                                                                                                      |
| `treatNullAs(BigDecimal replacement)`      | When encountering a `null` value in a stream, treat it as the given `replacement` value instead. [See example.](#replacing-null-with-another-bigdecimal)                                                                                                                                                        |
| `withMathContext(MathContext mathContext)` | Replace the `MathContext` used for all mathematical operations performed by this gatherer. [See example.](#specifying-a-new-mathcontext)                                                                                                                                                                        |
| `withOriginal()`                           | Include the original input value from the stream in addition to the calculated value in a [`WithOriginal`](https://github.com/tginsberg/gatherers4j/blob/main/src/main/java/com/ginsberg/gatherers4j/WithOriginal.java)record. [See example.](#emitting-a-record-containing-the-original-and-calculated-values) |

### Examples

#### Moving sum of window size 3

```java
Stream
    .of("1.0", "2.0", "10.0", "2.0")
    .map(BigDecimal::new)
    .gather(Gatherers4j.movingSum(3))
    .toList();

// [ 
//   BigDecimal("13.0"), 
//   BigDecimal("14.0") 
// ]
```

#### Including partial values

Showing that an in-process moving sum is emitted for each element, even if there aren't 3 elements from which to calculate a full window yet.

```java
Stream
    .of("1.0", "2.0", "10.0", "2.0")
    .map(BigDecimal::new)
    .gather(Gatherers4j.movingSum(3).includePartialValues())
    .toList();

// [ 
//   BigDecimal("1.0"), 
//   BigDecimal("3.0"), 
//   BigDecimal("13.0"), 
//   BigDecimal("14.0") 
// ]
```


#### Showing nulls are ignored by default

```java
Stream
    .of(null, null, new BigDecimal("10.0"), new BigDecimal("2.0"), new BigDecimal("1.0"))
    .gather(Gatherers4j.movingSum(3))
    .toList();

// [  
//   BigDecimal("13.0")
// ]
```

#### Treating null as zero

```java
Stream
    .of(null, null, new BigDecimal("10.0"), new BigDecimal("2.0"))
    .gather(Gatherers4j.movingSum(3).treatNullAsZero())
    .toList();

// [ 
//   BigDecimal("10.0"), 
//   BigDecimal("12.00") 
// ]
```

#### Replacing null with another `BigDecimal`

```java
Stream
    .of(null, null, new BigDecimal("10.0"), new BigDecimal("2.0"))
    .gather(Gatherers4j.movingSum(3).treatNullAs(new BigDecimal("3.5")))
    .toList();

// [ 
//   BigDecimal("17.0"), 
//   BigDecimal("15.5") 
// ]
```

#### Specifying a new `MathContext`

```java
Stream
    .of("1.111", "2.222", "10.333", "2.444")
    .map(BigDecimal::new)
    .gather(Gatherers4j
        .movingSum(3)
        .withMathContext(new MathContext(3, RoundingMode.DOWN))
    )
    .toList();

// [ 
//   BigDecimal("13.6"), 
//   BigDecimal("14.9") 
// ]
```

#### Emitting a record containing the original and calculated values

```java
Stream
    .of("1.0", "2.0", "10.0", "2.0")
    .map(BigDecimal::new)
    .gather(Gatherers4j.movingSum(3).withOriginal())
    .toList();

// [ 
//   WithOriginal[original=10.0, calculated=13.0]
//   WithOriginal[original=2.0, calculated=14.0]
// ]
```