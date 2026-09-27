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

import com.ginsberg.gatherers4j.dto.WithOriginal;
import com.ginsberg.gatherers4j.util.TestValueHolder;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import java.util.stream.Stream;

import static com.ginsberg.gatherers4j.util.TestUtils.BIG_DECIMAL_RECURSIVE_COMPARISON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class BigDecimalHarmonicMeanGathererTest {

    @Nested
    class Moving {

        @Test
        void ignoresNulls() {
            // Arrange
            final Stream<BigDecimal> input = Stream.of(null, BigDecimal.ONE, BigDecimal.TWO, new BigDecimal("4"));

            // Act
            final List<BigDecimal> output = input
                    .gather(Gatherers4j.movingHarmonicMean(2))
                    .toList();

            // Assert
            assertThat(output)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(
                            new BigDecimal("1"),
                            new BigDecimal("1.333333333333333"),
                            new BigDecimal("2.666666666666667")
                    );
        }

        @Test
        void movingHarmonicMean() {
            // Arrange
            final Stream<BigDecimal> input = Stream.of("1", "2", "4", "8").map(BigDecimal::new);

            // Act
            final List<BigDecimal> output = input
                    .gather(Gatherers4j.movingHarmonicMean(2))
                    .toList();

            // Assert
            assertThat(output)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(
                            new BigDecimal("1"),
                            new BigDecimal("1.333333333333333"),
                            new BigDecimal("2.666666666666667"),
                            new BigDecimal("5.333333333333333")
                    );
        }

        @Test
        void movingHarmonicMeanBy() {
            // Arrange
            final List<TestValueHolder> input = List.of(
                    new TestValueHolder(1, new BigDecimal("1")),
                    new TestValueHolder(2, new BigDecimal("2")),
                    new TestValueHolder(3, new BigDecimal("4")),
                    new TestValueHolder(4, new BigDecimal("8"))
            );

            // Act
            final List<BigDecimal> output = input.stream()
                    .gather(Gatherers4j.movingHarmonicMeanBy(2, TestValueHolder::value))
                    .toList();

            // Assert
            assertThat(output)
                    .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                    .containsExactly(
                            new BigDecimal("1"),
                            new BigDecimal("1.333333333333333"),
                            new BigDecimal("2.666666666666667"),
                            new BigDecimal("5.333333333333333")
                    );
        }

        @Test
        void movingHarmonicMeanExcludingPartialValues() {
            // Arrange
            final Stream<BigDecimal> input = Stream.of("1", "2", "4", "8").map(BigDecimal::new);

            // Act
            final List<BigDecimal> output = input
                    .gather(Gatherers4j.movingHarmonicMean(2).excludePartialValues())
                    .toList();

            // Assert
            assertThat(output)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(
                            new BigDecimal("1.333333333333333"),
                            new BigDecimal("2.666666666666667"),
                            new BigDecimal("5.333333333333333")
                    );
        }

        @Test
        void treatNullAsOne() {
            // Arrange
            final Stream<BigDecimal> input = Stream.of(
                    new BigDecimal("2"),
                    null,
                    new BigDecimal("8")
            );

            // Act
            final List<BigDecimal> output = input
                    .gather(Gatherers4j.movingHarmonicMean(2).treatNullAsOne())
                    .toList();

            // Assert
            assertThat(output)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(
                            new BigDecimal("2"),
                            new BigDecimal("1.333333333333333"),
                            new BigDecimal("1.777777777777778")
                    );
        }

        @Test
        void zeroInWindowIsUndefined() {
            // Arrange
            final Stream<BigDecimal> input = Stream.of(BigDecimal.ONE, BigDecimal.ZERO);

            // Act/Assert
            assertThatExceptionOfType(ArithmeticException.class).isThrownBy(() ->
                    input.gather(Gatherers4j.movingHarmonicMean(2)).toList()
            );
        }

        @ParameterizedTest(name = "windowSize of {0}")
        @ValueSource(ints = {-1, 0, 1})
        void windowSizeMustBeGreaterThanOne(final int windowSize) {
            assertThatIllegalArgumentException().isThrownBy(() ->
                    Gatherers4j.movingHarmonicMean(windowSize)
            );
        }
    }

    @Nested
    class Running {

        @Test
        void emptyStream() {
            // Arrange
            final Stream<BigDecimal> input = Stream.empty();

            // Act
            final List<BigDecimal> output = input
                    .gather(Gatherers4j.runningHarmonicMean())
                    .toList();

            // Assert
            assertThat(output).isEmpty();
        }

        @Test
        void ignoresNulls() {
            // Arrange
            final Stream<BigDecimal> input = Stream.of(null, BigDecimal.ONE, BigDecimal.TWO);

            // Act
            final List<BigDecimal> output = input
                    .gather(Gatherers4j.runningHarmonicMean())
                    .toList();

            // Assert
            assertThat(output)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(
                            new BigDecimal("1"),
                            new BigDecimal("1.333333333333333")
                    );
        }

        @SuppressWarnings("DataFlowIssue")
        @Test
        void mappingFunctionMustNotBeNull() {
            assertThatIllegalArgumentException().isThrownBy(() ->
                    Gatherers4j.runningHarmonicMeanBy(null)
            );
        }

        @SuppressWarnings("DataFlowIssue")
        @Test
        void mathContextCannotBeNull() {
            assertThatIllegalArgumentException().isThrownBy(() ->
                    Gatherers4j.runningHarmonicMean().withMathContext(null)
            );
        }

        @Test
        void mathContextChange() {
            // Arrange
            final Stream<BigDecimal> input = Stream.of("1", "2", "4").map(BigDecimal::new);

            // Act
            final List<BigDecimal> output = input
                    .gather(Gatherers4j.runningHarmonicMean().withMathContext(new MathContext(3)))
                    .toList();

            // Assert
            assertThat(output)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(
                            new BigDecimal("1"),
                            new BigDecimal("1.33"),
                            new BigDecimal("1.71")
                    );
        }

        @Test
        void runningHarmonicMean() {
            // Arrange
            final Stream<BigDecimal> input = Stream.of("1", "2", "4").map(BigDecimal::new);

            // Act
            final List<BigDecimal> output = input
                    .gather(Gatherers4j.runningHarmonicMean())
                    .toList();

            // Assert
            assertThat(output)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(
                            new BigDecimal("1"),
                            new BigDecimal("1.333333333333333"),
                            new BigDecimal("1.714285714285714")
                    );
        }

        @Test
        void runningHarmonicMeanBy() {
            // Arrange
            final List<TestValueHolder> input = List.of(
                    new TestValueHolder(1, new BigDecimal("1")),
                    new TestValueHolder(2, new BigDecimal("2")),
                    new TestValueHolder(3, new BigDecimal("4"))
            );

            // Act
            final List<BigDecimal> output = input.stream()
                    .gather(Gatherers4j.runningHarmonicMeanBy(TestValueHolder::value))
                    .toList();

            // Assert
            assertThat(output)
                    .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                    .containsExactly(
                            new BigDecimal("1"),
                            new BigDecimal("1.333333333333333"),
                            new BigDecimal("1.714285714285714")
                    );
        }

        @Test
        void singleElementStream() {
            // Arrange
            final Stream<BigDecimal> input = Stream.of(new BigDecimal("4"));

            // Act
            final List<BigDecimal> output = input
                    .gather(Gatherers4j.runningHarmonicMean())
                    .toList();

            // Assert
            assertThat(output)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(new BigDecimal("4"));
        }

        @Test
        void treatNullAsNonZero() {
            // Arrange
            final Stream<BigDecimal> input = Stream.of(null, BigDecimal.ONE, null, BigDecimal.ONE);

            // Act
            final List<BigDecimal> output = input
                    .gather(Gatherers4j.runningHarmonicMean().treatNullAs(BigDecimal.TEN))
                    .toList();

            // Assert
            assertThat(output)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(
                            new BigDecimal("10"),
                            new BigDecimal("1.818181818181818"),
                            new BigDecimal("2.5"),
                            new BigDecimal("1.818181818181818")
                    );
        }

        @Test
        void treatNullAsOne() {
            // Arrange
            final Stream<BigDecimal> input = Stream.of(
                    new BigDecimal("2"),
                    null,
                    new BigDecimal("8")
            );

            // Act
            final List<BigDecimal> output = input
                    .gather(Gatherers4j.runningHarmonicMean().treatNullAsOne())
                    .toList();

            // Assert
            assertThat(output)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(
                            new BigDecimal("2"),
                            new BigDecimal("1.333333333333333"),
                            new BigDecimal("1.846153846153846")
                    );
        }

        @Test
        void treatNullAsZeroIsUndefined() {
            // Arrange
            final Stream<BigDecimal> input = Stream.of(BigDecimal.ONE, null);

            // Act/Assert
            assertThatExceptionOfType(ArithmeticException.class).isThrownBy(() ->
                    input.gather(Gatherers4j.runningHarmonicMean().treatNullAsZero()).toList()
            );
        }

        @Test
        void withOriginalBigDecimal() {
            // Arrange
            final Stream<BigDecimal> input = Stream.of("1", "2", "4").map(BigDecimal::new);

            // Act
            final List<WithOriginal<BigDecimal, BigDecimal>> output = input
                    .gather(Gatherers4j.runningHarmonicMean().withOriginal())
                    .toList();

            // Assert
            assertThat(output)
                    .map(WithOriginal::calculated)
                    .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                    .containsExactly(
                            new BigDecimal("1"),
                            new BigDecimal("1.333333333333333"),
                            new BigDecimal("1.714285714285714")
                    );

            assertThat(output)
                    .map(WithOriginal::original)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(
                            new BigDecimal("1"),
                            new BigDecimal("2"),
                            new BigDecimal("4")
                    );
        }

        @Test
        void withOriginalRecordByMappedField() {
            // Arrange
            final List<TestValueHolder> input = List.of(
                    new TestValueHolder(1, new BigDecimal("1")),
                    new TestValueHolder(2, new BigDecimal("2")),
                    new TestValueHolder(3, new BigDecimal("4"))
            );

            // Act
            final List<WithOriginal<TestValueHolder, BigDecimal>> output = input.stream()
                    .gather(Gatherers4j.runningHarmonicMeanBy(TestValueHolder::value).withOriginal())
                    .toList();

            // Assert
            assertThat(output)
                    .extracting(WithOriginal::calculated)
                    .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                    .containsExactly(
                            new BigDecimal("1"),
                            new BigDecimal("1.333333333333333"),
                            new BigDecimal("1.714285714285714")
                    );

            assertThat(output)
                    .map(WithOriginal::original)
                    .containsExactlyInAnyOrderElementsOf(input);
        }

        @Test
        void zeroIsUndefined() {
            // Arrange
            final Stream<BigDecimal> input = Stream.of(BigDecimal.ONE, BigDecimal.ZERO);

            // Act/Assert
            assertThatExceptionOfType(ArithmeticException.class).isThrownBy(() ->
                    input.gather(Gatherers4j.runningHarmonicMean()).toList()
            );
        }
    }
}
