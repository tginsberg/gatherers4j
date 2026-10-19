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

package com.ginsberg.gatherers4j;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Gatherer;
import java.util.stream.Stream;

import static com.ginsberg.gatherers4j.util.GathererUtils.mustNotBeNull;
import static com.ginsberg.gatherers4j.util.GathererUtils.pushAll;

public final class EnsureMatchGatherer<INPUT extends @Nullable Object>
        implements Gatherer<INPUT, EnsureMatchGatherer.State<INPUT>, INPUT> {

    private final Mode mode;
    private final Predicate<? super INPUT> predicate;
    private Supplier<? extends Stream<? extends INPUT>> orElse;

    EnsureMatchGatherer(final Mode mode, final Predicate<? super INPUT> predicate) {
        this.mode = mode;
        this.predicate = mustNotBeNull(predicate, "Predicate must not be null");
        this.orElse = () -> {
            throw new IllegalStateException(mode.failureMessage);
        };
    }

    /// When the stream fails the check, call the given `Supplier` to produce an output instead of throwing an
    /// exception (the default behavior).
    ///
    /// Note: You will need a type witness when using this:
    ///
    /// `source.gather(Gatherers4j.<String>ensureAllMatch(s -> s.startsWith("A")).orElse(() -> Stream.of("A", "B")))`
    ///
    /// @param orElse - A non-null `Supplier`, the results of which will be used instead of the input stream.
    /// @return This `EnsureMatchGatherer`
    public EnsureMatchGatherer<INPUT> orElse(final Supplier<? extends Stream<? extends INPUT>> orElse) {
        this.orElse = mustNotBeNull(orElse, "The orElse function must not be null");
        return this;
    }

    /// When the stream fails the check, produce an empty stream instead of throwing an exception (the default
    /// behavior).
    ///
    /// Note: You will need a type witness when using this:
    ///
    /// `source.gather(Gatherers4j.<String>ensureAllMatch(s -> s.startsWith("A")).orElseEmpty())`
    ///
    /// @return This `EnsureMatchGatherer`
    public EnsureMatchGatherer<INPUT> orElseEmpty() {
        this.orElse = Stream::empty;
        return this;
    }

    @Override
    public BinaryOperator<State<INPUT>> combiner() {
        if (mode == Mode.Any) {
            return Gatherer.defaultCombiner();
        }
        return (left, right) -> {
            left.failed |= right.failed;
            if (!left.failed) {
                left.elements.addAll(right.elements);
            }
            return left;
        };
    }

    @Override
    public BiConsumer<State<INPUT>, Downstream<? super INPUT>> finisher() {
        return (state, downstream) -> {
            final boolean passed = mode == Mode.Any ? state.matched : !state.failed;
            if (passed) {
                pushAll(state.elements, downstream);
            } else {
                pushAll(orElse.get(), downstream);
            }
        };
    }

    @Override
    public Supplier<State<INPUT>> initializer() {
        return State::new;
    }

    @Override
    public Integrator<State<INPUT>, INPUT, INPUT> integrator() {
        return switch (mode) {
            case All, None -> Integrator.of((state, element, downstream) -> {
                if (predicate.test(element) == (mode == Mode.All)) {
                    state.elements.add(element);
                } else {
                    state.failed = true;
                }
                return !state.failed && !downstream.isRejecting();
            });
            case Any -> Integrator.ofGreedy((state, element, downstream) -> {
                if (state.matched) {
                    return downstream.push(element);
                }
                state.elements.add(element);
                if (predicate.test(element)) {
                    state.matched = true;
                    pushAll(state.elements, downstream);
                    state.elements.clear();
                }
                return !downstream.isRejecting();
            });
        };
    }

    enum Mode {
        All("Not all elements match the predicate"),
        Any("No element matches the predicate"),
        None("An element matches the predicate");

        final String failureMessage;

        Mode(final String failureMessage) {
            this.failureMessage = failureMessage;
        }
    }

    public static class State<INPUT extends @Nullable Object> {
        final List<INPUT> elements = new ArrayList<>();
        boolean failed;
        boolean matched;
    }
}
