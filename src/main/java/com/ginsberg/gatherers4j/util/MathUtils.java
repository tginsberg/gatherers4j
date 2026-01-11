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

import java.math.BigDecimal;
import java.math.MathContext;

import static com.ginsberg.gatherers4j.util.GathererUtils.mustNotBeNull;

/**
 * Mathematical utility functions.
 */
public abstract class MathUtils {

    /**
     * Compute the Nth root of a BigDecimal using Newton-Raphson iteration.
     * Supports negative values only when n is odd. Uses the provided MathContext for precision.
     *
     * @param value the value to find the root of
     * @param n     the root to find (n must be &gt; 0)
     * @param mc    the MathContext to use for precision
     * @return the Nth root of the value
     */
    public static BigDecimal nthRoot(
            final BigDecimal value,
            final long n,
            final MathContext mc
    ) {
        mustNotBeNull(value, "Value must not be null");
        mustNotBeNull(mc, "MathContext must not be null");
        if (n <= 0) {
            throw new IllegalArgumentException("n must be > 0");
        }
        if (value.signum() == 0) {
            return BigDecimal.ZERO;
        }
        final boolean negative = value.signum() < 0;
        if (negative && (n % 2 == 0)) {
            throw new ArithmeticException("Even root of negative value is not real");
        }
        final BigDecimal abs = negative ? value.negate() : value;
        // Initial guess: scale based on double approximation
        BigDecimal x = new BigDecimal(Math.pow(abs.doubleValue(), 1.0 / n), mc);
        if (x.signum() == 0) {
            x = BigDecimal.ONE; // fallback
        }
        final BigDecimal nBD = new BigDecimal(n);
        final int precision = mc.getPrecision() > 0 ? mc.getPrecision() : Math.max(abs.precision(), 34);
        final MathContext work = new MathContext(precision + 5, mc.getRoundingMode());
        final BigDecimal eps = BigDecimal.ONE.movePointLeft(precision);

        // Newton iteration: x_{k+1} = ((n-1)*x_k + A / x_k^{n-1}) / n
        for (int iter = 0; iter < 1_000; iter++) {
            BigDecimal xPow = pow(x, n - 1, work);
            if (xPow.signum() == 0) {
                x = BigDecimal.ONE;
                xPow = BigDecimal.ONE;
            }
            final BigDecimal numerator = xPow.multiply(x, work)
                    .multiply(nBD.subtract(BigDecimal.ONE), work)
                    .add(abs, work);
            final BigDecimal xNext = numerator.divide(nBD.multiply(xPow, work), work);
            final BigDecimal delta = xNext.subtract(x, work).abs();
            x = xNext;
            if (delta.compareTo(eps) <= 0) {
                break;
            }
        }
        x = x.round(mc);
        return negative ? x.negate() : x;
    }

    /**
     * Compute the Nth root of a BigDecimal using Newton-Raphson iteration and DECIMAL64 MathContext.
     *
     * @param value the value to find the root of
     * @param n     the root to find (n must be &gt; 0)
     * @return the Nth root of the value
     */
    public static BigDecimal nthRoot(final BigDecimal value, final long n) {
        return nthRoot(value, n, MathContext.DECIMAL64);
    }

    /**
     * Compute the power of a BigDecimal.
     *
     * @param base     the base
     * @param exponent the exponent (must be non-negative)
     * @param mc       the MathContext to use
     * @return the result of base^exponent
     */
    public static BigDecimal pow(final BigDecimal base, final long exponent, final MathContext mc) {
        if (exponent == 0) {
            return BigDecimal.ONE;
        } else if (exponent == 1) {
            return base;
        } else {
            BigDecimal result = BigDecimal.ONE;
            BigDecimal currentBase = base;
            long remainingExponent = exponent;

            while (remainingExponent > 0) {
                if ((remainingExponent & 1) == 1) {
                    result = result.multiply(currentBase, mc);
                }
                remainingExponent >>= 1;
                if (remainingExponent > 0) {
                    currentBase = currentBase.multiply(currentBase, mc);
                }
            }

            return result;
        }
    }
}
