# AGENTS.md

Instructions for AI agents working on Gatherers4j. Every rule here was checked against the code. When this file and
the code disagree, the code wins. Fix this file in the same change.

## Project

Gatherers4j is a Java library of custom [Stream Gatherers](https://openjdk.org/jeps/485) (JEP 485), published to Maven
Central as `com.ginsberg:gatherers4j`. JSpecify is its only runtime dependency.

- **Java:** Java 25 with no preview features. Language features that are final in Java 25 are fine, for example
  Markdown doc comments (`///`) and statements before `super(...)`. CI also builds on Java 27.
- **Build and tests:** Gradle with the Kotlin DSL, JUnit Jupiter and AssertJ.
- **Static analysis:** ErrorProne and NullAway. NullAway findings are compile errors in main code.
- **Mutation testing:** Pitest.
- **Docs:** a Hugo/Docsy site in `docs/gatherers4j/`.

The library is pre-1.0 and has shipped breaking changes in minor releases. Treat any new public method as permanent
anyway: removing it later is another breaking change.

## Commands

| Task | Command |
|---|---|
| Build and run all tests | `./gradlew build` |
| One test class | `./gradlew test --tests "com.ginsberg.gatherers4j.TakeUntilGathererTest"` |
| Build on another JDK | `./gradlew build -PjavaVersion=27` |
| Mutation testing | `./gradlew pitest` (fails below 75% mutation score or 80% line coverage) |
| Benchmarks | `./gradlew jmh` |
| Preview the docs site | `docs/gatherers4j/start.sh` (needs extended Hugo 0.160.1 or newer, plus npm) |

## Layout

```
src/main/java/module-info.java       exports com.ginsberg.gatherers4j, .dto, .enums
src/main/java/com/ginsberg/gatherers4j/
    Gatherers4j.java                 the facade: every public factory method lives here
    *Gatherer.java, *Gatherers.java  implementations (a *Gatherers file holds several small ones)
    *StatGatherer*.java,
    StatisticAccumulator*.java       the BigDecimal statistics engine (see below)
    dto/                             public output records: Pair, WithCount, WithIndex, WithOriginal
    enums/                           public options: Dataset, Frequency, Order, Rotate, Size
    util/                            internal, not exported: GathererUtils, CircularBuffer, MathUtils
src/test/java/com/ginsberg/gatherers4j/
    test/, util/                     shared test helpers
src/jmh/java/com/ginsberg/gatherers4j/bench/   JMH benchmarks
docs/gatherers4j/content/gatherers/<category>/ one docs page per public factory method
```

## Before adding public API

New public API is the maintainer's decision. Propose it first unless you were asked to build it. A candidate must pass
the [guiding principles](docs/gatherers4j/content/guiding-principles/_index.md):

- It can't be done with `map`, `filter`, or a collector unless outside state is enclosed.
- It isn't an alias.
- It follows the naming convention: `...By()` for mapped or comparator variants, `ensure...()` for validation,
  `...Indexed()` for index-aware variants, and imperative verbs.

## Library code

### The facade

- Users create every gatherer through a `public static` method on `Gatherers4j` and never call a constructor.
  Gatherer constructors are package-private.
- Return `Gatherer<INPUT, ?, OUTPUT>`, unless the gatherer has fluent options. In that case return the concrete type
  (for example `ZipWithGatherer` or `BigDecimalMovingStatGatherer`) so the options can be called. Any type a factory
  method returns must be `public`.
- A gatherer that takes a second source gets four overloads: `Iterable`, `Iterator`, `Stream` and varargs. See
  `crossWith`, `interleaveWith` and `zipWith`.
- A mapping function or `Comparator` is the last parameter, as in `movingSumBy(windowSize, fn)` and
  `movingStandardDeviationBy(dataset, windowSize, fn)`.
- Keep the facade methods in alphabetical order.
- Give every factory method a Markdown doc comment (`///`) with an `@param` for each parameter and each type
  parameter, and an `@return` ("A non-null `Gatherer`"). The build turns doclint off, so nothing will flag a missing
  tag.

### Implementing a gatherer

- **State:** put per-stream mutable state in a nested `static class State` and create it from `initializer()`. A
  gatherer instance can be reused across streams. The only exception is a single-use second source (`Iterator` or
  `Stream`), which `InterleavingGatherer` and `ZipWithGatherer` keep in a field.
- **Fluent options:** they set configuration fields and return `this`.
- **Integrator:** use `Integrator.ofGreedy(...)` unless the gatherer decides on its own to stop early, as `takeUntil`
  does. Return the result of `downstream.push(...)` or `!downstream.isRejecting()`.
- **Pushing many elements:** push collections, streams and iterators with `GathererUtils.pushAll`, which stops as
  soon as the downstream rejects.
- **Finisher:** implement `finisher()` only when there is something to emit after the input ends.
- **Combiner:** implement `combiner()` only when the gatherer emits nothing until its finisher and its state merges
  exactly, as in `FrequencyGatherer` and `UniquelyOccurringGatherer`. A gatherer that emits per element can't have a
  correct combiner. Leave the default, which runs the gatherer sequentially even in a parallel stream.
- **Argument validation:** validate in the constructor or the factory method, never in the integrator.
  - For null arguments, use `GathererUtils.mustNotBeNull(arg, "Predicate must not be null")`, which throws
    `IllegalArgumentException`. Don't use `Objects.requireNonNull`.
  - For other invalid values, throw `IllegalArgumentException` with a message that names the parameter and the rule
    it broke, for example "Window size must be greater than 1".
- **Windows:** use `util.CircularBuffer` for fixed-size trailing windows.
- **New types:** reuse the existing `dto` records and `enums` before adding new ones. In a public signature, prefer an
  enum over a boolean flag. Anything new in `dto` or `enums` is public API.

### Nullness

- Every package is `@NullMarked`, so types are non-null unless annotated with JSpecify's `@Nullable`.
- Gatherers accept null stream elements. Declare element type parameters as `INPUT extends @Nullable Object`.
- NullAway is disabled for test code.

### Style

- Every source file starts with the Apache 2.0 license header used in the existing files, with the current year.
- Use `final` on parameters and on local variables that aren't reassigned.
- Name type parameters in full (`INPUT`, `OUTPUT`, `MAPPED`, `SECOND`) rather than `T` or `R`.
- Keep methods and fields in alphabetical order unless another grouping is clearly easier to read.

## BigDecimal statistics engine

Every running and moving BigDecimal statistic runs on one engine. That covers sum, product, mean, geometric mean,
harmonic mean, RMS, median, variance, standard deviation and EMA.

- `BigDecimalRunningStatGatherer` and `BigDecimalMovingStatGatherer` are the public types the facade returns. Both
  extend the package-private `AbstractBigDecimalStatGatherer`. Together they handle:
  - the mapping function
  - null replacement (`treatNullAs*`)
  - `withMathContext` (default `DECIMAL64`)
  - `withOriginal()`
  - for the moving type, the `CircularBuffer` window and `excludePartialValues()`
- Each statistic is a package-private `StatisticAccumulator<OUTPUT>` in `StatisticAccumulators`, with the methods
  `add`, `evict` (moving form only), `isReady` and `value`. The accumulators share four internal building blocks:
  `CountSum`, `Moments`, `Product` and `SortedWindow`.

To add a statistic:

1. Add an accumulator to `StatisticAccumulators`, built on an existing building block where one fits. Implement
   `evict` if the statistic should have a moving form.
2. Add facade methods:
   - an identity form over `@Nullable BigDecimal` that passes `Function.identity()`
   - a `...By` form that takes `Function<INPUT, BigDecimal>`
   - each of these as a running form, a moving form, or both

   A population/sample choice is a `Dataset` parameter, not a separate method.
3. Don't write a new `Gatherer` class.

Numeric rules. The regression tests in `BigDecimalVarianceGathererTest`, `BigDecimalStandardDeviationGathererTest` and
`BigDecimalExponentialMovingAverageGathererTest` guard these. Don't "simplify" them:

- **Additive statistics** (sum, mean, harmonic mean, RMS, variance) round each input to the `MathContext` once, when it
  enters. After that they accumulate exactly, with no `MathContext` on add or subtract, so evicting a value from a
  moving window reverses its addition exactly.
- **`Moments` squares values exactly.** Variance is `(n*sumSq - sum*sum) / (n*n)`, or `/ (n*(n-1))` for a sample.
  Rounding happens only in that final divide. Rounding any earlier gives negative variance for inputs such as
  `1.000000000000001, 1.000000000000002, 1.000000000000003`.
- **Product, geometric mean and EMA** round at every step, because exact accumulation would add digits with every
  element.
- **Nulls:** a null that is still null after mapping and null replacement is skipped. It emits nothing and doesn't
  move the window.
- **`Dataset.Sample`:** statistics emit nothing until they have seen two values.
- **No `combiner()`:** these statistics emit per element, so they can't have a correct one (see "Combiner" above).

## Tests

- **Location and naming:** write one test class per gatherer in `src/test/java/com/ginsberg/gatherers4j/`, named
  `[Name]GathererTest` (or `[Name]GatherersTest` when the source file holds several gatherers).
- **Structure:** mark the Arrange, Act and Assert steps with comments. Use `@Nested` classes to group modes or
  overloads.
- **Required coverage:**
  - an empty stream
  - a single element
  - null elements
  - every null argument, asserting the exact message:
    `assertThatIllegalArgumentException().isThrownBy(...).withMessage("...")`
  - invalid numeric arguments, using `@ParameterizedTest` with `@ValueSource`
  - each fluent option
- **Parallel streams:** if the gatherer implements `combiner()`, test it with `@ParallelAndSequentialTest(values = {...})`
  from the `test` package. That annotation runs the test method once with a sequential stream and once with a
  parallel one.
- **Comparing `BigDecimal`:** compare with `.usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)`. For
  records that hold `BigDecimal` values, such as `WithOriginal`, use
  `.usingRecursiveFieldByFieldElementComparator(TestUtils.BIG_DECIMAL_RECURSIVE_COMPARISON)`.
- **Pitest:** run `./gradlew pitest` after adding a gatherer. A surviving mutant usually means an edge case has no
  test.

## Documentation

Every public factory method needs all three of these in the same change:

1. **A docs page** at `docs/gatherers4j/content/gatherers/<directory>/<methodName>.md`. Each `By` variant gets its own
   page.

   | Directory | `category:` value |
   |---|---|
   | `sequence-operations` | `Sequence Operations` |
   | `filtering-and-selection` | `Filtering and Selection` |
   | `grouping-and-windowing` | `Grouping and Windowing` |
   | `validation-and-constraints` | `Validation and Constraints` |
   | `mathematical` | `"Mathematical Operations"` |

   ```markdown
   ---
   title: "methodName()"
   linkTitle: "methodName()"
   show_in_table: true
   category: Filtering and Selection
   description: One sentence.
   ---
   ```

   The body has these sections, in order:
   - `### Implementation Notes`
   - `**Signature**` (or `**Signatures**`) listing the parameters
   - an `**Additional Methods**` table, if there are fluent options
   - `### Examples`, with one `####` heading per example and the output in a trailing `// [...]` comment

   Link JDK types with the `{{< jdklink linkName="..." package="java.base/..." >}}` shortcode. Start from an existing
   page in the same directory.
2. **A README row** in the matching category table. Keep the table alphabetical, and link to
   `https://tginsberg.github.io/gatherers4j/gatherers/<directory>/<methodname, lowercased>/`.
3. **A changelog bullet** under the unreleased version in `docs/gatherers4j/content/changelog/_index.md`, for example
   "+ Implement `name()` ...". Call out every rename, removal and behavior change. The root `CHANGELOG.md` only points
   to that page.

Add a JMH benchmark under `src/jmh/java/com/ginsberg/gatherers4j/bench/` only when a performance decision depends on
it.

## Versions and branches

- **`VERSION.txt`** holds the version, `X.Y.Z-SNAPSHOT` during development. On `main` and `release/*` the build
  publishes that string verbatim. On any other branch it publishes `<last branch path segment>-SNAPSHOT`.
- **Branches:** work for a release goes on `release/X.Y.Z`. Feature branches merge into it, and it merges to `main`
  when the release ships.

## Working tree

The working tree may be checked out with CRLF line endings (`core.autocrlf`). Keep each file's existing line endings,
and don't rewrite files whose content hasn't changed. Script-based edits are where this usually goes wrong.
