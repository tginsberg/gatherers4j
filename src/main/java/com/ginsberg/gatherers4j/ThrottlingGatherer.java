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

import org.jspecify.annotations.Nullable;

import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Supplier;
import java.util.stream.Gatherer;

import static com.ginsberg.gatherers4j.util.GathererUtils.NANOS_PER_MILLISECOND;
import static com.ginsberg.gatherers4j.util.GathererUtils.mustNotBeNull;

public class ThrottlingGatherer<INPUT extends @Nullable Object>
        implements Gatherer<INPUT, ThrottlingGatherer.State, INPUT> {

    public enum LimitRule {
        Drop,
        Pause
    }

    private final LimitRule limitRule;
    private final Duration duration;
    private final int allowed;
    private Clock clock = Clock.systemUTC();

    ThrottlingGatherer(final LimitRule limitRule, final int allowed, final Duration duration) {
        mustNotBeNull(limitRule, "LimitRule must not be null");
        mustNotBeNull(duration, "Duration must not be null");
        if (duration.toMillis() < 1) {
            throw new IllegalArgumentException("Minimum duration is 1ms");
        }
        if (allowed <= 0) {
            throw new IllegalArgumentException("Allowed must be positive");
        }
        this.limitRule = limitRule;
        this.duration = duration;
        this.allowed = allowed;
    }

    public ThrottlingGatherer<INPUT> withClock(final Clock clock) {
        mustNotBeNull(clock, "Clock must not be null");
        this.clock = clock;
        return this;
    }

    @Override
    public Supplier<State> initializer() {
        return () -> new State(limitRule, duration, allowed, clock);
    }

    @Override
    public Integrator<State, INPUT, INPUT> integrator() {
        return Integrator.ofGreedy((state, element, downstream) -> {
            if (!downstream.isRejecting() && state.attempt()) {
                downstream.push(element);
            }
            return !downstream.isRejecting();
        });
    }

    public static class State {
        final int allowedPerPeriod;
        final long periodDurationMillis;
        final LimitRule limitRule;
        final Clock clock;
        long thisPeriodEnd;
        int remainingPermits;

        State(final LimitRule limitRule, final Duration duration, final int allowed, final Clock clock) {
            this.limitRule = limitRule;
            this.allowedPerPeriod = allowed;
            this.periodDurationMillis = duration.toMillis();
            this.clock = clock;
            resetPeriod();
        }

        private void resetPeriod() {
            thisPeriodEnd = clock.millis() + periodDurationMillis;
            remainingPermits = allowedPerPeriod;
        }

        // Assuming this is not run in parallel. Gate with a lock if that assumption fails/changes.
        boolean attempt() {
            final long now = clock.millis();
            if(now < thisPeriodEnd) {
                // The current period has not ended
                if(remainingPermits == 0) {
                    if(limitRule == LimitRule.Drop) {
                        return false;
                    }
                    // Wait until next period, reset counters, fall through to take permit.
                    LockSupport.parkNanos((thisPeriodEnd - now) * NANOS_PER_MILLISECOND);
                    resetPeriod();
                }
            } else {
                // We're in a new period, reset the counters
                // and fall through to take permit.
                resetPeriod();
            }
            remainingPermits--;
            return true;
        }
    }
}
