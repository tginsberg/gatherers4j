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

class TakeUntilGathererTest {

    @Test
    @SuppressWarnings("DataFlowIssue")
    void predicateMustNotBeNull() {
        assertThatThrownBy(() -> Gatherers4j.takeUntil(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void takeUntilIncludesTriggeringElement() {
        // Arrange
        final Stream<String> input = Stream.of("A", "BB", "CCC", "DDDD", "EEEEE");

        // Act
        final List<String> output = input.gather(Gatherers4j.takeUntil(it -> it.equals("CCC"))).toList();

        // Assert
        assertThat(output).containsExactly("A", "BB", "CCC");
    }
}