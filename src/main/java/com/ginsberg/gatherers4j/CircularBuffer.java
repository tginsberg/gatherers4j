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

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class CircularBuffer<T extends @Nullable Object> implements Iterable<T>{
    private final T[] buffer;
    private int size = 0;
    private int head = 0;
    private int tail = 0;

    @SuppressWarnings("unchecked")
    public CircularBuffer(int capacity) {
        buffer = (T[]) new Object[capacity];
    }

    public void add(T element) {
        buffer[tail] = element;
        tail = (tail + 1) % buffer.length;

        if (size < buffer.length) {
            size++;
        } else {
            head = (head + 1) % buffer.length;
        }
    }

    public T removeFirst() {
        if (size == 0) {
            throw new NoSuchElementException();
        }

        final T element = buffer[head];
        head = (head + 1) % buffer.length;
        size--;
        return element;
    }

    public void removeFirst(final int n) {
        if (n <= 0) {
            return;
        }
        if (n >= size) {
            head = tail = 0;
            size = 0;
            return;
        }

        head = (head + n) % buffer.length;
        size -= n;
    }

    public List<T> asList() {
        @SuppressWarnings("unchecked")
        T[] result = (T[]) new Object[size];

        if (head + size <= buffer.length) {
            // Elements are contiguous
            System.arraycopy(buffer, head, result, 0, size);
        } else {
            int firstPart = buffer.length - head;
            System.arraycopy(buffer, head, result, 0, firstPart);
            System.arraycopy(buffer, 0, result, firstPart, size - firstPart);
        }

        //noinspection Java9CollectionFactory
        return Collections.unmodifiableList(Arrays.asList(result));
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            private int index = 0;
            private int remaining = size;

            @Override
            public boolean hasNext() {
                return remaining > 0;
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }

                final T element = buffer[(head + index++) % buffer.length];
                //index++;
                remaining--;
                return element;
            }
        };
    }
}