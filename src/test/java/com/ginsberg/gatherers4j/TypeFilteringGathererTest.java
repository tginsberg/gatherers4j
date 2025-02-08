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

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class TypeFilteringGathererTest {

    @Test
    void multipleTypes() {
        // Arrange
        final Stream<Number> input = Stream.of(1, 2.0, 3L, (short) 4, (byte) 5);

        // Act
        final var output = input.gather(
                Gatherers4j.filterInstanceOf(Integer.class, Short.class)
        ).toList();

        // Assert
        assertThat(output).satisfiesExactly(
                item ->assertThat(item)
                        .isEqualTo(1)
                        .isInstanceOf(Integer.class),
                item -> assertThat(item)
                        .isEqualTo((short)4)
                        .isInstanceOf(Short.class)
        );
    }

    @Test
    void mustHaveAtLeastOneValidType() {
        assertThatThrownBy(TypeFilteringGatherer::of)
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void singleType() {
        // Arrange
        final Stream<Number> input = Stream.of(1, 2.0, 3L, (short) 4, (byte) 5);

        // Act
        final List<Integer> output = input.gather(Gatherers4j.filterInstanceOf(Integer.class)).toList();

        // Assert
        assertThat(output).satisfiesExactly(item ->
                assertThat(item)
                        .isEqualTo(1)
                        .isInstanceOf(Integer.class)
        );
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    void validTypesMustNotBeNull() {
        assertThatThrownBy(() -> {
            TypeFilteringGatherer.of((Class<Object>[])null);
        }).isInstanceOf(IllegalArgumentException.class);
    }

}