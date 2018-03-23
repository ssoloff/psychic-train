package io.github.ssoloff.psychictrain.engine.impl;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.annotation.concurrent.Immutable;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import io.github.ssoloff.psychictrain.engine.Broker;
import io.github.ssoloff.psychictrain.engine.Publisher;
import io.github.ssoloff.psychictrain.engine.PublisherFactory;
import io.github.ssoloff.psychictrain.engine.PublisherToken;
import io.github.ssoloff.psychictrain.engine.Subscriber;
import io.github.ssoloff.psychictrain.engine.SubscriberContext;
import io.github.ssoloff.psychictrain.engine.SubscriberFactory;
import io.github.ssoloff.psychictrain.engine.SubscriberToken;
import io.github.ssoloff.psychictrain.engine.Topic;
import io.github.ssoloff.psychictrain.engine.TopicMatcher;

final class DefaultBroker implements Broker {
  private static final Logger logger = Logger.getLogger(DefaultBroker.class.getName());

  private final Map<PublisherId, PublisherEntry> publisherEntriesById = new IdentityHashMap<>();
  private final Map<SubscriberId, SubscriberEntry> subscriberEntriesById = new IdentityHashMap<>();

  private void notifySubscribers(final Topic<?> topic) {
    subscriberEntriesById.values().stream()
        .filter(it -> it.topicMatcher.matches(topic))
        .forEach(it -> it.subscriber.topicChanged(topic));
  }

  void publish(final PublisherId publisherId, final Object value) {
    // TODO: replace with Optional#ifPresentOrElse
    // FIXME: need to be able to detect cycles and abort them
    final @Nullable PublisherEntry publisherEntry = publisherEntriesById.get(publisherId);
    if (publisherEntry == null) {
      logger.warning("attempt to publish value by unregistered publisher (" + publisherId + ")");
      return;
    }

    notifySubscribers(publisherEntry.topic);
  }

  @Override
  public <@NonNull P extends Publisher> PublisherToken<P> registerPublisher(
      final Topic<?> topic,
      final PublisherFactory<P> publisherFactory) {
    final PublisherId publisherId = PublisherId.newInstance();
    final P publisher = publisherFactory.newPublisher(value -> publish(publisherId, value));
    publisherEntriesById.put(publisherId, new PublisherEntry(topic));
    // TODO: notify subscribers (maybe not because publisher current is not pushing
    // a default value)
    // need to consider how that's going to work
    return new DefaultPublisherToken<>(this, publisherId, publisher);
  }

  @Override
  public <@NonNull S extends Subscriber> SubscriberToken<S> registerSubscriber(
      final TopicMatcher topicMatcher,
      final SubscriberFactory<S> subscriberFactory) {
    final SubscriberId subscriberId = SubscriberId.newInstance();
    final S subscriber = subscriberFactory.newSubscriber(new SubscriberContext() {
      // no methods
    });
    subscriberEntriesById.put(subscriberId, new SubscriberEntry(subscriber, topicMatcher));
    return new DefaultSubscriberToken<>(this, subscriberId, subscriber);
  }

  void unregisterPublisher(final PublisherId publisherId) {
    if (publisherEntriesById.remove(publisherId) == null) {
      logger.warning("attempt to unregister unregistered publisher (" + publisherId + ")");
    }
    // TODO: notify subscribers
  }

  void unregisterSubscriber(final SubscriberId subscriberId) {
    if (subscriberEntriesById.remove(subscriberId) == null) {
      logger.warning("attempt to unregister unregistered subscriber (" + subscriberId + ")");
    }
  }

  @Immutable
  private static final class PublisherEntry {
    final Topic<?> topic;

    PublisherEntry(final Topic<?> topic) {
      this.topic = topic;
    }
  }

  @Immutable
  private static final class SubscriberEntry {
    final Subscriber subscriber;
    final TopicMatcher topicMatcher;

    SubscriberEntry(final Subscriber subscriber, final TopicMatcher topicMatcher) {
      this.subscriber = subscriber;
      this.topicMatcher = topicMatcher;
    }
  }
}