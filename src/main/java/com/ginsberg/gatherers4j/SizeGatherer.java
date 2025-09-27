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

import com.ginsberg.gatherers4j.enums.Size;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Gatherer;
import java.util.stream.Stream;

import static com.ginsberg.gatherers4j.util.GathererUtils.mustNotBeNull;
import static com.ginsberg.gatherers4j.util.GathererUtils.pushAll;

public class SizeGatherer<INPUT extends @Nullable Object>
        implements Gatherer<INPUT, SizeGatherer.State<INPUT>, INPUT> {

    private final long targetSize;
    private final Size operation;
    private Supplier<Stream<INPUT>> orElse;

    SizeGatherer(final Size operation, final long targetSize) {
        if (targetSize < 0) {
            throw new IllegalArgumentException("Target size cannot be negative");
        }
        this.operation = operation;
        this.targetSize = targetSize;
        this.orElse = () -> {
            throw new IllegalStateException("Invalid stream size: wanted " + operation.name() + " " + targetSize);
        };
    }

    /// When the current stream does not have the correct length, call the given
    /// `Supplier<Stream<INPUT>>` to produce an output instead of throwing an exception (the default behavior).
    ///
    /// Note: You will need a type witness when using this:
    ///
    /// `source.gather(Gatherers4j.<String>ensureSizeExactly(2).orElse(() -> Stream.of("A", "B")))`
    ///
    /// @param orElse - A non-null `Supplier`, the results of which will be used instead of the input stream.
    public SizeGatherer<INPUT> orElse(final Supplier<Stream<INPUT>> orElse) {
        this.orElse = mustNotBeNull(orElse, "The orElse function must not be null");
        return this;
    }

    /// When the current stream does not have the correct length, produce an empty stream instead of throwing
    /// an exception (the default behavior).
    ///
    /// Note: You will need a type witness when using this:
    ///
    /// `source.gather(Gatherers4j.<String>ensureSizeExactly(2).orElseEmpty())`
    ///
    public SizeGatherer<INPUT> orElseEmpty() {
        this.orElse = Stream::empty;
        return this;
    }

    @Override
    public BiConsumer<State<INPUT>, Downstream<? super INPUT>> finisher() {
        return (state, downstream) -> {
            if (!state.failed && operation.accept(state.elements.size(), targetSize)) {
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
        return (state, element, downstream) -> {
            if (operation.tryAccept(state.elements.size() + 1, targetSize)) {
                state.elements.add(element);
            } else {
                state.failed = true;
            }
            return !state.failed || !downstream.isRejecting();
        };
    }

    public static class State<INPUT> {
        boolean failed = false;
        final List<INPUT> elements = new ArrayList<>();
    }
}
