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

import com.ginsberg.gatherers4j.util.GathererUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.ginsberg.gatherers4j.util.GathererUtils.mustNotBeNull;
import static org.assertj.core.api.Assertions.*;


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
    }

    @SuppressWarnings("ConstantValue")
    @Nested
    class SafeEquals {

        @Test
        void withTwoNulls() {
            assertThat(GathererUtils.safeEquals(null, null)).isTrue();
        }

        @Test
        void withTwoNonNullsThatAreEqual() {
            assertThat(GathererUtils.safeEquals("A", "A")).isTrue();
        }

        @Test
        void withTwoNonNullsThatAreNotEqual() {
            assertThat(GathererUtils.safeEquals("A", "B")).isFalse();
        }

        @Test
        void withLeftNullRightNotNull() {
            assertThat(GathererUtils.safeEquals(null, "A")).isFalse();
        }

        @Test
        void withLeftNotNullRightNull() {
            assertThat(GathererUtils.safeEquals("A", null)).isFalse();
        }
    }

}