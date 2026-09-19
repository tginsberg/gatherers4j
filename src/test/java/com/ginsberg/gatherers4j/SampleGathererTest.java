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

import org.apache.commons.statistics.inference.AlternativeHypothesis;
import org.apache.commons.statistics.inference.BinomialTest;
import org.apache.commons.statistics.inference.ChiSquareTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class SampleGathererTest {

    private static final double ALPHA = 1e-6;

    @Nested
    class FixedSizeReservoirSampling {

        @Test
        void sampleSizeOneOnTwoElementStreamShouldReturnBothEqually() {
            // Arrange
            final int runs = 10_000;
            final Random random = new Random(42);
            final BinomialTest binomialTest = BinomialTest.withDefaults().with(AlternativeHypothesis.TWO_SIDED);
            int countA = 0;

            // Act
            for (int i = 0; i < runs; i++) {
                if ("A".equals(Stream.of("A", "B").gather(Gatherers4j.sampleFixedSize(1, random)).toList().getFirst())) {
                    countA++;
                }
            }

            // Assert
            assertThat(binomialTest.test(runs, countA, 0.5).reject(ALPHA)).isFalse();
        }

        @Test
        void includesAllElementsWhenSampleSizeNotMet() {
            // Arrange
            final Stream<String> input = Stream.of("A", "B", "C");

            // Act
            final List<String> output = input.gather(Gatherers4j.sampleFixedSize(4)).toList();

            // Assert
            assertThat(output).containsExactly("A", "B", "C");
        }

        @Test
        void inclusionProbability() {
            // Arrange
            final List<Integer> input = List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
            final int runs = 10_000;
            final Random random = new Random(42);
            final int sampleSize = 4;
            final int[] counts = new int[input.size()];
            final double expectedProbability = (double) sampleSize / input.size();
            final BinomialTest binomialTest = BinomialTest.withDefaults().with(AlternativeHypothesis.TWO_SIDED);

            // Act
            for (int i = 0; i < runs; i++) {
                input.stream().gather(Gatherers4j.sampleFixedSize(sampleSize, random)).forEach(it -> counts[it]++);
            }

            // Assert
            for (int i = 0; i < counts.length; i++) {
                assertThat(binomialTest.test(runs, counts[i], expectedProbability).reject(ALPHA / counts.length))
                        .as("element %d selected %d times", i, counts[i])
                        .isFalse();
            }
        }

        @ParameterizedTest(name = "sampleSize of {0}")
        @ValueSource(ints = {-1, 0})
        void sampleSizeMustBeAtLeast1(int size) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> Gatherers4j.sampleFixedSize(size)
            );
        }

        @Test
        void samplesWhenSizeLessThanStreamLength() {
            // Arrange
            final Stream<String> input = Stream.of("A", "B", "C");

            // Act
            final List<String> output = input.gather(Gatherers4j.sampleFixedSize(2)).toList();

            // Assert
            assertThat(output).hasSize(2);
        }

        @Test
        void uniformSelection() {
            // Arrange
            final List<Integer> input = List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
            final Random random = new Random(42);
            final long[] counts = new long[10];

            // Act
            for (int i = 0; i < 100_000; i++) {
                input.stream().gather(Gatherers4j.sampleFixedSize(4, random)).forEach(it -> counts[it]++);
            }

            // Assert
            assertThat(ChiSquareTest.withDefaults().test(counts).reject(ALPHA)).isFalse();
        }

        @Test
        void testKnownSample() {
            // Arrange
            final Stream<String> input = Stream.of("A", "B", "C", "D", "E", "F", "G");

            // Act
            final String output = input
                    .gather(Gatherers4j.sampleFixedSize(3, new Random(42)))
                    .collect(Collectors.joining());

            // Assert
            assertThat(output).isEqualTo("CDF");
        }

        @SuppressWarnings("DataFlowIssue")
        @Test
        void withNullRandomGenerator() {
            assertThatIllegalArgumentException().isThrownBy(() -> Gatherers4j.sampleFixedSize(5, null));
        }
    }

    @Nested
    class PercentageBernoulli {

        @Test
        void inclusionProbability() {
            // Arrange
            final List<Integer> input = List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
            final int runs = 10_000;
            final Random random = new Random(42);
            final double samplePercentage = 0.4;
            final int[] counts = new int[input.size()];
            final BinomialTest binomialTest = BinomialTest.withDefaults().with(AlternativeHypothesis.TWO_SIDED);

            // Act
            for (int i = 0; i < runs; i++) {
                input.stream().gather(Gatherers4j.samplePercentage(samplePercentage, random)).forEach(it -> counts[it]++);
            }

            // Assert
            for (int i = 0; i < counts.length; i++) {
                assertThat(binomialTest.test(runs, counts[i], samplePercentage).reject(ALPHA / counts.length))
                        .as("element %d selected %d times", i, counts[i])
                        .isFalse();
            }
        }

        @Test
        void testKnownSample() {
            // Arrange
            final Stream<String> input = Stream.of("A", "B", "C", "D", "E", "F", "G");

            // Act
            final String output = input
                    .gather(Gatherers4j.samplePercentage(0.5, new Random(42)))
                    .collect(Collectors.joining());

            // Assert
            assertThat(output).isEqualTo("CDG");
        }


        @ParameterizedTest(name = "samplePercentage of {0}")
        @ValueSource(doubles = {0.0, 1.01, -0.1, Double.NaN})
        void percentageMustBeInRange(final double percentage) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> Gatherers4j.samplePercentage(percentage)
            );
        }

        @SuppressWarnings("DataFlowIssue")
        @Test
        void withNullRandomGenerator() {
            assertThatIllegalArgumentException().isThrownBy(() -> Gatherers4j.samplePercentage(0.5, null));
        }
    }

}