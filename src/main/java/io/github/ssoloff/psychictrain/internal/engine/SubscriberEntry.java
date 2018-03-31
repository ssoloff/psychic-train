package io.github.ssoloff.psychictrain.internal.engine;

import java.util.Set;

import javax.annotation.concurrent.Immutable;

import io.github.ssoloff.psychictrain.api.engine.Subscriber;
import io.github.ssoloff.psychictrain.api.engine.Topic;
import io.github.ssoloff.psychictrain.api.engine.TopicMatcher;

@Immutable
final class SubscriberEntry {
  private final Subscriber subscriber;
  private final TopicMatcher topicMatcher;

  SubscriberEntry(final Subscriber subscriber, final TopicMatcher topicMatcher) {
    this.subscriber = subscriber;
    this.topicMatcher = topicMatcher;
  }

  boolean matches(final Topic<?> topic) {
    return topicMatcher.matches(topic);
  }

  void notifySubscriberTopicsChanged(final Set<Topic<?>> topics) {
    subscriber.topicsChanged(topics);
  }
}
