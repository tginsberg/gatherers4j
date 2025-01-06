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

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Gatherer;

public class SizeGatherer<INPUT extends @Nullable Object>
        implements Gatherer<INPUT, SizeGatherer.State<INPUT>, INPUT> {

    private final long targetSize;
    private final Operation operation;

    SizeGatherer(final Operation operation, final long targetSize) {
        if (targetSize < 0) {
            throw new IllegalArgumentException("Target size cannot be negative");
        }
        this.operation = operation;
        this.targetSize = targetSize;
    }

    @Override
    public BiConsumer<State<INPUT>, Downstream<? super INPUT>> finisher() {
        return (state, downstream) -> {
            operation.checkFinalLength(state.elements.size(), targetSize);
            state.elements.forEach(downstream::push);
        };
    }

    @Override
    public Supplier<State<INPUT>> initializer() {
        return State::new;
    }

    @Override
    public Integrator<State<INPUT>, INPUT, INPUT> integrator() {
        return (state, element, downstream) -> {
            operation.tryAccept(state.elements.size() + 1, targetSize);
            state.elements.add(element);
            return !downstream.isRejecting();
        };
    }

    enum Operation {
        Equal {
            @Override
            void tryAccept(long length, long target) {
                if(length > target) {
                    fail(target);
                }
            }

            @Override
            void checkFinalLength(long length, long target) {
                if (length != target) {
                    fail(target);
                }
            }

            void fail(long target) {
                throw new IllegalStateException("Stream length must be equal to " + target);
            }
        },
        GreaterThan {
            @Override
            void checkFinalLength(long length, long target) {
                if (length <= target) {
                    fail(target);
                }
            }

            void fail(long target) {
                throw new IllegalStateException("Stream length must be greater than " + target);
            }

        },
        GreaterThanOrEqualTo {
            @Override
            void checkFinalLength(long length, long target) {
                if (length < target) {
                    fail(target);
                }
            }

            void fail(long target) {
                throw new IllegalStateException("Stream length must be greater than or equal to " + target);
            }
        },
        LessThan {
            @Override
            void tryAccept(long length, long target) {
                if(length >= target) {
                    fail(target);
                }
            }

            @Override
            void checkFinalLength(long length, long target) {
                if (length >= target) {
                    fail(target);
                }
            }

            void fail(long target) {
                throw new IllegalStateException("Stream length must be less than " + target);
            }
        },
        LessThanOrEqualTo {
            @Override
            void tryAccept(long length, long target) {
                if(length > target) {
                    fail(target);
                }
            }

            @Override
            void checkFinalLength(long length, long target) {
                if (length > target) {
                    fail(target);
                }
            }

            void fail(long target) {
                throw new IllegalStateException("Stream length must be less than or equal to " + target);
            }
        };

        abstract void checkFinalLength(long length, long target);
        void tryAccept(long length, long target){
            // Empty implementation
        }
    }

    public static class State<INPUT> {
        final List<INPUT> elements = new ArrayList<>();
    }
}
