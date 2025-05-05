package com.ginsberg.gatherers4j;

import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.function.Function;
import java.util.function.Supplier;

public class BigDecimalExponentialMovingAverageGatherer<INPUT extends @Nullable Object>
        extends BigDecimalGatherer<INPUT> {

    private final double alpha;

    public static <INPUT extends @Nullable Object> BigDecimalExponentialMovingAverageGatherer<INPUT> withAlpha(
            final double alpha,
            final Function<INPUT, @Nullable BigDecimal> mappingFunction
    ) {
        return new BigDecimalExponentialMovingAverageGatherer<>(alpha, mappingFunction);
    }

    public static <INPUT extends @Nullable Object> BigDecimalExponentialMovingAverageGatherer<INPUT> withPeriod(
            final int periods,
            final Function<INPUT, @Nullable BigDecimal> mappingFunction
    ) {
        if (periods <= 1) {
            throw new IllegalArgumentException("periods must be greater than 1");
        }
        final double alpha = 2.0 / (((long) periods) + 1);
        return new BigDecimalExponentialMovingAverageGatherer<>(alpha, mappingFunction);
    }

    private BigDecimalExponentialMovingAverageGatherer(
            final double alpha,
            final Function<INPUT, @Nullable BigDecimal> mappingFunction
    ) {
        super(mappingFunction);
        if (alpha <= 0 || alpha >= 1.0) {
            throw new IllegalArgumentException("alpha must be between 0.0 and 1.0, exclusive, got " + alpha);
        }
        this.alpha = alpha;
    }

    @Override
    public Supplier<BigDecimalGatherer.State> initializer() {
        return () -> new BigDecimalExponentialMovingAverageGatherer.State(alpha);
    }

    static class State implements BigDecimalGatherer.State {
        final BigDecimal alpha;
        final BigDecimal oneMinusAlpha;
        boolean first = true;
        BigDecimal ema = BigDecimal.ZERO;

        State(final double alpha) {
            this.alpha = BigDecimal.valueOf(alpha);
            this.oneMinusAlpha = BigDecimal.ONE.subtract(this.alpha);
        }

        @Override
        public void add(final BigDecimal element, final MathContext mathContext) {
            if (first) {
                first = false;
                ema = element;
            } else {
                ema = element.multiply(alpha).add(ema.multiply(oneMinusAlpha));
            }
        }

        @Override
        public BigDecimal calculate() {
            return ema;
        }
    }
}
