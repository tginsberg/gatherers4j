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

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.ginsberg.gatherers4j.util.GathererUtils.mustNotBeNull;
import static com.ginsberg.gatherers4j.util.GathererUtils.pushAll;

public final class EnsureDistinctGatherer<INPUT extends @Nullable Object> extends DistinctKeyGatherer<INPUT> {

    private Supplier<Stream<INPUT>> orElse = () -> {
        throw new IllegalStateException("Elements are not distinct");
    };

    EnsureDistinctGatherer(final Function<? super INPUT, ? extends @Nullable Object> mappingFunction) {
        super(mappingFunction, OnRepeat.Fail);
    }

    /// When an element repeats, call the given `Supplier<Stream<INPUT>>` to produce an output instead of throwing
    /// an exception (the default behavior).
    ///
    /// Note: You will need a type witness when using this:
    ///
    /// `source.gather(Gatherers4j.<String>ensureDistinct().orElse(() -> Stream.of("A", "B")))`
    ///
    /// @param orElse - A non-null `Supplier`, the results of which will be used instead of the input stream.
    /// @return This `EnsureDistinctGatherer`
    public EnsureDistinctGatherer<INPUT> orElse(final Supplier<Stream<INPUT>> orElse) {
        this.orElse = mustNotBeNull(orElse, "The orElse function must not be null");
        return this;
    }

    /// When an element repeats, produce an empty stream instead of throwing an exception (the default behavior).
    ///
    /// Note: You will need a type witness when using this:
    ///
    /// `source.gather(Gatherers4j.<String>ensureDistinct().orElseEmpty())`
    ///
    /// @return This `EnsureDistinctGatherer`
    public EnsureDistinctGatherer<INPUT> orElseEmpty() {
        this.orElse = Stream::empty;
        return this;
    }

    @Override
    void onFailure(final Downstream<? super INPUT> downstream) {
        pushAll(orElse.get(), downstream);
    }
}
