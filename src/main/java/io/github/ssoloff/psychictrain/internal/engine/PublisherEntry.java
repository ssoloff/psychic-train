package io.github.ssoloff.psychictrain.internal.engine;

import org.eclipse.jdt.annotation.NonNull;

import io.github.ssoloff.psychictrain.api.engine.Topic;

final class PublisherEntry {
  private static final Object NO_VALUE = new Object();

  private final Topic<?> topic;
  private Object value = NO_VALUE;

  PublisherEntry(final Topic<?> topic) {
    this.topic = topic;
  }

  Topic<?> getTopic() {
    return topic;
  }

  <@NonNull T> T getValue() {
    assert value != NO_VALUE;

    @SuppressWarnings("unchecked")
    final T typedValue = (T) value;
    return typedValue;
  }

  boolean hasValue() {
    return value != NO_VALUE;
  }

  boolean matches(final Topic<?> otherTopic) {
    return this.topic.equals(otherTopic);
  }

  void setValue(final Object value) {
    topic.getTypeToken().getRawType().cast(value);
    this.value = value;
  }
}
