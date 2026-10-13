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

import com.ginsberg.gatherers4j.test.ParallelAndSequentialTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static com.ginsberg.gatherers4j.test.ParallelAndSequentialTest.NULL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class DistinctLatestGathererTest {

    @ParallelAndSequentialTest(values = {"a", "b", "c", "aa", "bb", "cc", "aaa"})
    void distinctLatestBy(final Stream<String> input) {
        // Act
        final List<String> output = input
                .gather(Gatherers4j.distinctLatestBy(String::length))
                .toList();

        // Assert
        assertThat(output).containsExactly("c", "cc", "aaa");
    }

    @ParallelAndSequentialTest(values = {})
    void distinctLatestByEmptyStream(final Stream<String> input) {
        // Act
        final List<String> output = input
                .gather(Gatherers4j.distinctLatestBy(String::length))
                .toList();

        // Assert
        assertThat(output).isEmpty();
    }

    @ParallelAndSequentialTest(values = {"a", "bb", "c"})
    void distinctLatestByEmitsInPositionOfFirstOccurrence(final Stream<String> input) {
        // Act
        final List<String> output = input
                .gather(Gatherers4j.distinctLatestBy(String::length))
                .toList();

        // Assert
        assertThat(output).containsExactly("c", "bb");
    }

    @Test
    void distinctLatestByStopsWhenDownstreamRejects() {
        // Arrange
        final Stream<String> input = Stream.of("a", "bb", "c", "ddd");

        // Act
        final List<String> output = input
                .gather(Gatherers4j.distinctLatestBy(String::length))
                .limit(2)
                .toList();

        // Assert
        assertThat(output).containsExactly("c", "bb");
    }

    @ParallelAndSequentialTest(values = {"a"})
    void distinctLatestBySingleElement(final Stream<String> input) {
        // Act
        final List<String> output = input
                .gather(Gatherers4j.distinctLatestBy(String::length))
                .toList();

        // Assert
        assertThat(output).containsExactly("a");
    }

    @ParallelAndSequentialTest(values = {NULL, "a", NULL, "b"})
    void distinctLatestByWithNull(final Stream<String> input) {
        // Act
        final List<String> output = input
                .gather(Gatherers4j.distinctLatestBy(it -> it == null ? null : it.length()))
                .toList();

        // Assert
        assertThat(output).containsExactly(null, "b");
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    void distinctLatestByWithNullMappingFunction() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> Gatherers4j.distinctLatestBy(null))
                .withMessage("Mapping function must not be null");
    }
}
