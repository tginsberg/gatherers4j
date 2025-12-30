---
title: "exponentialMovingAverageWithPeriodBy()"
linkTitle: "exponentialMovingAverageWithPeriodBy()"
show_in_table: true
category: "Mathematical Operations"
description: Calculate the exponential moving average of `BigDecimal` objects mapped from a `Stream<INPUT>` via a `mappingFunction` over the given number of `periods`.

---

### Implementation Notes
This implementation is suitable for mapping an arbitrary `Stream<INPUT>` to `BigDecimal` via a `mappingFunction`; for a version that operates directly on a `Stream<BigDecimal>`, see [`exponentialMovingAverageWithPeriod()`](/gatherers4j/gatherers/mathematical/exponentialmovingaveragewithperiod/).
By default, nulls are ignored and play no part in calculations, see `treatNullAs()` and `treatNullAsZero()` below for ways to change this behavior. 

**Signatures**

`exponentialMovingAverageWithPeriodBy(int periods, Function<INPUT, BigDecimal> mappingFunction)`
* `periods` - The number of `periods` to average over, must be greater than 1.
* `mappingFunction` - A non-null function to map stream `INPUT` elements into `BigDecimal` for calculation

**Additional Methods**

| Method                                | Purpose                                                                                                                                                                                                                                                                                                              |
|---------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `treatNullAsZero()`                   | When encountering a `null` value in a stream, treat it as `BigDecimal.ZERO` instead. [See example.](#treating-null-as-zero)                                                                                                                                                                                          |
| `treatNullAs(BigDecimal replacement)` | When encountering a `null` value in a stream, treat it as the given `replacement` value instead. [See example.](#replacing-null-with-another-bigdecimal)                                                                                                                                                             |
| `withOriginal()`                      | Include the original input value from the stream in addition to the calculated value in a [`WithOriginal`](https://github.com/tginsberg/gatherers4j/blob/main/src/main/java/com/ginsberg/gatherers4j/dto/WithOriginal.java) record. [See example.](#emitting-a-record-containing-the-original-and-calculated-values) |

### Examples

#### Exponential moving average with period of 3, mapped from an object

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
    .gather(Gatherers4j.exponentialMovingAverageWithPeriodBy(3, NamedValue::value))
    .toList();

// [
//    BigDecimal("10.5")
//    BigDecimal("12.85")
//    BigDecimal("10.775")
//    BigDecimal("11.3875")
//    BigDecimal("10.59375")
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
    .gather(Gatherers4j.exponentialMovingAverageWithPeriodBy(3, NamedValue::value))
    .toList();

// [
//    BigDecimal("8.7")
//    BigDecimal("10.35")
//    BigDecimal("10.075")
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
        .exponentialMovingAverageWithPeriodBy(3, NamedValue::value)
        .treatNullAsZero()
    )
    .toList();

// [
//    BigDecimal("0")
//    BigDecimal("0.0")
//    BigDecimal("4.35")
//    BigDecimal("8.175")
//    BigDecimal("8.9875")
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
        .exponentialMovingAverageWithPeriodBy(3, NamedValue::value)
        .treatNullAs(BigDecimal.ONE)
    )
    .toList();

// [
//    BigDecimal("1")
//    BigDecimal("1.0")
//    BigDecimal("4.85")
//    BigDecimal("8.425")
//    BigDecimal("9.1125")
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
        .exponentialMovingAverageWithPeriodBy(3, NamedValue::value)
        .withOriginal()
    )
    .toList();

// [
//    WithOriginal[original=NamedValue[name=first, value=10.5], calculated=10.5]
//    WithOriginal[original=NamedValue[name=second, value=15.2], calculated=12.85]
//    WithOriginal[original=NamedValue[name=third, value=8.7], calculated=10.775]
//    WithOriginal[original=NamedValue[name=fourth, value=12.0], calculated=11.3875]
//    WithOriginal[original=NamedValue[name=fifth, value=9.8], calculated=10.59375]
// ]
```