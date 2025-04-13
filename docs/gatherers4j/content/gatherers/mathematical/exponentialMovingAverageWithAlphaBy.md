---
title: "exponentialMovingAverageWithAlphaBy()"
linkTitle: "exponentialMovingAverageWithAlphaBy()"
show_in_table: true
category: "Mathematical Operations"
description: Calculate the exponential moving average of `BigDecimal` objects mapped from a `Stream<INPUT>` via a `mappingFunction` with the given `alpha`.

---

### Implementation Notes
This implementation is suitable mapping an arbitrary `Stream<INPUT>` to `BigDecimal` via a `mappingFunction`; for a version that operates directly on a `Stream<BigDecimal>`, see [`exponentialMovingAverageWithAlpha()`](/gatherers4j/gatherers/mathematical/exponentialmovingaveragewithalpha/).
By default, nulls are ignored and play no part in calculations, see `treatNullAs()` and `treatNullAsZero()` below for ways to change this behavior. 

**Signatures**

`exponentialMovingAverageWithAlphaBy(double alpha, Function<INPUT, BigDecimal> mappingFunction)`
* `alpha` - The alpha value to use, which must be between 0 and 1, exclusive
* `mappingFunction` - A non-null function to map stream `INPUT` elements into `BigDecimal` for calculation

**Additional Methods**

| Method                                | Purpose                                                                                                                                                                                                                                                                                                         |
|---------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `treatNullAsZero()`                   | When encountering a `null` value in a stream, treat it as `BigDecimal.ZERO` instead. [See example.](#treating-null-as-zero)                                                                                                                                                                                     |
| `treatNullAs(BigDecimal replacement)` | When encountering a `null` value in a stream, treat it as the given `replacement` value instead. [See example.](#replacing-null-with-another-bigdecimal)                                                                                                                                                        |
| `withOriginal()`                      | Include the original input value from the stream in addition to the calculated value in a [`WithOriginal`](https://github.com/tginsberg/gatherers4j/blob/main/src/main/java/com/ginsberg/gatherers4j/WithOriginal.java)record. [See example.](#emitting-a-record-containing-the-original-and-calculated-values) |

### Examples

#### Exponential moving average with alpha of 0.3, mapped from an object

```java
record NamedValue(String name, BigDecimal value) {}

Stream
    .of(
        new NamedValue("first",  new BigDecimal("10.5")),
        new NamedValue("second", new BigDecimal("15.2")),
        new NamedValue("third",  new BigDecimal("8.7")),
        new NamedValue("fourth", new BigDecimal("12.0")),
        new NamedValue("fifth",  new BigDecimal("9.8"))
    )
    .gather(Gatherers4j.exponentialMovingAverageWithAlphaBy(0.3, NamedValue::value))
    .toList();

// [
//    BigDecimal("10.5")
//    BigDecimal("11.91")
//    BigDecimal("10.947")
//    BigDecimal("11.2629")
//    BigDecimal("10.82403")
// ]

```

#### Showing nulls are ignored by default

```java
record NamedValue(String name, BigDecimal value) {}

Stream
    .of(
        new NamedValue("first",  null),
        new NamedValue("second", null),
        new NamedValue("third",  new BigDecimal("8.7")),
        new NamedValue("fourth", new BigDecimal("12.0")),
        new NamedValue("fifth",  new BigDecimal("9.8"))
    )
    .gather(Gatherers4j.exponentialMovingAverageWithAlphaBy(0.3, NamedValue::value))
    .toList();

// [
//    BigDecimal("8.7")
//    BigDecimal("9.69")
//    BigDecimal("9.723")
// ]
```

#### Treating null as zero

```java
record NamedValue(String name, BigDecimal value) {}

Stream
    .of(
        new NamedValue("first",  null),
        new NamedValue("second", null),
        new NamedValue("third",  new BigDecimal("8.7")),
        new NamedValue("fourth", new BigDecimal("12.0")),
        new NamedValue("fifth",  new BigDecimal("9.8"))
    )
    .gather(Gatherers4j
        .exponentialMovingAverageWithAlphaBy(0.3, NamedValue::value)
        .treatNullAsZero()
    )
    .toList();

// [
//    BigDecimal("0")
//    BigDecimal("0.0")
//    BigDecimal("2.61")
//    BigDecimal("5.427")
//    BigDecimal("6.7389")
// ]
```

#### Replacing null with another `BigDecimal`


```java
record NamedValue(String name, BigDecimal value) {}

Stream
    .of(
        new NamedValue("first",  null),
        new NamedValue("second", null),
        new NamedValue("third",  new BigDecimal("8.7")),
        new NamedValue("fourth", new BigDecimal("12.0")),
        new NamedValue("fifth",  new BigDecimal("9.8"))
    )
    .gather(Gatherers4j
        .exponentialMovingAverageWithAlphaBy(0.3, NamedValue::value)
        .treatNullAs(BigDecimal.ONE)
    )
    .toList();

// [
//    BigDecimal("1")
//    BigDecimal("1.0")
//    BigDecimal("3.31")
//    BigDecimal("5.917")
//    BigDecimal("7.0819")
// ]
```


#### Emitting a record containing the original and calculated values


```java
record NamedValue(String name, BigDecimal value) {}

Stream
    .of(
        new NamedValue("first",  new BigDecimal("10.5")),
        new NamedValue("second", new BigDecimal("15.2")),
        new NamedValue("third",  new BigDecimal("8.7")),
        new NamedValue("fourth", new BigDecimal("12.0")),
        new NamedValue("fifth",  new BigDecimal("9.8"))
    )
    .gather(Gatherers4j
        .exponentialMovingAverageWithAlphaBy(0.3, NamedValue::value)
        .withOriginal()
    )
    .toList();

// [
//    WithOriginal[original=NamedValue[name=first, value=10.5], calculated=10.5]
//    WithOriginal[original=NamedValue[name=second, value=15.2], calculated=11.91]
//    WithOriginal[original=NamedValue[name=third, value=8.7], calculated=10.947]
//    WithOriginal[original=NamedValue[name=fourth, value=12.0], calculated=11.2629]
//    WithOriginal[original=NamedValue[name=fifth, value=9.8], calculated=10.82403]
// ]
```