---
title: "runningSampleStandardDeviationBy()"
linkTitle: "runningSampleStandardDeviationBy()"
show_in_table: true
category: "Mathematical Operations"
description: Calculate the running sample standard deviation of a `BigDecimal` objects mapped from a `Stream<BigDecimal>` via a `mappingFunction`.

---

### Implementation Notes
This implementation is suitable mapping an arbitrary `Stream<INPUT>` to `BigDecimal` via a `mappingFunction`; for a version that operates directly on a `Stream<BigDecimal>`, see [`runningSampleStandardDeviation()`](/gatherers4j/gatherers/mathematical/runningsamplestandarddeviation/).
By default, nulls are ignored and play no part in calculations, see `treatNullAs()` and `treatNullAsZero()` below for ways to change this behavior. The default `MathContext`
for all calculations is {{< jdklink linkName="MathContext.DECIMAL64" package="java.base/java/math/MathContext.html#DECIMAL64" >}}, but this can be overridden (see `withMathContext()`, below).


**Signatures**

`runningSampleStandardDeviationBy(Function<INPUT, BigDecimal> mappingFunction)`
* `mappingFunction` - A non-null function to map stream `INPUT` elements into `BigDecimal` for calculation

**Additional Methods**

| Method                                     | Purpose                                                                                                                                                                                                                                                                                                         |
|--------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `treatNullAsZero()`                        | When encountering a `null` value in a stream, treat it as `BigDecimal.ZERO` instead. [See example.](#treating-null-as-zero)                                                                                                                                                                                     |
| `treatNullAs(BigDecimal replacement)`      | When encountering a `null` value in a stream, treat it as the given `replacement` value instead. [See example.](#replacing-null-with-another-bigdecimal)                                                                                                                                                        |
| `withMathContext(MathContext mathContext)` | Replace the `MathContext` used for all mathematical operations performed by this gatherer. [See example.](#specifying-a-new-mathcontext)                                                                                                                                                                        |
| `withOriginal()`                           | Include the original input value from the stream in addition to the calculated value in a [`WithOriginal`](https://github.com/tginsberg/gatherers4j/blob/main/src/main/java/com/ginsberg/gatherers4j/dto/WithOriginal.java)record. [See example.](#emitting-a-record-containing-the-original-and-calculated-values) |

### Examples

#### Running Standard Deviation (Sample), mapped from an object

```java
record NamedValue(String name, BigDecimal value) {}

Stream
    .of(
        new NamedValue("first",  new BigDecimal("1.0")),
        new NamedValue("second", new BigDecimal("2.0")),
        new NamedValue("third",  new BigDecimal("10.0")),
        new NamedValue("fourth", new BigDecimal("20.0")),
        new NamedValue("fifth",  new BigDecimal("30.0"))
    )
    .gather(Gatherers4j.runningSampleStandardDeviationBy(NamedValue::value))
    .toList();

// [ 
//   BigDecimal("0.0"), 
//   BigDecimal("0.7071067811865475"),
//   BigDecimal("4.932882862316247"), 
//   BigDecimal("8.808140174482542"),
//   BigDecimal("12.36122971228995") 
// ]
```

#### Showing nulls are ignored by default

```java
record NamedValue(String name, BigDecimal value) {}

Stream
    .of(
        new NamedValue("first",  null),
        new NamedValue("second", null),
        new NamedValue("third",  new BigDecimal("10.0")),
        new NamedValue("fourth", new BigDecimal("20.0")),
        new NamedValue("fifth",  new BigDecimal("30.0"))
    )
    .gather(Gatherers4j.runningSampleStandardDeviationBy(NamedValue::value))
    .toList();

// [
//   BigDecimal("0"), 
//   BigDecimal("7.071067811865475"), 
//   BigDecimal("10.0") 
// ]
```

#### Treating null as zero

```java
record NamedValue(String name, BigDecimal value) {}

Stream
    .of(
        new NamedValue("first",  null),
        new NamedValue("second", null),
        new NamedValue("third",  new BigDecimal("10.0")),
        new NamedValue("fourth", new BigDecimal("20.0")),
        new NamedValue("fifth",  new BigDecimal("30.0"))
    )
    .gather(Gatherers4j.runningSampleStandardDeviationBy(NamedValue::value).treatNullAsZero())
    .toList();

// [
//   BigDecimal("0"),
//   BigDecimal("0"),
//   BigDecimal("5.773502691896258"), 
//   BigDecimal("9.574271077563381"), 
//   BigDecimal("13.0384048104053") 
// ]
```

#### Replacing null with another `BigDecimal`


```java
record NamedValue(String name, BigDecimal value) {}

Stream
    .of(
        new NamedValue("first",  null),
        new NamedValue("second", null),
        new NamedValue("third",  new BigDecimal("10.0")),
        new NamedValue("fourth", new BigDecimal("20.0")),
        new NamedValue("fifth",  new BigDecimal("30.0"))
    )
    .gather(Gatherers4j.runningSampleStandardDeviationBy(NamedValue::value).treatNullAs(BigDecimal.TWO))
    .toList();

// [
//   BigDecimal("0"),
//   BigDecimal("0"),
//   BigDecimal("4.618802153517006"), 
//   BigDecimal("8.544003745317531"), 
//   BigDecimal("12.13260071048248") 
// ]
```


#### Specifying a new `MathContext`


```java
record NamedValue(String name, BigDecimal value) {}

Stream
    .of(
        new NamedValue("first",  new BigDecimal("1.0")),
        new NamedValue("second", new BigDecimal("2.0")),
        new NamedValue("third",  new BigDecimal("10.0")),
        new NamedValue("fourth", new BigDecimal("20.0")),
        new NamedValue("fifth",  new BigDecimal("30.0"))
    )
    .gather(Gatherers4j
        .runningSampleStandardDeviationBy(NamedValue::value)
        .withMathContext(new MathContext(3, RoundingMode.DOWN))
    )
    .toList();

// [ 
//   BigDecimal("0"), 
//   BigDecimal("0.707"),
//   BigDecimal("4.92"), 
//   BigDecimal("8.8"),
//   BigDecimal("12.3") 
// ]
```


#### Emitting a record containing the original and calculated values


```java
record NamedValue(String name, BigDecimal value) {}

Stream
    .of(
        new NamedValue("first",  new BigDecimal("1.0")),
        new NamedValue("second", new BigDecimal("2.0")),
        new NamedValue("third",  new BigDecimal("10.0")),
        new NamedValue("fourth", new BigDecimal("20.0")),
        new NamedValue("fifth",  new BigDecimal("30.0"))
    )
    .gather(Gatherers4j.runningSampleStandardDeviationBy(NamedValue::value).withOriginal())
    .toList();

// [ 
//   WithOriginal[original=NamedValue[name=first, value=1.0], calculated=0]
//   WithOriginal[original=NamedValue[name=second, value=2.0], calculated=0.7071067811865475]
//   WithOriginal[original=NamedValue[name=third, value=10.0], calculated=4.932882862316247]
//   WithOriginal[original=NamedValue[name=fourth, value=20.0], calculated=8.808140174482542]
//   WithOriginal[original=NamedValue[name=fifth, value=30.0], calculated=12.36122971228995]
// ]
```