---
title: "exponentialMovingAverageWithPeriod()"
linkTitle: "exponentialMovingAverageWithPeriod()"
show_in_table: true
category: "Mathematical Operations"
description: Create an exponential average of `BigDecimal` values, over the given number of `periods`.

---

### Implementation Notes
This implementation is suitable for `Stream<BigDecimal>`, for a version that takes user-specified mapping function see [`exponentialMovingAverageWithPeriodBy()`](/gatherers4j/gatherers/mathematical/exponentialmovingaveragewithperiodby/).
By default, nulls are ignored and play no part in calculations, see `treatNullAs()` and `treatNullAsZero()` below for ways to change this behavior. 

**Signatures**

`exponentialMovingAverageWithPeriod(int periods)`
* `periods` - The number of `periods` to average over, must be greater than 1.

**Additional Methods**

| Method                                | Purpose                                                                                                                                                                                                                                                                                                         |
|---------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `treatNullAsZero()`                   | When encountering a `null` value in a stream, treat it as `BigDecimal.ZERO` instead. [See example.](#treating-null-as-zero)                                                                                                                                                                                     |
| `treatNullAs(BigDecimal replacement)` | When encountering a `null` value in a stream, treat it as the given `replacement` value instead. [See example.](#replacing-null-with-another-bigdecimal)                                                                                                                                                        |
| `withOriginal()`                      | Include the original input value from the stream in addition to the calculated value in a [`WithOriginal`](https://github.com/tginsberg/gatherers4j/blob/main/src/main/java/com/ginsberg/gatherers4j/WithOriginal.java)record. [See example.](#emitting-a-record-containing-the-original-and-calculated-values) |

### Examples

#### Exponential moving average with period of 3

```java
Stream
    .of("10.5", "15.2", "8.7", "12.0", "9.8")
    .map(BigDecimal::new)
    .gather(Gatherers4j.exponentialMovingAverageWithPeriod(3))
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
Stream
    .of(null, null, "10.5", null, "15.2", "8.7", "12.0", "9.8")
    .map(it -> it == null ? null : new BigDecimal(it))
    .gather(Gatherers4j.exponentialMovingAverageWithPeriod(3))
    .toList();

// [
//    BigDecimal("10.5")
//    BigDecimal("12.85")
//    BigDecimal("10.775")
//    BigDecimal("11.3875")
//    BigDecimal("10.59375")
// ]
```

#### Treating null as zero

```java
Stream
    .of(null, null, "10.5", null, "15.2", "8.7", "12.0", "9.8")
    .map(it -> it == null ? null : new BigDecimal(it))
    .gather(Gatherers4j
        .exponentialMovingAverageWithPeriod(3)
        .treatNullAsZero()
    )
    .toList();

// [
//    BigDecimal("0")
//    BigDecimal("0.0")
//    BigDecimal("5.25")
//    BigDecimal("2.625")
//    BigDecimal("8.9125")
//    BigDecimal("8.80625")
//    BigDecimal("10.403125")
//    BigDecimal("10.1015625")
// ]
```

#### Replacing null with another `BigDecimal`

```java
Stream
    .of(null, null, "10.5", null, "15.2", "8.7", "12.0", "9.8")
    .map(it -> it == null ? null : new BigDecimal(it))
    .gather(Gatherers4j
        .exponentialMovingAverageWithPeriod(3)
        .treatNullAs(BigDecimal.ONE)
    )
    .toList();

// [
//    BigDecimal("1")
//    BigDecimal("1.0")
//    BigDecimal("5.75")
//    BigDecimal("3.375")
//    BigDecimal("9.2875")
//    BigDecimal("8.99375")
//    BigDecimal("10.496875")
//    BigDecimal("10.1484375")
// ]
```

#### Emitting a record containing the original and calculated values

```java
Stream
    .of("10.5", "15.2", "8.7", "12.0", "9.8")
    .map(BigDecimal::new)
    .gather(Gatherers4j
        .exponentialMovingAverageWithPeriod(3)
        .withOriginal()
    )
   .toList();

// [
//    WithOriginal[original=10.5, calculated=10.5]
//    WithOriginal[original=15.2, calculated=12.85]
//    WithOriginal[original=8.7, calculated=10.775]
//    WithOriginal[original=12.0, calculated=11.3875]
//    WithOriginal[original=9.8, calculated=10.59375]
// ]
```