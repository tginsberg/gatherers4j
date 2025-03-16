/*
 * Copyright 2025 Todd Ginsberg
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
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SampleGathererTest {

    @Nested
    class FixedSizeReservoirSampling {
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
            final int samples = 100_000;
            final int sampleSize = 4;
            final int[] counts = new int[10];
            final double expectedProbability = (double) sampleSize / samples;
            final BinomialTest binomialTest = BinomialTest.withDefaults().with(AlternativeHypothesis.TWO_SIDED);

            // Act
            for (int i = 0; i < samples; i++) {
                input.stream().gather(Gatherers4j.sampleFixedSize(sampleSize)).forEach(it -> counts[it]++);
            }

            // Assert
            for (int count : counts) {
                assertThat(binomialTest.test(samples, count, expectedProbability).getPValue()).isLessThan(0.05);
            }
        }

        @ParameterizedTest(name = "sampleSize of {0}")
        @ValueSource(ints = {-1, 0})
        void sampleSizeMustBeAtLeast1(int size) {
            assertThatThrownBy(() ->
                    Gatherers4j.sampleFixedSize(size)
            ).isExactlyInstanceOf(IllegalArgumentException.class);
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
            final long[] counts = new long[10];

            // Act
            for (int i = 0; i < 100_000; i++) {
                input.stream().gather(Gatherers4j.sampleFixedSize(4)).forEach(it -> counts[it]++);
            }

            // Assert
            assertThat(ChiSquareTest.withDefaults().test(counts).getPValue()).isLessThan(0.05);
        }

    }

    @Nested
    class PercentagePoisson {

        @Test
        void inclusionProbability() {
            // Arrange
            final List<Integer> input = List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
            final int samples = 100_000;
            final double samplePercentage = 0.4;
            final int[] counts = new int[10];
            final double expectedProbability = samplePercentage / samples;
            final BinomialTest binomialTest = BinomialTest.withDefaults().with(AlternativeHypothesis.TWO_SIDED);

            // Act
            for (int i = 0; i < samples; i++) {
                input.stream().gather(Gatherers4j.samplePercentage(samplePercentage)).forEach(it -> counts[it]++);
            }

            // Assert
            for (int count : counts) {
                assertThat(binomialTest.test(samples, count, expectedProbability).getPValue()).isLessThan(0.05);
            }
        }

        @ParameterizedTest(name = "samplePercentage of {0}")
        @ValueSource(doubles = {0.0, 1.01, -0.1})
        void sampleSizeMustBeAtLeast1(final double percentage) {
            assertThatThrownBy(() ->
                    Gatherers4j.samplePercentage(percentage)
            ).isExactlyInstanceOf(IllegalArgumentException.class);
        }
    }

}