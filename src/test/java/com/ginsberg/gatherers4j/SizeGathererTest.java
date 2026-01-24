/*
 * Copyright 2024 Todd Ginsberg
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

import com.ginsberg.gatherers4j.enums.Size;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SizeGathererTest {

    @Nested
    class Common {
        @Test
        void canReplaceStreamEmpty() {
            // Arrange
            final Stream<String> input = Stream.of("A");

            // Act
            final List<String> output = input
                    .gather(Gatherers4j.<String>ensureSize(Size.Equals, 2).orElseEmpty())
                    .toList();

            // Assert
            assertThat(output).isEmpty();
        }

        @Test
        void canReplaceStreamNonempty() {
            // Arrange
            final Stream<String> input = Stream.of("A");

            // Act
            final List<String> output = input
                    .gather(Gatherers4j.<String>ensureSize(Size.Equals, 2).orElse(() -> Stream.of("A", "B")))
                    .toList();

            // Assert
            assertThat(output).containsExactly("A", "B");
        }


        @Test
        void orElseMustNotBeNull() {
            //noinspection DataFlowIssue
            assertThatIllegalArgumentException()
                .isThrownBy(() -> Gatherers4j.ensureSize(Size.Equals, 2).orElse(null))
                .withMessage("The orElse function must not be null");
        }

        @Test
        void targetSizeMustNotBeNegative() {
            assertThatIllegalArgumentException()
                .isThrownBy(() -> Gatherers4j.ensureSize(Size.Equals, -1))
                .withMessage("Target size cannot be negative");
        }
    }

    @Nested
    class Equals {

        @Test
        void doesNotEmitOverTarget() {
            assertThatIllegalStateException()
                .isThrownBy(() ->
                    Stream.of("A", "B", "C").gather(Gatherers4j.ensureSize(Size.Equals, 2)).toList()
                ).withMessage("Invalid stream size: wanted Equals 2");
        }

        @Test
        void doesNotEmitUnderTarget() {
            assertThatIllegalStateException()
                .isThrownBy(() ->
                    Stream.of("A").gather(Gatherers4j.ensureSize(Size.Equals, 2)).toList()
                ).withMessage(  "Invalid stream size: wanted Equals 2");
        }

        @Test
        void emitsAtTarget() {
            // Arrange
            final Stream<String> input = Stream.of("A", "B", "C");

            // Act
            final List<String> output = input.gather(Gatherers4j.ensureSize(Size.Equals, 3)).toList();

            // Assert
            assertThat(output).containsExactly("A", "B", "C");
        }
    }

    @Nested
    class GreaterThan {

        @Test
        void doesNotEmitAtTarget() {
            assertThatIllegalStateException()
                .isThrownBy(() ->
                    Stream.of("A", "B").gather(Gatherers4j.ensureSize(Size.GreaterThan, 2)).toList()
                ).withMessage(  "Invalid stream size: wanted GreaterThan 2");
        }

        @Test
        void doesNotEmitUnderTarget() {
            assertThatIllegalStateException()
                .isThrownBy(() ->
                    Stream.of("A").gather(Gatherers4j.ensureSize(Size.GreaterThan, 2))
                        .toList()
                ).withMessage("Invalid stream size: wanted GreaterThan 2");
        }

        @Test
        void emitsOverTarget() {
            // Arrange
            final Stream<String> input = Stream.of("A", "B", "C");

            // Act
            final List<String> output = input.gather(Gatherers4j.ensureSize(Size.GreaterThan, 2)).toList();

            // Assert
            assertThat(output).containsExactly("A", "B", "C");
        }
    }

    @Nested
    class GreaterThanOrEqualTo {

        @Test
        void doesNotEmitUnderTarget() {
            assertThatIllegalStateException()
                .isThrownBy(() ->
                    Stream.of("A").gather(Gatherers4j.ensureSize(Size.GreaterThanOrEqualTo, 2))
                        .toList()
                ).withMessage("Invalid stream size: wanted GreaterThanOrEqualTo 2");
        }

        @Test
        void emitsAtTarget() {
            // Arrange
            final Stream<String> input = Stream.of("A", "B");

            // Act
            final List<String> output = input.gather(Gatherers4j.ensureSize(Size.GreaterThanOrEqualTo, 2)).toList();

            // Assert
            assertThat(output).containsExactly("A", "B");
        }

        @Test
        void emitsOverTarget() {
            // Arrange
            final Stream<String> input = Stream.of("A", "B", "C");

            // Act
            final List<String> output = input.gather(Gatherers4j.ensureSize(Size.GreaterThanOrEqualTo, 2)).toList();

            // Assert
            assertThat(output).containsExactly("A", "B", "C");
        }

    }

    @Nested
    class LessThan {

        @Test
        void doesNotEmitAtTarget() {
            assertThatIllegalStateException()
                .isThrownBy(() ->
                    Stream.of("A", "B").gather(Gatherers4j.ensureSize(Size.LessThan, 2)).toList()
                )
                .withMessage("Invalid stream size: wanted LessThan 2");
        }

        @Test
        void doesNotEmitOverTarget() {
            assertThatIllegalStateException()
                .isThrownBy(() ->
                    Stream.of("A", "B", "C").gather(Gatherers4j.ensureSize(Size.LessThan, 2))
                        .toList()
                )
                .withMessage("Invalid stream size: wanted LessThan 2");
        }

        @Test
        void emitsUnderTarget() {
            // Arrange
            final Stream<String> input = Stream.of("A");

            // Act
            final List<String> output = input.gather(Gatherers4j.ensureSize(Size.LessThan,  2)).toList();

            // Assert
            assertThat(output).containsExactly("A");
        }
    }


    @Nested
    class LessThanOrEqualTo {

        @Test
        void doesNotEmitOverTarget() {
            assertThatIllegalStateException()
                .isThrownBy(() ->
                    Stream.of("A", "B", "C")
                        .gather(Gatherers4j.ensureSize(Size.LessThanOrEqualTo, 2)).toList()
                ).withMessage("Invalid stream size: wanted LessThanOrEqualTo 2");
        }

        @Test
        void emitsAtTarget() {
            // Arrange
            final Stream<String> input = Stream.of("A", "B");

            // Act
            final List<String> output = input.gather(Gatherers4j.ensureSize(Size.LessThanOrEqualTo,  2)).toList();

            // Assert
            assertThat(output).containsExactly("A", "B");
        }

        @Test
        void emitsUnderTarget() {
            // Arrange
            final Stream<String> input = Stream.of("A");

            // Act
            final List<String> output = input.gather(Gatherers4j.ensureSize(Size.LessThanOrEqualTo,  2)).toList();

            // Assert
            assertThat(output).containsExactly("A");
        }
    }
}
