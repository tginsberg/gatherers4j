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
import java.util.List;
import java.util.stream.Stream;

import static com.ginsberg.gatherers4j.util.TestUtils.BIG_DECIMAL_RECURSIVE_COMPARISON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class BigDecimalProductGathererTest {

    @Nested
    class Moving {

        @Test
        void ignoresNulls() {
            // Arrange
            final Stream<BigDecimal> input = Stream.of(null, BigDecimal.TWO, BigDecimal.TWO, BigDecimal.TEN);

            // Act
            final List<BigDecimal> output = input
                    .gather(Gatherers4j.movingProduct(2))
                    .toList();

            // Assert
            assertThat(output)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(
                            new BigDecimal("2"),
                            new BigDecimal("4"),
                            new BigDecimal("20")
                    );
        }

        @Test
        @SuppressWarnings("DataFlowIssue")
        void mathContextCannotBeNull() {
            assertThatIllegalArgumentException().isThrownBy(() ->
                    Gatherers4j.movingProduct(2).withMathContext(null)
            );
        }

        @Test
        void movingProduct() {
            // Arrange
            final Stream<BigDecimal> input = Stream.of("1", "2", "3", "4").map(BigDecimal::new);

            // Act
            final List<BigDecimal> output = input
                    .gather(Gatherers4j.movingProduct(2))
                    .toList();

            // Assert
            assertThat(output)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(
                            new BigDecimal("1"),
                            new BigDecimal("2"),
                            new BigDecimal("6"),
                            new BigDecimal("12")
                    );
        }

        @Test
        void movingProductWithZero() {
            // Arrange
            final Stream<BigDecimal> input = Stream.of("1", "0", "0", "0", "3", "4").map(BigDecimal::new);

            // Act
            final List<BigDecimal> output = input
                    .gather(Gatherers4j.movingProduct(2))
                    .toList();

            // Assert
            assertThat(output)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(
                            new BigDecimal("1"),
                            new BigDecimal("0"),
                            new BigDecimal("0"),
                            new BigDecimal("0"),
                            new BigDecimal("0"),
                            new BigDecimal("12")
                    );
        }

        @Test
        void movingProductExcludingPartialValues() {
            // Arrange
            final Stream<BigDecimal> input = Stream.of("1", "2", "3", "4").map(BigDecimal::new);

            // Act
            final List<BigDecimal> output = input
                    .gather(Gatherers4j.movingProduct(2).excludePartialValues())
                    .toList();

            // Assert
            assertThat(output)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(
                            new BigDecimal("2"),
                            new BigDecimal("6"),
                            new BigDecimal("12")
                    );
        }

        @Test
        void movingProductBy() {
            // Arrange
            final List<TestValueHolder> input = List.of(
                    new TestValueHolder(1, new BigDecimal("1")),
                    new TestValueHolder(2, new BigDecimal("2")),
                    new TestValueHolder(3, new BigDecimal("10")),
                    new TestValueHolder(4, new BigDecimal("20")),
                    new TestValueHolder(5, new BigDecimal("30"))
            );

            // Act
            final List<BigDecimal> output = input.stream()
                    .gather(Gatherers4j.movingProductBy(2, TestValueHolder::value))
                    .toList();

            // Assert
            assertThat(output)
                    .usingRecursiveFieldByFieldElementComparator(BIG_DECIMAL_RECURSIVE_COMPARISON)
                    .containsExactly(
                            new BigDecimal("1"),
                            new BigDecimal("2"),
                            new BigDecimal("20"),
                            new BigDecimal("200"),
                            new BigDecimal("600")
                    );
        }

        @Test
        void movingProductWithPartialsWithOriginal() {
            // Arrange
            final Stream<BigDecimal> input = Stream.of("1", "2", "3", "4").map(BigDecimal::new);

            // Act
            final List<WithOriginal<BigDecimal, BigDecimal>> output = input
                    .gather(Gatherers4j.movingProduct(2)
                            .withOriginal()
                    )
                    .toList();

            // Assert
            assertThat(output)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(
                            new WithOriginal<>(new BigDecimal("1"), new BigDecimal("1")),
                            new WithOriginal<>(new BigDecimal("2"), new BigDecimal("2")),
                            new WithOriginal<>(new BigDecimal("3"), new BigDecimal("6")),
                            new WithOriginal<>(new BigDecimal("4"), new BigDecimal("12"))
                    );
        }

        @Test
        void treatNullAsOne() {
            // Arrange
            final Stream<BigDecimal> input = Stream.of(
                    new BigDecimal("2"),
                    new BigDecimal("3"),
                    null,
                    new BigDecimal("4")
            );

            // Act
            final List<BigDecimal> output = input
                    .gather(Gatherers4j.movingProduct(2).treatNullAsOne())
                    .toList();

            // Assert
            assertThat(output)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(
                            new BigDecimal("2"),
                            new BigDecimal("6"),
                            new BigDecimal("3"),
                            new BigDecimal("4")
                    );
        }

        @ParameterizedTest(name = "windowSize of {0}")
        @ValueSource(ints = {-1, 0, 1})
        void windowSizeMustBeGreaterThanOne(final int windowSize) {
            assertThatIllegalArgumentException().isThrownBy(() ->
                    Gatherers4j.movingProduct(windowSize)
            );
        }
    }

    @Nested
    class Running {
        @Test
        void ignoresNull() {
            // Arrange
            final Stream<BigDecimal> input = Stream.of(
                    new BigDecimal("2"),
                    new BigDecimal("3"),
                    null,
                    new BigDecimal("4")
            );

            // Act
            final List<BigDecimal> output = input
                    .gather(Gatherers4j.runningProduct())
                    .toList();

            // Assert
            assertThat(output)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(
                            new BigDecimal("2"),
                            new BigDecimal("6"),
                            new BigDecimal("24")
                    );
        }

        @Test
        @SuppressWarnings("DataFlowIssue")
        void mathContextCannotBeNull() {
            assertThatIllegalArgumentException().isThrownBy(() ->
                    Gatherers4j.runningProduct().withMathContext(null)
            );
        }

        @Test
        void runningProduct() {
            // Arrange
            final Stream<BigDecimal> input = Stream.of("1", "2", "3").map(BigDecimal::new);

            // Act
            final List<BigDecimal> output = input
                    .gather(Gatherers4j.runningProduct())
                    .toList();

            // Assert
            assertThat(output)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(
                            BigDecimal.ONE,
                            new BigDecimal("2"),
                            new BigDecimal("6")
                    );
        }

        @Test
        void runningProductBy() {
            // Arrange
            final Stream<TestValueHolder> input = Stream.of(
                    new TestValueHolder(1, new BigDecimal("1.0")),
                    new TestValueHolder(2, new BigDecimal("2.0")),
                    new TestValueHolder(3, new BigDecimal("10.0")),
                    new TestValueHolder(4, new BigDecimal("20.0")),
                    new TestValueHolder(5, new BigDecimal("30.0"))
            );

            // Act
            final List<BigDecimal> output = input.gather(Gatherers4j.runningProductBy(TestValueHolder::value)).toList();

            // Assert
            assertThat(output)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(
                            BigDecimal.ONE,
                            new BigDecimal("2"),
                            new BigDecimal("20"),
                            new BigDecimal("400"),
                            new BigDecimal("12000")
                    );
        }

        @Test
        void treatNullAsOne() {
            // Arrange
            final Stream<BigDecimal> input = Stream.of(
                    new BigDecimal("2"),
                    new BigDecimal("3"),
                    null,
                    new BigDecimal("4")
            );

            // Act
            final List<BigDecimal> output = input
                    .gather(Gatherers4j.runningProduct().treatNullAsOne())
                    .toList();

            // Assert
            assertThat(output)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(
                            new BigDecimal("2"),
                            new BigDecimal("6"),
                            new BigDecimal("6"),
                            new BigDecimal("24")
                    );
        }

        @Test
        void withOriginalBigDecimal() {
            // Arrange
            final Stream<BigDecimal> input = Stream.of("1", "2", "3").map(BigDecimal::new);

            // Act
            final List<WithOriginal<BigDecimal, BigDecimal>> output = input
                    .gather(Gatherers4j.runningProduct().withOriginal())
                    .toList();

            // Assert
            assertThat(output)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(
                            new WithOriginal<>(BigDecimal.ONE, BigDecimal.ONE),
                            new WithOriginal<>(new BigDecimal("2"), new BigDecimal("2")),
                            new WithOriginal<>(new BigDecimal("3"), new BigDecimal("6"))

                    );
        }
    }
}