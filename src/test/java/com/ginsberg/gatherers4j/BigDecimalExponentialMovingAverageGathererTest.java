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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Stream;

import static com.ginsberg.gatherers4j.util.TestUtils.BIG_DECIMAL_RECURSIVE_COMPARISON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class BigDecimalExponentialMovingAverageGathererTest {

    @ParameterizedTest
    @ValueSource(doubles = {Double.MIN_VALUE, 0.9999999999})
    void alphaMustBeInRange(final double value) {
        assertDoesNotThrow(() ->
                Gatherers4j.exponentialMovingAverageWithAlpha(value)
        );
    }

    @ParameterizedTest
    @ValueSource(doubles = {-Double.MAX_VALUE, -Double.MIN_VALUE, 1.0, Double.MAX_VALUE})
    void alphaMustNotBeOutOfRange(final double value) {
        assertThatIllegalArgumentException().isThrownBy(() ->
                Gatherers4j.exponentialMovingAverageWithAlpha(value)
        );
    }

    @Test
    void exponentialMovingAverageWithAlpha() {
        // Arrange
        final Stream<BigDecimal> input = Stream.of("10.5", "15.2", "8.7", "12.0", "9.8").map(BigDecimal::new);

        // Act
        final List<BigDecimal> output = input
                .gather(Gatherers4j.exponentialMovingAverageWithAlpha(0.3))
                .toList();

        // Assert
        assertThat(output)
                .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                .containsExactly(
                        new BigDecimal("10.5"),
                        new BigDecimal("11.91"),
                        new BigDecimal("10.947"),
                        new BigDecimal("11.2629"),
                        new BigDecimal("10.82403")
                );
    }

    @Test
    void exponentialMovingAverageWithAlphaBy() {
        // Arrange
        final Stream<TestValueHolder> input = Stream.of("10.5", "15.2", "8.7", "12.0", "9.8")
                .map(BigDecimal::new)
                .map(it -> new TestValueHolder(0, it));

        // Act
        final List<BigDecimal> output = input
                .gather(Gatherers4j.exponentialMovingAverageWithAlphaBy(0.3, TestValueHolder::value))
                .map(it -> it.setScale(4, RoundingMode.HALF_UP).stripTrailingZeros())
                .toList();

        // Assert
        assertThat(output)
                .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                .containsExactly(
                        new BigDecimal("10.5"),
                        new BigDecimal("11.91"),
                        new BigDecimal("10.947"),
                        new BigDecimal("11.2629"),
                        new BigDecimal("10.824")
                );
    }

    @Test
    void exponentialMovingAverageWithOriginal() {
        // Arrange
        final Stream<BigDecimal> input = Stream.of("10.5", "15.2", "8.7", "12.0", "9.8").map(BigDecimal::new);

        // Act
        final List<WithOriginal<BigDecimal, BigDecimal>> output = input
                .gather(Gatherers4j.exponentialMovingAverageWithAlpha(0.3).withOriginal())
                .toList();

        // Assert
        assertThat(output)
                .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                .containsExactly(
                        new WithOriginal<>(new BigDecimal("10.5"), new BigDecimal("10.5")),
                        new WithOriginal<>(new BigDecimal("15.2"), new BigDecimal("11.91")),
                        new WithOriginal<>(new BigDecimal("8.7"), new BigDecimal("10.947")),
                        new WithOriginal<>(new BigDecimal("12.0"), new BigDecimal("11.2629")),
                        new WithOriginal<>(new BigDecimal("9.8"), new BigDecimal("10.82403"))
                );
    }

    @Test
    void exponentialMovingAverageWithPeriod() {
        // Arrange
        final Stream<BigDecimal> input = Stream.of("10.5", "15.2", "8.7", "12.0", "9.8").map(BigDecimal::new);

        // Act
        final List<BigDecimal> output = input
                .gather(Gatherers4j.exponentialMovingAverageWithPeriod(3))
                .toList();

        // Assert
        assertThat(output)
                .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                .containsExactly(
                        new BigDecimal("10.5"),
                        new BigDecimal("12.85"),
                        new BigDecimal("10.775"),
                        new BigDecimal("11.3875"),
                        new BigDecimal("10.59375")
                );
    }

    @Test
    void exponentialMovingAverageWithPeriodBy() {
        // Arrange
        final Stream<TestValueHolder> input = Stream.of("10.5", "15.2", "8.7", "12.0", "9.8")
                .map(BigDecimal::new)
                .gather(Gatherers4j.mapIndexed(TestValueHolder::new));

        // Act
        final List<BigDecimal> output = input
                .gather(Gatherers4j.exponentialMovingAverageWithPeriodBy(3, TestValueHolder::value))
                .map(it -> it.setScale(4, RoundingMode.HALF_UP).stripTrailingZeros())
                .toList();

        // Assert
        assertThat(output)
                .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                .containsExactly(
                        new BigDecimal("10.5"),
                        new BigDecimal("12.85"),
                        new BigDecimal("10.775"),
                        new BigDecimal("11.3875"),
                        new BigDecimal("10.5938")
                );
    }

    @Test
    @SuppressWarnings("DataFlowIssue")
    void mathContextCannotBeNull() {
        assertThatIllegalArgumentException().isThrownBy(() ->
                Gatherers4j.exponentialMovingAverageWithAlpha(0.4).withMathContext(null)
        );
    }

    @ParameterizedTest
    @ValueSource(ints = {2, Integer.MAX_VALUE})
    void periodMustBeInRange(final int value) {
        assertDoesNotThrow(() ->
                Gatherers4j.exponentialMovingAverageWithPeriod(value)
        );
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 1})
    void periodMustNotBeOutOfRange(final int value) {
        assertThatIllegalArgumentException().isThrownBy(() ->
                Gatherers4j.exponentialMovingAverageWithPeriod(value)
        );
    }

    @Test
    void replaceNullWithOne() {
        // Arrange
        final Stream<BigDecimal> input = Stream.of("10.5", null, "15.2", null, "8.7", null, "12.0", null, "9.8")
                .map(it -> it == null ? null : new BigDecimal(it));

        // Act
        final List<BigDecimal> output = input
                .gather(Gatherers4j.exponentialMovingAverageWithAlpha(0.3).treatNullAs(BigDecimal.ONE))
                .map(it -> it.setScale(4, RoundingMode.HALF_UP).stripTrailingZeros())
                .toList();

        // Assert
        assertThat(output)
                .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                .containsExactly(
                        new BigDecimal("10.5"),
                        new BigDecimal("7.65"),
                        new BigDecimal("9.915"),
                        new BigDecimal("7.2405"),
                        new BigDecimal("7.6784"),
                        new BigDecimal("5.6748"),
                        new BigDecimal("7.5724"),
                        new BigDecimal("5.6007"),
                        new BigDecimal("6.8605")
                );
    }

    @Test
    void replaceNullWithZero() {
        // Arrange
        final Stream<BigDecimal> input = Stream.of("10.5", null, "15.2", null, "8.7", null, "12.0", null, "9.8")
                .map(it -> it == null ? null : new BigDecimal(it));

        // Act
        final List<BigDecimal> output = input
                .gather(Gatherers4j.exponentialMovingAverageWithAlpha(0.3).treatNullAsZero())
                .map(it -> it.setScale(4, RoundingMode.HALF_UP).stripTrailingZeros())
                .toList();

        // Assert
        assertThat(output)
                .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                .containsExactly(
                        new BigDecimal("10.5"),
                        new BigDecimal("7.35"),
                        new BigDecimal("9.705"),
                        new BigDecimal("6.7935"),
                        new BigDecimal("7.3655"),
                        new BigDecimal("5.1558"),
                        new BigDecimal("7.2091"),
                        new BigDecimal("5.0463"),
                        new BigDecimal("6.4724")
                );
    }

    @Test
    void skipsNulls() {
        // Arrange
        final Stream<BigDecimal> input = Stream.of("10.5", null, "15.2", null, "8.7", null, "12.0", null, "9.8")
                .map(it -> it == null ? null : new BigDecimal(it));

        // Act
        final List<BigDecimal> output = input
                .gather(Gatherers4j.exponentialMovingAverageWithAlpha(0.3))
                .map(it -> it.setScale(4, RoundingMode.HALF_UP).stripTrailingZeros())
                .toList();

        // Assert
        assertThat(output)
                .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                .containsExactly(
                        new BigDecimal("10.5"),
                        new BigDecimal("11.91"),
                        new BigDecimal("10.947"),
                        new BigDecimal("11.2629"),
                        new BigDecimal("10.824")
                );
    }

}