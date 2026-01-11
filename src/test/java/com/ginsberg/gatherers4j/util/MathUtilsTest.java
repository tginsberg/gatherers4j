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

import java.math.BigDecimal;
import java.math.MathContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MathUtilsTest {

    @Nested
    class NthRoot {
        @Test
        void squareRoot() {
            assertThat(MathUtils.nthRoot(new BigDecimal("16"), 2))
                    .isEqualByComparingTo("4");
        }

        @Test
        void cubeRoot() {
            assertThat(MathUtils.nthRoot(new BigDecimal("27"), 3))
                    .isEqualByComparingTo("3");
        }

        @Test
        void fourthRoot() {
            assertThat(MathUtils.nthRoot(new BigDecimal("625"), 4))
                    .isEqualByComparingTo("5");
        }

        @Test
        void rootOfZero() {
            assertThat(MathUtils.nthRoot(BigDecimal.ZERO, 2))
                    .isEqualByComparingTo("0");
        }

        @Test
        void oddRootOfNegativeValue() {
            assertThat(MathUtils.nthRoot(new BigDecimal("-8"), 3))
                    .isEqualByComparingTo("-2");
        }

        @Test
        void evenRootOfNegativeValueThrows() {
            assertThatThrownBy(() -> MathUtils.nthRoot(new BigDecimal("-16"), 2))
                    .isExactlyInstanceOf(ArithmeticException.class);
        }

        @Test
        void nthRootWithMathContext() {
            final MathContext mc = new MathContext(10);
            assertThat(MathUtils.nthRoot(new BigDecimal("2"), 2, mc))
                    .isEqualByComparingTo("1.414213562");
        }

        @Test
        void largeNthRoot() {
            final long n = (long) Integer.MAX_VALUE + 1;
            assertThat(MathUtils.nthRoot(BigDecimal.ONE, n))
                    .isEqualByComparingTo("1");
        }
    }

    @Nested
    class Pow {
        @Test
        void zeroExponent() {
            assertThat(MathUtils.pow(new BigDecimal("10"), 0, MathContext.DECIMAL64))
                    .isEqualByComparingTo("1");
        }

        @Test
        void oneExponent() {
            assertThat(MathUtils.pow(new BigDecimal("10"), 1, MathContext.DECIMAL64))
                    .isEqualByComparingTo("10");
        }

        @Test
        void positiveExponent() {
            assertThat(MathUtils.pow(new BigDecimal("2"), 10, MathContext.DECIMAL64))
                    .isEqualByComparingTo("1024");
        }
    }
}
