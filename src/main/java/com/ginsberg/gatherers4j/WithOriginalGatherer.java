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

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Supplier;
import java.util.stream.Gatherer;

public class WithOriginalGatherer<INPUT, STATE, OUTPUT>
        implements Gatherer<INPUT, STATE, WithOriginal<INPUT, OUTPUT>> {

    private final Gatherer<INPUT, STATE, OUTPUT> delegate;

    WithOriginalGatherer(final Gatherer<INPUT, STATE, OUTPUT> delegate) {
        this.delegate = delegate;
    }

    @Override
    public Supplier<STATE> initializer() {
        return delegate.initializer();
    }

    @Override
    public Integrator<STATE, INPUT, WithOriginal<INPUT, OUTPUT>> integrator() {
        final CapturingDownstream<OUTPUT> capturingDownstream = new CapturingDownstream<>();
        final Integrator<STATE, INPUT, OUTPUT> delegateIntegrator = delegate.integrator();

        return (state, element, downstream) -> {
            final boolean response = delegateIntegrator.integrate(state, element, capturingDownstream);
            while (!capturingDownstream.captured.isEmpty()) {
                downstream.push(new WithOriginal<>(element, capturingDownstream.captured.poll()));
            }
            return response;
        };
    }

    private static class CapturingDownstream<OUTPUT> implements Downstream<OUTPUT> {

        private final Deque<OUTPUT> captured = new ConcurrentLinkedDeque<>();

        @Override
        public boolean push(final OUTPUT capturedElement) {
            captured.push(capturedElement);
            return true; // Unused
        }
    }
}
