package com.ginsberg.gatherers4j.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CircularBufferTest {

    @Test
    void add() {
        // Arrange
        final CircularBuffer<String> cb = new CircularBuffer<>(2);

        // Act
        Stream.of("A", "B").forEach(cb::add);

        // Assert
        assertThat(cb.asList()).containsExactly("A", "B");
    }

    @Test
    void addOverwritesFirst() {
        // Arrange
        final CircularBuffer<String> cb = new CircularBuffer<>(2);

        // Act
        Stream.of("A", "B", "C").forEach(cb::add);

        // Assert
        assertThat(cb.asList()).containsExactly("B", "C");
    }

    @Test
    void addReturnsNullWhenNotOverwriting() {
        // Arrange
        final CircularBuffer<String> cb = new CircularBuffer<>(2);

        // Act
        final List<String> output = Stream.of("A", "B").map(cb::add).toList();

        // Assert
        assertThat(output).containsExactly(null, null);
    }

    @Test
    void addReturnsPreviousWhenOverwriting() {
        // Arrange
        final CircularBuffer<String> cb = new CircularBuffer<>(1);

        // Act
        final List<String> output = Stream.of("A", "B", "C").map(cb::add).toList();

        // Assert
        assertThat(output).containsExactly(null, "A", "B");
    }

    @Test
    void asList() {
        // Arrange
        final CircularBuffer<String> cb = new CircularBuffer<>(5);
        Stream.of("A", "B", "C", "D").forEach(cb::add);

        // Act
        final List<String> output = cb.asList();

        // Assert
        assertThat(output).containsExactly("A", "B", "C", "D");
    }

    @Test
    void asListEmpty() {
        // Arrange
        final CircularBuffer<String> cb = new CircularBuffer<>(5);

        // Act
        final List<String> output = cb.asList();

        // Assert
        assertThat(output).isEmpty();
    }

    @Test
    void createdEmpty() {
        // Arrange
        final CircularBuffer<String> cb = new CircularBuffer<>(2);

        // Assert
        assertThat(cb.isEmpty()).isTrue();
        assertThat(cb.size()).isEqualTo(0);
    }

    @Test
    void drop() {
        // Arrange
        final CircularBuffer<String> cb = new CircularBuffer<>(5);
        Stream.of("A", "B", "C", "D").forEach(cb::add);

        // Act
        cb.drop(3);

        // Assert
        assertThat(cb.size()).isEqualTo(1);
        assertThat(cb.asList()).containsExactly("D");
    }

    @Test
    void dropMoreThanSize() {
        // Arrange
        final CircularBuffer<String> cb = new CircularBuffer<>(5);
        Stream.of("A", "B", "C", "D").forEach(cb::add);

        // Act
        cb.drop(6);

        // Assert
        assertThat(cb.size()).isEqualTo(0);
        assertThat(cb.isEmpty()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0})
    void dropWithoutEffect(final int drops) {
        // Arrange
        final CircularBuffer<String> cb = new CircularBuffer<>(2);
        Stream.of("A", "B").forEach(cb::add);

        // Act
        cb.drop(drops);

        // Assert
        assertThat(cb.asList()).containsExactly("A", "B");
    }

    @Test
    void iterator() {
        // Arrange
        final CircularBuffer<String> cb = new CircularBuffer<>(5);
        Stream.of("A", "B", "C", "D").forEach(cb::add);

        // Act
        final Iterator<String> iterator = cb.iterator();

        // Assert
        assertThat(iterator).toIterable().containsExactly("A", "B", "C", "D");
    }

    @Test
    void iteratorDoesNotSupportRemove() {
        assertThatThrownBy(() -> {
                    final CircularBuffer<String> cb = new CircularBuffer<>(5);
                    Stream.of("A", "B", "C", "D").forEach(cb::add);
                    cb.iterator().remove();
                }
        ).isExactlyInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void iteratorEmpty() {
        // Arrange
        final CircularBuffer<String> cb = new CircularBuffer<>(5);

        // Act
        final Iterator<String> iterator = cb.iterator();

        // Assert
        assertThat(iterator).toIterable().isEmpty();
    }

    @Test
    void iteratorEmptyWhenNext() {
        assertThatThrownBy(() ->
                new CircularBuffer<>(1).iterator().next()
        ).isExactlyInstanceOf(NoSuchElementException.class);
    }

    @Test
    void removeFirst() {
        // Arrange
        final CircularBuffer<String> cb = new CircularBuffer<>(2);
        Stream.of("A", "B").forEach(cb::add);

        // Act
        final String removed = cb.removeFirst();

        // Assert
        assertThat(removed).isEqualTo("A");
        assertThat(cb.size()).isEqualTo(1);
    }

    @Test
    void removeFirstWhenEmpty() {
        assertThatThrownBy(() ->
                new CircularBuffer<>(1).removeFirst()
        ).isExactlyInstanceOf(NoSuchElementException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0})
    void sizeMustBeGreaterThanZero(final int value) {
        assertThatThrownBy(() ->
                new CircularBuffer<>(value)
        ).isExactlyInstanceOf(IllegalArgumentException.class);
    }
}