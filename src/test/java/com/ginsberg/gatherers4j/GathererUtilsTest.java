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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class GathererUtilsTest {

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