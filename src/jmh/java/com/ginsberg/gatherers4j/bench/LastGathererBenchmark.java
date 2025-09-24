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

package com.ginsberg.gatherers4j.bench;

import com.ginsberg.gatherers4j.Gatherers4j;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.util.List;
import java.util.SplittableRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1)
public class LastGathererBenchmark {

    @State(Scope.Thread)
    public static class DataState {
        @Param({"1", "10", "1000"})
        public int lastCount;

        @Param({"1000", "100000", "1000000"})
        public int streamSize;

        int[] data = {};

        @Setup(Level.Trial)
        public void setUp() {
            // Fill with pseudo-random data to avoid constant-folding
            final SplittableRandom rnd = new SplittableRandom(42);
            data = IntStream.generate(rnd::nextInt)
                    .limit(streamSize)
                    .toArray();
        }
    }

    @Benchmark
    public void lastGatherer_toList(final DataState s, final Blackhole bh) {
        final List<Integer> out = IntStream.of(s.data)
                .boxed()
                .gather(Gatherers4j.takeLast(s.lastCount))
                .toList();
        bh.consume(out);
    }
}
