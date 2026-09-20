---
title: "runningStandardDeviationBy()"
linkTitle: "runningStandardDeviationBy()"
show_in_table: true
category: "Mathematical Operations"
description: Calculate the running standard deviation of `BigDecimal` objects mapped from a `Stream<INPUT>` via a `mappingFunction`, as either a population or a sample.
aliases:
  - /gatherers/mathematical/runningpopulationstandarddeviationby/
  - /gatherers/mathematical/runningsamplestandarddeviationby/

---

### Implementation Notes
This implementation is suitable for mapping an arbitrary `Stream<INPUT>` to `BigDecimal` via a `mappingFunction`; for a version that operates directly on a `Stream<BigDecimal>`, see [`runningStandardDeviation()`](/gatherers4j/gatherers/mathematical/runningstandarddeviation/).
By default, nulls are ignored and play no part in calculations, see `treatNullAs()` and `treatNullAsZero()` below for ways to change this behavior. The default `MathContext`
for all calculations is {{< jdklink linkName="MathContext.DECIMAL64" package="java.base/java/math/MathContext.html#DECIMAL64" >}}, but this can be overridden (see `withMathContext()`, below).

Use `Dataset.Population` when the elements are the entire dataset (the variance is divided by *n*), and `Dataset.Sample` when they are a sample of a larger population (the variance is divided by *n - 1*). The sample standard deviation of a single value is `0`.


**Signatures**

`runningStandardDeviationBy(Dataset dataset, Function<INPUT, BigDecimal> mappingFunction)`
* `dataset` - Either `Population` or `Sample`
* `mappingFunction` - A non-null function to map stream `INPUT` elements into `BigDecimal` for calculation

**Additional Methods**

| Method                                     | Purpose                                                                                                                                                                                                                                                                                                              |
|--------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `treatNullAsZero()`                        | When encountering a `null` value in a stream, treat it as `BigDecimal.ZERO` instead. [See example.](#treating-null-as-zero)                                                                                                                                                                                          |
| `treatNullAs(BigDecimal replacement)`      | When encountering a `null` value in a stream, treat it as the given `replacement` value instead. [See example.](#replacing-null-with-another-bigdecimal)                                                                                                                                                             |
| `treatNullAsOne()`                         | When encountering a `null` value in a stream, treat it as `BigDecimal.ONE` instead.                                                                                                                                                                                                                                  |
| `withMathContext(MathContext mathContext)` | Replace the `MathContext` used for all mathematical operations performed by this gatherer. [See example.](#specifying-a-new-mathcontext)                                                                                                                                                                             |
| `withOriginal()`                           | Include the original input value from the stream in addition to the calculated value in a [`WithOriginal`](https://github.com/tginsberg/gatherers4j/blob/main/src/main/java/com/ginsberg/gatherers4j/dto/WithOriginal.java) record. [See example.](#emitting-a-record-containing-the-original-and-calculated-values) |

### Examples

#### Running Standard Deviation (Population), mapped from an object

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
    .gather(Gatherers4j.runningStandardDeviationBy(Dataset.Population, NamedValue::value))
    .toList();

// [ 
//   BigDecimal("0.0"), 
//   BigDecimal("0.5"),
//   BigDecimal("4.02768199119819"), 
//   BigDecimal("7.628073151196179"),
//   BigDecimal("11.0562199688682") 
// ]
```

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
    .gather(Gatherers4j.runningStandardDeviationBy(Dataset.Sample, NamedValue::value))
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
    .gather(Gatherers4j.runningStandardDeviationBy(Dataset.Population, NamedValue::value))
    .toList();

// [
//   BigDecimal("0.0"), 
//   BigDecimal("5.0"), 
//   BigDecimal("8.164965809277261") 
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
    .gather(Gatherers4j.runningStandardDeviationBy(Dataset.Population, NamedValue::value).treatNullAsZero())
    .toList();

// [
//   BigDecimal("0"),
//   BigDecimal("0"),
//   BigDecimal("4.714045207910317"), 
//   BigDecimal("8.2915619758885"), 
//   BigDecimal("11.6619037896906") 
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
    .gather(Gatherers4j.runningStandardDeviationBy(Dataset.Population, NamedValue::value).treatNullAs(BigDecimal.TWO))
    .toList();

// [
//   BigDecimal("0"),
//   BigDecimal("0"),
//   BigDecimal("3.771236166328253"), 
//   BigDecimal("7.399324293474371"), 
//   BigDecimal("10.85172797300043") 
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
        .runningStandardDeviationBy(Dataset.Population, NamedValue::value)
        .withMathContext(new MathContext(3, RoundingMode.DOWN))
    )
    .toList();

// [ 
//   BigDecimal("0.0"), 
//   BigDecimal("0.5"),
//   BigDecimal("4.02"), 
//   BigDecimal("7.62"),
//   BigDecimal("11") 
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
    .gather(Gatherers4j.runningStandardDeviationBy(Dataset.Population, NamedValue::value).withOriginal())
    .toList();

// [ 
//   WithOriginal[original=NamedValue[name=first, value=1.0], calculated=0.0]
//   WithOriginal[original=NamedValue[name=second, value=2.0], calculated=0.5]
//   WithOriginal[original=NamedValue[name=third, value=10.0], calculated=4.02768199119819]
//   WithOriginal[original=NamedValue[name=fourth, value=20.0], calculated=7.628073151196179]
//   WithOriginal[original=NamedValue[name=fifth, value=30.0], calculated=11.0562199688682]
// ]
```
