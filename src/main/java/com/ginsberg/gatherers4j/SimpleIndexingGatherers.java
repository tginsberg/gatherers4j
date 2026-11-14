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

import com.ginsberg.gatherers4j.dto.WithIndex;
import org.jspecify.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.stream.Gatherer;
import java.util.stream.Stream;

import static com.ginsberg.gatherers4j.util.GathererUtils.mustNotBeNull;
import static com.ginsberg.gatherers4j.util.GathererUtils.pushAll;

public class SimpleIndexingGatherers {

    static <INPUT extends @Nullable Object> Gatherer<INPUT, ?, INPUT> dropWhileIndexed(
            final BiPredicate<? super Long, ? super INPUT> predicate
    ) {
        mustNotBeNull(predicate, "Predicate must not be null");
        return Gatherer.ofSequential(
                DropWhileState::new,
                Gatherer.Integrator.ofGreedy((state, element, downstream) -> {
                    if (state.dropping && predicate.test(state.index++, element)) {
                        return !downstream.isRejecting();
                    }
                    state.dropping = false;
                    return downstream.push(element);
                })
        );
    }

    static <INPUT extends @Nullable Object> Gatherer<INPUT, ?, INPUT> filterIndexed(
            final BiPredicate<? super Long, ? super INPUT> predicate
    ) {
        mustNotBeNull(predicate, "Predicate must not be null");
        return Gatherer.ofSequential(
                State::new,
                Gatherer.Integrator.ofGreedy((state, element, downstream) -> {
                    if (predicate.test(state.index++, element)) {
                        downstream.push(element);
                    }
                    return !downstream.isRejecting();
                })
        );
    }

    static <INPUT extends @Nullable Object, OUTPUT extends @Nullable Object> Gatherer<INPUT, ?, OUTPUT> flatMapIndexed(
            final BiFunction<? super Long, ? super INPUT, ? extends @Nullable Stream<? extends OUTPUT>> mappingFunction
    ) {
        mustNotBeNull(mappingFunction, "mappingFunction must not be null");
        return Gatherer.ofSequential(
                State::new,
                Gatherer.Integrator.ofGreedy((state, element, downstream) -> {
                    try (final Stream<? extends OUTPUT> mapped = mappingFunction.apply(state.index++, element)) {
                        if (mapped != null) {
                            pushAll(mapped, downstream);
                        }
                    }
                    return !downstream.isRejecting();
                })
        );
    }

    static <INPUT extends @Nullable Object, OUTPUT extends @Nullable Object> Gatherer<INPUT, ?, OUTPUT> mapIndexed(
            final BiFunction<? super Long, ? super INPUT, ? extends @Nullable OUTPUT> mappingFunction
    ) {
        mustNotBeNull(mappingFunction, "mappingFunction must not be null");
        return Gatherer.ofSequential(
                State::new,
                Gatherer.Integrator.ofGreedy((state, element, downstream) ->
                        downstream.push(mappingFunction.apply(state.index++, element))
                )
        );
    }

    static <INPUT extends @Nullable Object> Gatherer<INPUT, ?, INPUT> peekIndexed(
            final BiConsumer<? super Long, ? super INPUT> peekingConsumer
    ) {
        mustNotBeNull(peekingConsumer, "peekingConsumer must not be null");
        return Gatherer.ofSequential(
                State::new,
                Gatherer.Integrator.ofGreedy((state, element, downstream) -> {
                    peekingConsumer.accept(state.index++, element);
                            return downstream.push(element);
                        }
                )
        );
    }

    static <INPUT extends @Nullable Object> Gatherer<INPUT, ?, INPUT> takeWhileIndexed(
            final BiPredicate<? super Long, ? super INPUT> predicate
    ) {
        mustNotBeNull(predicate, "Predicate must not be null");
        return Gatherer.ofSequential(
                State::new,
                (state, element, downstream) ->
                        predicate.test(state.index++, element) && downstream.push(element)
        );
    }

    static <INPUT extends @Nullable Object> Gatherer<INPUT, ?, WithIndex<INPUT>> withIndex() {
        return mapIndexed(WithIndex::new);
    }

    private static class DropWhileState extends State {
        boolean dropping = true;
    }

    private static class State {
        long index;
    }
}
