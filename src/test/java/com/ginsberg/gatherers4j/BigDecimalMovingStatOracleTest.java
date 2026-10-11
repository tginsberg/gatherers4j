/*
 * Copyright 2024-2026 Todd Ginsberg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ginsberg.gatherers4j;

import com.ginsberg.gatherers4j.dto.StatisticalSummary;
import com.ginsberg.gatherers4j.enums.Dataset;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/// Compares every output of the moving BigDecimal statistics against a naive oracle.
///
/// The moving statistics are incremental: each value is added to an accumulator when it enters the window and
/// evicted when it leaves. If eviction is not an exact inverse of addition, error builds up over a long stream
/// ("drift"). The oracle has no state. At each position it takes the values currently in the window and computes
/// the statistic from scratch, using the semantics documented in AGENTS.md.
///
/// All inputs are generated from a fixed seed, so a failure reproduces. The failure message names the case, the
/// position in the stream, and the window contents.
class BigDecimalMovingStatOracleTest {

    private static final long BASE_SEED = 20_260_101L;
    private static final int STREAM_LENGTH = 3_000;
    private static final MathContext SMALL = new MathContext(5);
    private static final MathContext WIDE_MC = new MathContext(60);

    // Generation settings. The `zeroDensity` is the expected number of zeros per window.
    private static final Inputs SIGNED = new Inputs(false, 6, 0.5);
    private static final Inputs POSITIVE = new Inputs(true, 6, 0.0);
    // Geometric mean takes the root of the product, which is computed via doubles to get a starting guess for the
    // Newton iteration. A product beyond the range of a double can't be seeded, so keep the magnitudes modest.
    private static final Inputs GEOMETRIC = new Inputs(true, 2, 0.5);

    static Stream<Case> cases() {
        final List<Case> cases = new ArrayList<>();
        for (final int window : new int[]{2, 3, 10, 50}) {
            for (final MathContext mc : new MathContext[]{MathContext.DECIMAL64, SMALL}) {
                for (final Profile profile : Profile.values()) {
                    cases.add(new Case(window, mc, profile));
                }
            }
        }
        return cases.stream();
    }

    // ---------------------------------------------------------------------------------------------------------
    // Statistics that accumulate exactly: the gatherer must match the oracle exactly.
    // ---------------------------------------------------------------------------------------------------------

    @ParameterizedTest(name = "{0}")
    @MethodSource("cases")
    void movingSum(final Case c) {
        verify(c, SIGNED, () -> Gatherers4j.movingSum(c.window()),
                (window, mc) -> roundAll(window, mc).stream().reduce(BigDecimal.ZERO, BigDecimal::add).round(mc),
                exactly()
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("cases")
    void movingMean(final Case c) {
        verify(c, SIGNED, () -> Gatherers4j.movingMean(c.window()),
                (window, mc) -> sum(roundAll(window, mc)).divide(BigDecimal.valueOf(window.size()), mc),
                exactly()
        );
    }

    // Defined for non-zero values. Mixed signs can sum the reciprocals to zero, so only positives are used.
    // The one rounding on entry is the rounding of the reciprocal of each value.
    @ParameterizedTest(name = "{0}")
    @MethodSource("cases")
    void movingHarmonicMean(final Case c) {
        verify(c, POSITIVE, () -> Gatherers4j.movingHarmonicMean(c.window()),
                (window, mc) -> {
                    final BigDecimal reciprocals = window.stream()
                            .map(it -> BigDecimal.ONE.divide(it, mc))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return BigDecimal.valueOf(window.size()).divide(reciprocals, mc);
                },
                exactly()
        );
    }

    // The one rounding on entry is the rounding of each square.
    @ParameterizedTest(name = "{0}")
    @MethodSource("cases")
    void movingRootMeanSquare(final Case c) {
        verify(c, SIGNED, () -> Gatherers4j.movingRootMeanSquare(c.window()),
                (window, mc) -> {
                    final BigDecimal sumOfSquares = window.stream()
                            .map(it -> it.multiply(it).round(mc))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return sumOfSquares.divide(BigDecimal.valueOf(window.size()), mc).sqrt(mc);
                },
                exactly()
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("cases")
    void movingVariancePopulation(final Case c) {
        verify(c, SIGNED, () -> Gatherers4j.movingVariance(Dataset.Population, c.window()),
                (window, mc) -> variance(window, Dataset.Population, mc),
                exactly()
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("cases")
    void movingVarianceSample(final Case c) {
        verify(c, SIGNED, () -> Gatherers4j.movingVariance(Dataset.Sample, c.window()),
                (window, mc) -> window.size() < 2 ? null : variance(window, Dataset.Sample, mc),
                exactly()
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("cases")
    void movingStandardDeviationPopulation(final Case c) {
        verify(c, SIGNED, () -> Gatherers4j.movingStandardDeviation(Dataset.Population, c.window()),
                (window, mc) -> variance(window, Dataset.Population, mc).sqrt(mc),
                exactly()
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("cases")
    void movingStandardDeviationSample(final Case c) {
        verify(c, SIGNED, () -> Gatherers4j.movingStandardDeviation(Dataset.Sample, c.window()),
                (window, mc) -> window.size() < 2 ? null : variance(window, Dataset.Sample, mc).sqrt(mc),
                exactly()
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("cases")
    void movingMedian(final Case c) {
        verify(c, SIGNED, () -> Gatherers4j.movingMedian(c.window()),
                (window, mc) -> {
                    final List<BigDecimal> sorted = new ArrayList<>(roundAll(window, mc));
                    sorted.sort(Comparator.naturalOrder());
                    final int n = sorted.size();
                    return n % 2 == 1
                            ? sorted.get(n / 2)
                            : sorted.get(n / 2 - 1).add(sorted.get(n / 2)).divide(BigDecimal.TWO, mc);
                },
                exactly()
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("cases")
    void movingStatistics(final Case c) {
        verify(c, SIGNED, () -> Gatherers4j.movingStatistics(c.window()),
                BigDecimalMovingStatOracleTest::summary,
                (actual, expected, position) -> sameSummary(actual, expected)
        );
    }

    // ---------------------------------------------------------------------------------------------------------
    // Statistics that round at every step. The gatherer multiplies the rounded window values together, rounding
    // each multiply, and never divides a value back out, so the oracle uses the exact product of the rounded window
    // values and the tolerance covers that rounding.
    //
    // Tolerance. Every multiply rounds to `precision` digits, so it adds a relative error of at most
    // 0.5 * 10^(1 - precision), and the standard deviation of one such error is below 0.3 * 10^(1 - precision).
    // These errors are independent and unbiased for the default HALF_EVEN rounding, so after k multiplies the
    // relative error is a random walk of about 0.3 * 10^(1 - precision) * sqrt(k). Every product the gatherer
    // reports is built from the values currently in the window, so k is about the number of values in the window,
    // however long the stream. The tolerance is 3 * 10^(1 - precision) * sqrt(k), about ten standard deviations. It
    // does not grow with the stream, so error that accumulates across evictions fails the test.
    // ---------------------------------------------------------------------------------------------------------

    @ParameterizedTest(name = "{0}")
    @MethodSource("cases")
    void movingProduct(final Case c) {
        verify(c, SIGNED, () -> Gatherers4j.movingProduct(c.window()),
                (window, mc) -> exactProduct(roundAll(window, mc)),
                (actual, expected, position) -> withinRelative(actual, expected, productTolerance(position))
        );
    }

    // Defined for non-negative values: an even root of a negative product is not real. Zero gives zero.
    // The expected root is computed to 60 digits, so it is the true geometric mean of the rounded window values.
    @ParameterizedTest(name = "{0}")
    @MethodSource("cases")
    void movingGeometricMean(final Case c) {
        verify(c, GEOMETRIC, () -> Gatherers4j.movingGeometricMean(c.window()),
                (window, mc) -> nthRoot(exactProduct(roundAll(window, mc)), window.size()),
                (actual, expected, position) -> {
                    // The product's error is divided across the n-th root. Add one ulp-scale term for the
                    // gatherer rounding its answer to `precision` digits.
                    final double ulp = Math.pow(10, 1 - position.mc().getPrecision());
                    final double tolerance = ulp * (3 * Math.sqrt(position.operations()) / position.count() + 1);
                    return withinRelative(actual, expected, tolerance);
                }
        );
    }

    // ---------------------------------------------------------------------------------------------------------
    // Harness
    // ---------------------------------------------------------------------------------------------------------

    /// Run the gatherer produced by `factory` over generated input, in both default and `excludePartialValues()`
    /// mode, and compare each output with the oracle.
    ///
    /// @param oracle Computes the statistic from scratch over the current window, or returns `null` if the
    ///               statistic has no value for a window of this size.
    private <OUTPUT> void verify(
            final Case c,
            final Inputs inputs,
            final Supplier<BigDecimalMovingStatGatherer<@Nullable BigDecimal, OUTPUT>> factory,
            final Oracle<OUTPUT> oracle,
            final Check<OUTPUT> check
    ) {
        final List<@Nullable BigDecimal> input = generate(c, inputs);
        // Nulls are skipped by the gatherer: they emit nothing and do not move the window.
        final List<BigDecimal> present = input.stream().filter(Objects::nonNull).toList();

        final List<Expected<OUTPUT>> expected = new ArrayList<>();
        for (int i = 0; i < present.size(); i++) {
            final List<BigDecimal> window = present.subList(Math.max(0, i + 1 - c.window()), i + 1);
            final OUTPUT value = oracle.compute(window, c.mc());
            if (value != null) {
                expected.add(new Expected<>(i + 1, window, value));
            }
        }

        for (final boolean excludePartial : new boolean[]{false, true}) {
            final var gatherer = factory.get().withMathContext(c.mc());
            if (excludePartial) {
                gatherer.excludePartialValues();
            }
            final List<Expected<OUTPUT>> wanted = expected.stream()
                    .filter(it -> !excludePartial || it.window().size() == c.window())
                    .toList();

            // Act
            final List<OUTPUT> actual = input.stream().gather(gatherer).toList();

            // Assert
            assertThat(actual)
                    .as("%s, excludePartialValues=%s: number of outputs", c, excludePartial)
                    .hasSize(wanted.size());
            for (int k = 0; k < wanted.size(); k++) {
                final Expected<OUTPUT> want = wanted.get(k);
                final Position position = new Position(want.seen(), want.window().size(), c.window(), c.mc());
                if (!check.matches(actual.get(k), want.value(), position)) {
                    fail("%s, excludePartialValues=%s, seed=%d%nafter %d non-null elements, window=%s%nactual:   %s%nexpected: %s",
                            c, excludePartial, c.seed(), want.seen(), want.window(), actual.get(k), want.value());
                }
            }
        }
    }

    private static List<@Nullable BigDecimal> generate(final Case c, final Inputs inputs) {
        final Random random = new Random(c.seed());
        final double zeroProbability = inputs.zeroDensity() / c.window();
        final List<@Nullable BigDecimal> values = new ArrayList<>(STREAM_LENGTH);
        for (int i = 0; i < STREAM_LENGTH; i++) {
            if (random.nextDouble() < 0.03) {
                values.add(null);
            } else if (random.nextDouble() < zeroProbability) {
                values.add(BigDecimal.ZERO.setScale(random.nextInt(4)));
            } else {
                final BigDecimal value = switch (c.profile()) {
                    case WIDE -> wideValue(random, 1 + random.nextInt(c.mc().getPrecision() + 4), inputs);
                    case NEAR_ONE -> nearOneValue(random, c.mc().getPrecision());
                    case PRECISION_EDGE -> precisionEdgeValue(random, c.mc().getPrecision(), inputs);
                };
                values.add(!inputs.positiveOnly() && random.nextBoolean() ? value.negate() : value);
            }
        }
        return values;
    }

    // A random number of significant digits at a random magnitude.
    private static BigDecimal wideValue(final Random random, final int digits, final Inputs inputs) {
        return withMagnitude(random, new BigInteger(digitString(random, digits, false)), digits, inputs);
    }

    // Values within a few hundred ulps of 1, like 1.000000000000001: sums of squares nearly cancel their squared sum.
    private static BigDecimal nearOneValue(final Random random, final int precision) {
        return BigDecimal.ONE.add(BigDecimal.valueOf(random.nextInt(2001) - 1000, precision - 1 + random.nextInt(4)));
    }

    // Values with one fewer, the same, or more digits than the MathContext, often leading with nines so that
    // rounding to the MathContext carries into a new digit.
    private static BigDecimal precisionEdgeValue(final Random random, final int precision, final Inputs inputs) {
        final int digits = Math.max(1, precision - 1 + random.nextInt(4));
        return withMagnitude(random, new BigInteger(digitString(random, digits, random.nextBoolean())), digits, inputs);
    }

    private static BigDecimal withMagnitude(
            final Random random,
            final BigInteger digits,
            final int digitCount,
            final Inputs inputs
    ) {
        final int exponent = random.nextInt(2 * inputs.maxExponent() + 1) - inputs.maxExponent();
        return new BigDecimal(digits, digitCount - 1).scaleByPowerOfTen(exponent);
    }

    private static String digitString(final Random random, final int digits, final boolean leadWithNines) {
        final StringBuilder sb = new StringBuilder(digits);
        for (int i = 0; i < digits; i++) {
            if (i == 0) {
                sb.append(leadWithNines ? 9 : 1 + random.nextInt(9));
            } else if (leadWithNines && i < digits - 1) {
                sb.append(9);
            } else {
                sb.append(random.nextInt(10));
            }
        }
        return sb.toString();
    }

    // ---------------------------------------------------------------------------------------------------------
    // Oracle
    // ---------------------------------------------------------------------------------------------------------

    private static List<BigDecimal> roundAll(final List<BigDecimal> window, final MathContext mc) {
        return window.stream().map(it -> it.round(mc)).toList();
    }

    private static BigDecimal sum(final List<BigDecimal> values) {
        return values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal exactProduct(final List<BigDecimal> values) {
        return values.stream().reduce(BigDecimal.ONE, BigDecimal::multiply);
    }

    /// A two-pass variance, which is a different route to the same exact quotient as the accumulator's
    /// `(n * sumSq - sum * sum) / n^2`. With `S` the exact sum of the rounded values, the sum of squared
    /// deviations from the mean is `sum((n*x - S)^2) / n^2`, so the population variance is
    /// `sum((n*x - S)^2) / n^3` and the sample variance is `sum((n*x - S)^2) / (n^2 * (n - 1))`.
    /// Only the final division rounds.
    private static BigDecimal variance(final List<BigDecimal> window, final Dataset dataset, final MathContext mc) {
        final List<BigDecimal> rounded = roundAll(window, mc);
        final BigDecimal n = BigDecimal.valueOf(rounded.size());
        final BigDecimal total = sum(rounded);
        BigDecimal squaredDeviations = BigDecimal.ZERO;
        for (final BigDecimal value : rounded) {
            final BigDecimal scaledDeviation = value.multiply(n).subtract(total);
            squaredDeviations = squaredDeviations.add(scaledDeviation.multiply(scaledDeviation));
        }
        final BigDecimal divisor = dataset == Dataset.Population
                ? n.pow(3)
                : n.pow(2).multiply(n.subtract(BigDecimal.ONE));
        return squaredDeviations.divide(divisor, mc);
    }

    private static StatisticalSummary summary(final List<BigDecimal> window, final MathContext mc) {
        final List<BigDecimal> rounded = roundAll(window, mc);
        final BigDecimal n = BigDecimal.valueOf(rounded.size());
        final BigDecimal total = sum(rounded);
        final BigDecimal populationVariance = variance(window, Dataset.Population, mc);
        final BigDecimal sampleVariance = rounded.size() > 1 ? variance(window, Dataset.Sample, mc) : null;
        return new StatisticalSummary(
                rounded.size(),
                total.round(mc),
                total.divide(n, mc),
                rounded.stream().min(Comparator.naturalOrder()).orElseThrow(),
                rounded.stream().max(Comparator.naturalOrder()).orElseThrow(),
                populationVariance,
                populationVariance.sqrt(mc),
                sampleVariance,
                sampleVariance == null ? null : sampleVariance.sqrt(mc)
        );
    }

    /// The n-th root of a non-negative number to 60 digits, by Newton's method. This is independent of the
    /// library's `MathUtils.nthRoot`, which is what the gatherer uses.
    private static BigDecimal nthRoot(final BigDecimal value, final int n) {
        if (value.signum() == 0) {
            return BigDecimal.ZERO;
        }
        final BigDecimal bigN = BigDecimal.valueOf(n);
        BigDecimal x = new BigDecimal(Math.pow(value.doubleValue(), 1.0 / n));
        for (int i = 0; i < 100; i++) {
            final BigDecimal next = x.multiply(BigDecimal.valueOf(n - 1))
                    .add(value.divide(x.pow(n - 1, WIDE_MC), WIDE_MC))
                    .divide(bigN, WIDE_MC);
            final boolean converged = next.subtract(x).abs().compareTo(next.movePointLeft(50)) <= 0;
            x = next;
            if (converged) {
                break;
            }
        }
        return x;
    }

    // ---------------------------------------------------------------------------------------------------------
    // Comparison
    // ---------------------------------------------------------------------------------------------------------

    private static Check<BigDecimal> exactly() {
        return (actual, expected, position) -> actual.compareTo(expected) == 0;
    }

    private static boolean sameSummary(final StatisticalSummary actual, final StatisticalSummary expected) {
        return actual.count() == expected.count()
                && actual.sum().compareTo(expected.sum()) == 0
                && actual.mean().compareTo(expected.mean()) == 0
                && actual.min().compareTo(expected.min()) == 0
                && actual.max().compareTo(expected.max()) == 0
                && actual.populationVariance().compareTo(expected.populationVariance()) == 0
                && actual.populationStandardDeviation().compareTo(expected.populationStandardDeviation()) == 0
                && sameNullable(actual.sampleVariance(), expected.sampleVariance())
                && sameNullable(actual.sampleStandardDeviation(), expected.sampleStandardDeviation());
    }

    private static boolean sameNullable(final @Nullable BigDecimal actual, final @Nullable BigDecimal expected) {
        if (actual == null || expected == null) {
            return actual == null && expected == null;
        }
        return actual.compareTo(expected) == 0;
    }

    private static double productTolerance(final Position position) {
        return 3 * Math.pow(10, 1 - position.mc().getPrecision()) * Math.sqrt(position.operations());
    }

    /// Zero must be exactly zero. Anything else must be within a relative tolerance.
    private static boolean withinRelative(final BigDecimal actual, final BigDecimal expected, final double tolerance) {
        if (expected.signum() == 0) {
            return actual.signum() == 0;
        }
        if (actual.signum() != expected.signum()) {
            return false;
        }
        final BigDecimal relativeError = actual.subtract(expected).abs().divide(expected.abs(), WIDE_MC);
        return relativeError.doubleValue() <= tolerance;
    }

    // ---------------------------------------------------------------------------------------------------------
    // Types
    // ---------------------------------------------------------------------------------------------------------

    enum Profile {
        WIDE(0), NEAR_ONE(1), PRECISION_EDGE(2);

        // Part of each case's seed, fixed here so reordering the constants doesn't change the generated inputs.
        private final int seedOffset;

        Profile(final int seedOffset) {
            this.seedOffset = seedOffset;
        }
    }

    record Case(int window, MathContext mc, Profile profile) {
        long seed() {
            return BASE_SEED + 1_000L * window + 10L * mc.getPrecision() + profile.seedOffset;
        }

        @Override
        public String toString() {
            return "window=%d, precision=%d, profile=%s".formatted(window, mc.getPrecision(), profile);
        }
    }

    private record Inputs(boolean positiveOnly, int maxExponent, double zeroDensity) {
    }

    private record Expected<OUTPUT>(int seen, List<BigDecimal> window, OUTPUT value) {
    }

    /// Where the comparison is in the stream. `seen` non-null elements have entered the gatherer.
    private record Position(int seen, int count, int windowSize, MathContext mc) {
        // One rounded multiply per value in the window.
        double operations() {
            return count;
        }
    }

    @FunctionalInterface
    private interface Oracle<OUTPUT> {
        @Nullable OUTPUT compute(List<BigDecimal> window, MathContext mc);
    }

    @FunctionalInterface
    private interface Check<OUTPUT> {
        boolean matches(OUTPUT actual, OUTPUT expected, Position position);
    }
}
