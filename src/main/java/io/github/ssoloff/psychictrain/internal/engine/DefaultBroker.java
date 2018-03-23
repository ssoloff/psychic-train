package io.github.ssoloff.psychictrain.internal.engine;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import javax.annotation.concurrent.Immutable;

import org.eclipse.jdt.annotation.NonNull;

import io.github.ssoloff.psychictrain.api.engine.Broker;
import io.github.ssoloff.psychictrain.api.engine.Publisher;
import io.github.ssoloff.psychictrain.api.engine.PublisherFactory;
import io.github.ssoloff.psychictrain.api.engine.PublisherToken;
import io.github.ssoloff.psychictrain.api.engine.Subscriber;
import io.github.ssoloff.psychictrain.api.engine.SubscriberContext;
import io.github.ssoloff.psychictrain.api.engine.SubscriberFactory;
import io.github.ssoloff.psychictrain.api.engine.SubscriberToken;
import io.github.ssoloff.psychictrain.api.engine.Topic;
import io.github.ssoloff.psychictrain.api.engine.TopicMatcher;

final class DefaultBroker implements Broker {
  private static final Logger logger = Logger.getLogger(DefaultBroker.class.getName());

  private final Map<PublisherId, PublisherEntry> publisherEntriesById = new IdentityHashMap<>();
  private final Map<SubscriberId, SubscriberEntry> subscriberEntriesById = new IdentityHashMap<>();

  private void notifySubscriberForAllMatchingTopics(final SubscriberEntry subscriberEntry) {
    publisherEntriesById.values().stream()
        .filter(publisherEntry -> subscriberEntry.topicMatcher.matches(publisherEntry.topic))
        .forEach(publisherEntry -> subscriberEntry.subscriber.topicChanged(publisherEntry.topic));
  }

  private void notifySubscribersForTopic(final Topic<?> topic) {
    subscriberEntriesById.values().stream()
        .filter(subscriberEntry -> subscriberEntry.topicMatcher.matches(topic))
        .forEach(subscriberEntry -> subscriberEntry.subscriber.topicChanged(topic));
  }

  void publish(final PublisherId publisherId, final Object value) {
    // FIXME: need to be able to detect cycles and abort them
    Optional.ofNullable(publisherEntriesById.get(publisherId)).ifPresentOrElse(
        publisherEntry -> notifySubscribersForTopic(publisherEntry.topic),
        () -> logger.warning("attempt to publish value by unregistered publisher (" + publisherId + ")"));
  }

  @Override
  public <@NonNull P extends Publisher> PublisherToken<P> registerPublisher(
      final Topic<?> topic,
      final PublisherFactory<P> publisherFactory) {
    final PublisherId publisherId = PublisherId.newInstance();
    final P publisher = publisherFactory.newPublisher(value -> publish(publisherId, value));
    publisherEntriesById.put(publisherId, new PublisherEntry(topic));
    // TODO: this will eventually be removed because the VALUES have not
    // actually changed because the publisher has not yet published.
    notifySubscribersForTopic(topic);
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
    final SubscriberEntry subscriberEntry = new SubscriberEntry(subscriber, topicMatcher);
    subscriberEntriesById.put(subscriberId, subscriberEntry);
    // TODO: requires further investigation... we're firing an event before
    // the caller has had a chance to do anything with the token. that may
    // prevent the ultimate destination from receiving the event...?
    notifySubscriberForAllMatchingTopics(subscriberEntry);
    return new DefaultSubscriberToken<>(this, subscriberId, subscriber);
  }

  void unregisterPublisher(final PublisherId publisherId) {
    Optional.ofNullable(publisherEntriesById.remove(publisherId)).ifPresentOrElse(
        publisherEntry -> notifySubscribersForTopic(publisherEntry.topic),
        () -> logger.warning("attempt to unregister unregistered publisher (" + publisherId + ")"));
  }

  void unregisterSubscriber(final SubscriberId subscriberId) {
    if (!Optional.ofNullable(subscriberEntriesById.remove(subscriberId)).isPresent()) {
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
