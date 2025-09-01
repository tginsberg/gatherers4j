/*
 * Copyright 2025 Todd Ginsberg
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

package com.ginsberg.gatherers4j.test;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.AnnotationConsumer;
import org.junit.jupiter.params.support.ParameterDeclarations;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

import static com.ginsberg.gatherers4j.test.ParallelAndSequentialTest.NULL;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class StreamSourceArgumentProvider
        implements ArgumentsProvider, AnnotationConsumer<ParallelAndSequentialTest> {

    private Object[] array;

    @Override
    public void accept(final ParallelAndSequentialTest streamSource) {
        this.array = Arrays
                .stream(streamSource.values())
                .map(it -> Objects.equals(it, NULL) ? null : streamSource.type().mapper.apply(it))
                .toArray();
    }

    @Override
    public Stream<? extends Arguments> provideArguments(
            final ParameterDeclarations parameterDeclarations,
            final ExtensionContext context
    ) {
        return Stream.of(
                arguments(named("Sequential", Arrays.stream(array))),
                arguments(named("Parallel", Arrays.stream(array).parallel()))
        );
    }
}
