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

package com.ginsberg.gatherers4j.util;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Gatherer;
import java.util.stream.Stream;

import static com.ginsberg.gatherers4j.util.GathererUtils.mustNotBeNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class GathererUtilsTest {

    @Nested
    class ListOfNullables {
        @Test
        void containsNulls() {
            // Arrange
            final String left = null;
            final String right = null;

            // Act
            final List<String> output = Arrays.asList(left, right);

            // Assert
            assertThat(output).isNotNull().hasSize(2).containsExactly(left, right);
        }

        @Test
        void containsNonNulls() {
            // Arrange
            final String left = "A";
            final String right = "B";

            // Act
            final List<String> output = Arrays.asList(left, right);

            // Assert
            assertThat(output).isNotNull().hasSize(2).containsExactly(left, right);
        }
    }

    @SuppressWarnings("DataFlowIssue")
    @Nested
    class MustNotBeNull {
        @Test
        void whenNull() {
            assertThatThrownBy(() -> mustNotBeNull(null, "123"))
                    .isExactlyInstanceOf(IllegalArgumentException.class)
                    .hasMessage("123");
        }

        @Test
        void whenNotNull() {
            assertThatNoException().isThrownBy(() -> mustNotBeNull("NonNull", "123"));
        }


        @Test
        void returnsValueWhenNotNull() {
            // Arrange
            final String input = "A";

            // Act
            final String output = mustNotBeNull(input, "Error");

            // Assert
            assertThat(input).isEqualTo(output);
        }
    }

    @Nested
    class PushAllShortCircuiting {

        private static final class CountingDownstream<INPUT> implements Gatherer.Downstream<INPUT> {
            final int maxAccept;
            int pushes = 0;

            CountingDownstream(final int maxAccept) {
                this.maxAccept = maxAccept;
            }

            @Override
            public boolean push(final INPUT item) {
                pushes++;
                return !isRejecting();
            }

            @Override
            public boolean isRejecting() {
                return pushes >= maxAccept;
            }
        }

        @Test
        void pushAllCollectionStopsWhenDownstreamRejects() {
            // Arrange
            final List<String> elements = List.of("A", "B", "C", "D");
            final CountingDownstream<String> downstream = new CountingDownstream<>(2);

            // Act
            GathererUtils.pushAll(elements, downstream);

            // Assert
            assertThat(downstream.pushes).isEqualTo(2);
        }

        @Test
        void pushAllIteratorStopsWhenDownstreamRejects() {
            // Arrange
            final List<String> elements = List.of("A", "B", "C", "D");
            final CountingDownstream<String> downstream = new CountingDownstream<>(2);

            // Act
            GathererUtils.pushAll(elements.iterator(), downstream);

            // Assert
            assertThat(downstream.pushes).isEqualTo(2);
        }

        @Test
        void pushAllStreamStopsWhenDownstreamRejects() {
            // Arrange
            final Stream<String> elements = Stream.of("A", "B", "C", "D");
            final CountingDownstream<String> downstream = new CountingDownstream<>(2);

            // Act
            GathererUtils.pushAll(elements, downstream);

            // Assert
            assertThat(downstream.pushes).isEqualTo(2);
        }
    }

}