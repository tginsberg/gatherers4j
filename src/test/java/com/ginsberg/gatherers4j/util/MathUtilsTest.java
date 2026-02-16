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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.math.MathContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MathUtilsTest {

    @Nested
    class NthRoot {
        @Test
        void firstRootReturnsSelf() {
            assertThat(MathUtils.nthRoot(new BigDecimal("16"), 1))
                    .isEqualByComparingTo("16");
        }

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

        @ParameterizedTest(name = "root is {0}")
        @ValueSource(ints = {-1, 0 } )
        void invalidRoots(int root) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> MathUtils.nthRoot(BigDecimal.ONE, root));
        }

        @SuppressWarnings("DataFlowIssue")
        @Test
        void missingValue() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> MathUtils.nthRoot(null, 2));
        }

        @SuppressWarnings("DataFlowIssue")
        @Test
        void missingMathContext() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> MathUtils.nthRoot(BigDecimal.ONE, 2, null));
        }

        @Test
        void verySmallPositiveValue() {
            assertThat(MathUtils.nthRoot(new BigDecimal("0.00000001"), 2))
                    .isEqualByComparingTo("0.0001");
        }

        @ParameterizedTest(name = "root {0} of ONE returns ONE")
        @ValueSource(longs = {1, 2, 3, 5, 10, 100, 1000})
        void rootOfOneReturnsOne(long root) {
            assertThat(MathUtils.nthRoot(BigDecimal.ONE, root))
                    .isEqualByComparingTo("1");
        }

        @Test
        void zeroPrecisionMathContext() {
            final MathContext mc = new MathContext(0);
            final BigDecimal result = MathUtils.nthRoot(new BigDecimal("100"), 2, mc);
            assertThat(result).isEqualByComparingTo("10");
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

        @Test
        void negativeExponent() {
            // Should throw or have defined behavior - let's see what happens
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> MathUtils.pow(new BigDecimal("2"), -5, MathContext.DECIMAL64));
        }

        @Test
        void exponentTwo() {
            assertThat(MathUtils.pow(new BigDecimal("7"), 2, MathContext.DECIMAL64))
                    .isEqualByComparingTo("49");
        }

        @Test
        void oddExponentFive() {
            assertThat(MathUtils.pow(new BigDecimal("3"), 5, MathContext.DECIMAL64))
                    .isEqualByComparingTo("243");
        }

        @Test
        void oddExponentSeven() {
            assertThat(MathUtils.pow(new BigDecimal("2"), 7, MathContext.DECIMAL64))
                    .isEqualByComparingTo("128");
        }

        @Test
        void largeEvenExponent() {
            assertThat(MathUtils.pow(new BigDecimal("2"), 16, MathContext.DECIMAL64))
                    .isEqualByComparingTo("65536");
        }

        @Test
        void largeEvenExponentThirtyTwo() {
            assertThat(MathUtils.pow(new BigDecimal("2"), 32, MathContext.DECIMAL64))
                    .isEqualByComparingTo("4294967296");
        }

        @Test
        void zeroBaseWithZeroExponent() {
            assertThat(MathUtils.pow(BigDecimal.ZERO, 0, MathContext.DECIMAL64))
                    .isEqualByComparingTo("1");
        }

        @Test
        void zeroBaseWithPositiveExponent() {
            assertThat(MathUtils.pow(BigDecimal.ZERO, 5, MathContext.DECIMAL64))
                    .isEqualByComparingTo("0");
        }

        @ParameterizedTest(name = "exponent {0} of ONE returns ONE")
        @ValueSource(ints = {0, 1, 100})
        void oneBaseWithVariousExponents(int exponent) {
            assertThat(MathUtils.pow(BigDecimal.ONE, exponent, MathContext.DECIMAL64))
                    .isEqualByComparingTo("1");
        }
        @Test
        void negativeBase() {
            assertThat(MathUtils.pow(new BigDecimal("-2"), 3, MathContext.DECIMAL64))
                    .isEqualByComparingTo("-8");
        }

        @Test
        void negativeBaseEvenExponent() {
            assertThat(MathUtils.pow(new BigDecimal("-3"), 4, MathContext.DECIMAL64))
                    .isEqualByComparingTo("81");
        }

        @SuppressWarnings("DataFlowIssue")
        @Test
        void nullBase() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> MathUtils.pow(null, 2, MathContext.DECIMAL64));
        }

        @SuppressWarnings("DataFlowIssue")
        @Test
        void nullMathContext() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> MathUtils.pow(new BigDecimal("2"), 2, null));
        }
    }
}
