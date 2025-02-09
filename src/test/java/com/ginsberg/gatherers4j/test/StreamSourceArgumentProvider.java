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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.stream.Stream;

public class StreamSourceArgumentProvider
        implements ArgumentsProvider, AnnotationConsumer<StreamSource> {

    private Object[] array;

    @Override
    public void accept(final StreamSource streamSource) {
        if (streamSource.strings().length > 0) {
            this.array = streamSource.strings();
        } else if (streamSource.stringBigDecimals().length > 0) {
            this.array = Arrays
                    .stream(streamSource.stringBigDecimals())
                    .map(it -> it.isEmpty() ? null : new BigDecimal(it))
                    .toArray();
        }
    }

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
        return Stream.of(
                Arguments.arguments("sequential", Arrays.stream(array)),
                Arguments.arguments("parallel", Arrays.stream(array).parallel())
        );
    }
}
