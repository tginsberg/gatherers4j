---
title: "exponentialMovingAverageWithAlpha()"
linkTitle: "exponentialMovingAverageWithAlpha()"
show_in_table: true
category: "Mathematical Operations"
description: Create an exponential average of `BigDecimal` values, with the given `alpha`.

---

### Implementation Notes
This implementation is suitable for `Stream<BigDecimal>`. For a version that takes a user-specified mapping function, see [`exponentialMovingAverageWithAlphaBy()`](/gatherers4j/gatherers/mathematical/exponentialmovingaveragewithalphaby/).
By default, nulls are ignored and play no part in calculations, see `treatNullAs()` and `treatNullAsZero()` below for ways to change this behavior.

**Signatures**

`exponentialMovingAverageWithAlpha(double alpha)`
* `alpha` - The alpha value to use, which must be between 0 and 1, exclusive

**Additional Methods**

| Method                                | Purpose                                                                                                                                                                                                                                                                                                              |
|---------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `treatNullAsZero()`                   | When encountering a `null` value in a stream, treat it as `BigDecimal.ZERO` instead. [See example.](#treating-null-as-zero)                                                                                                                                                                                          |
| `treatNullAs(BigDecimal replacement)` | When encountering a `null` value in a stream, treat it as the given `replacement` value instead. [See example.](#replacing-null-with-another-bigdecimal)                                                                                                                                                             |
| `withOriginal()`                      | Include the original input value from the stream in addition to the calculated value in a [`WithOriginal`](https://github.com/tginsberg/gatherers4j/blob/main/src/main/java/com/ginsberg/gatherers4j/dto/WithOriginal.java) record. [See example.](#emitting-a-record-containing-the-original-and-calculated-values) |

### Examples

#### Exponential moving average with alpha of 0.3

```java
Stream
    .of("10.5", "15.2", "8.7", "12.0", "9.8")
    .map(BigDecimal::new)
    .gather(Gatherers4j.exponentialMovingAverageWithAlpha(0.3))
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
Stream
    .of(null, null, "10.5", null, "15.2", "8.7", "12.0", "9.8")
    .map(it -> it == null ? null : new BigDecimal(it))
    .gather(Gatherers4j.exponentialMovingAverageWithAlpha(0.3))
    .toList();

// [
//    BigDecimal("10.5")
//    BigDecimal("11.91")
//    BigDecimal("10.947")
//    BigDecimal("11.2629")
//    BigDecimal("10.82403")
// ]
```

#### Treating null as zero

```java
Stream
    .of(null, null, "10.5", null, "15.2", "8.7", "12.0", "9.8")
    .map(it -> it == null ? null : new BigDecimal(it))
    .gather(Gatherers4j
        .exponentialMovingAverageWithAlpha(0.3)
        .treatNullAsZero()
    )
    .toList();

// [
//    BigDecimal("0")
//    BigDecimal("0.0")
//    BigDecimal("3.15")
//    BigDecimal("2.205")
//    BigDecimal("6.1035")
//    BigDecimal("6.88245")
//    BigDecimal("8.417715")
//    BigDecimal("8.8324005")
// ]
```

#### Replacing null with another `BigDecimal`

```java
Stream
    .of(null, null, "10.5", null, "15.2", "8.7", "12.0", "9.8")
    .map(it -> it == null ? null : new BigDecimal(it))
    .gather(Gatherers4j
        .exponentialMovingAverageWithAlpha(0.3)
        .treatNullAs(BigDecimal.ONE)
    )
    .toList();

// [
//    BigDecimal("1")
//    BigDecimal("1.0")
//    BigDecimal("3.85")
//    BigDecimal("2.995")
//    BigDecimal("6.6565")
//    BigDecimal("7.26955")
//    BigDecimal("8.688685")
//    BigDecimal("9.0220795")
// ]
```

#### Emitting a record containing the original and calculated values

```java
Stream
    .of("10.5", "15.2", "8.7", "12.0", "9.8")
    .map(BigDecimal::new)
    .gather(Gatherers4j
        .exponentialMovingAverageWithAlpha(0.3)
        .withOriginal()
    )
    .toList();

// [
//    WithOriginal[original=10.5, calculated=10.5]
//    WithOriginal[original=15.2, calculated=11.91]
//    WithOriginal[original=8.7, calculated=10.947]
//    WithOriginal[original=12.0, calculated=11.2629]
//    WithOriginal[original=9.8, calculated=10.82403]
// ]
```