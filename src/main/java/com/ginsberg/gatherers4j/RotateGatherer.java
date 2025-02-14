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

package com.ginsberg.gatherers4j;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Gatherer;

public class RotateGatherer<INPUT extends @Nullable Object>
        implements Gatherer<INPUT, RotateGatherer.State<INPUT>, INPUT> {

    public enum Direction {
        Left {
            Direction flip() {
                return Right;
            }
        }, Right {
            Direction flip() {
                return Left;
            }
        };
        abstract Direction flip();
    }

    private final Direction direction;
    private final int distance;

    public RotateGatherer(final Direction direction, final int distance) {
        if (distance < 0) {
            this.distance = -distance;
            this.direction = direction.flip();
        } else {
            this.direction = direction;
            this.distance = distance;
        }
    }

    @Override
    public Supplier<State<INPUT>> initializer() {
        return State::new;
    }

    @Override
    public Integrator<State<INPUT>, INPUT, INPUT> integrator() {
        return Integrator.ofGreedy((state, element, downstream) -> {
            if (distance == 0) {
                downstream.push(element);
            } else {
                state.fullStream.add(element);
            }
            return !downstream.isRejecting();
        });
    }

    @Override
    public BiConsumer<State<INPUT>, Downstream<? super INPUT>> finisher() {
        return (inputState, downstream) -> {
            final int size = inputState.fullStream.size();
            if (size == 0) {
                return;
            }
            final int rotateDistance = distance % size;
            for (int i = 0; i < size; i++) {
                if (direction == Direction.Left) {
                    downstream.push(inputState.fullStream.get((i + rotateDistance) % size));
                } else {
                    downstream.push(inputState.fullStream.get((i - rotateDistance + size) % size));
                }
            }
        };
    }

    public static class State<INPUT> {
        final List<INPUT> fullStream = new ArrayList<>();
    }
}
