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

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.locks.LockSupport;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ThrottlingGathererTest {

    @Test
    void amountIsNegative() {
        assertThatThrownBy(() ->
                Stream.of("A").gather(Gatherers4j.throttle(-1, Duration.ofSeconds(1)))
        ).isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void amountIsZero() {
        assertThatThrownBy(() ->
                Stream.of("A").gather(Gatherers4j.throttle(-1, Duration.ofSeconds(1)))
        ).isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void clockMustNotBeNull() {
        assertThatThrownBy(() ->
                Stream.of("A").gather(Gatherers4j.throttle(1, Duration.ofSeconds(1)).withClock(null))
        ).isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void defaultsRuleToPause() {
        // Arrange/Act
        final ThrottlingGatherer<String> gatherer = new ThrottlingGatherer<>(null, 1, Duration.ofSeconds(1));

        // Assert
        assertThat(gatherer).hasFieldOrPropertyWithValue("limitRule", ThrottlingGatherer.LimitRule.Pause);
    }

    @Test
    void durationIsNegative() {
        assertThatThrownBy(() ->
                Stream.of("A").gather(Gatherers4j.throttle(1, Duration.ofSeconds(-1)))
        ).isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void durationIsNull() {
        assertThatThrownBy(() ->
                Stream.of("A").gather(Gatherers4j.throttle(1, null))
        ).isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void durationIsZero() {
        assertThatThrownBy(() ->
                Stream.of("A").gather(Gatherers4j.throttle(1, Duration.ofSeconds(0)))
        ).isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testThrottlingCrossesPeriod() {
        // Arrange
        final Stream<String> input = Stream.of("A", "B", "C");
        final Duration duration = Duration.ofMillis(100);
        final Clock clock = new PredictableClock(0, 0, 0, 101, 0, 0);

        // Act
        final List<Long> output = input
                .gather(Gatherers4j.throttle(2, duration).withClock(clock))
                .map(_ -> System.currentTimeMillis())
                .toList();

        // Assert
        assertThat(output.get(1) - output.get(0)).isLessThan(duration.toMillis());
        assertThat(output.get(2) - output.get(0)).isGreaterThanOrEqualTo(duration.toMillis());
    }

    @Test
    void testThrottlingWithDrop() {
        // Arrange
        final Stream<String> input = Stream.of("A", "B", "C");
        final Duration duration = Duration.ofMillis(100);

        // Act
        final List<String> output = input
                .gather(Gatherers4j.debounce(2, duration))
                .toList();

        // Assert
        assertThat(output).containsExactly("A", "B");
    }

    @Test
    void testThrottlingWithPause() {
        // Arrange
        final Stream<String> input = Stream.of("A", "B", "C");
        final Duration duration = Duration.ofMillis(100);

        // Act
        final List<Long> output = input
                .gather(Gatherers4j.throttle(2, duration))
                .map(_ -> System.currentTimeMillis())
                .toList();

        // Assert
        assertThat(output.get(1) - output.get(0)).isLessThan(duration.toMillis());
        assertThat(output.get(2) - output.get(0)).isGreaterThanOrEqualTo(duration.toMillis());
    }

    private static class PredictableClock extends Clock {

        private final int[] pauses;
        private int invocation;

        private PredictableClock(final int... pauses) {
            this.pauses = pauses;
        }

        @Override
        public ZoneId getZone() {
            return null;
        }

        @Override
        public Instant instant() {
            int when = pauses[invocation];
            if (when > 0) {
                LockSupport.parkNanos(when * GathererUtils.NANOS_PER_MILLISECOND);
            }
            invocation = (invocation + 1) % pauses.length;
            return Instant.now();
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return null;
        }
    }
}