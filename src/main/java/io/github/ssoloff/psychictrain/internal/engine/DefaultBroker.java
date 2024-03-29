package io.github.ssoloff.psychictrain.internal.engine;

import static com.google.common.base.Preconditions.checkState;

import java.util.Collection;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

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

  private final Set<Topic<?>> inFlightTopics = new HashSet<>();
  private final Map<PublisherId, PublisherEntry> publisherEntriesById = new IdentityHashMap<>();
  private final Map<SubscriberId, SubscriberEntry> subscriberEntriesById = new IdentityHashMap<>();

  private Multimap<Topic<?>, ?> getValuesForMatchingTopics(final SubscriberId subscriberId) {
    final ImmutableMultimap.Builder<Topic<?>, Object> valuesByTopicBuilder = ImmutableMultimap.builder();
    Optional.ofNullable(subscriberEntriesById.get(subscriberId)).ifPresentOrElse(
        subscriberEntry -> publisherEntriesById.values().stream()
            .filter(publisherEntry -> subscriberEntry.matches(publisherEntry.getTopic()))
            .filter(PublisherEntry::hasValue)
            .forEach(publisherEntry -> valuesByTopicBuilder.put(publisherEntry.getTopic(), publisherEntry.getValue())),
        () -> logger.warning("attempt to retrieve values by unregistered subscriber (" + subscriberId + ")"));
    return valuesByTopicBuilder.build();
  }

  private <@NonNull T> Collection<T> getValuesForTopic(final Topic<T> topic) {
    return ImmutableList.copyOf(publisherEntriesById.values().stream()
        .filter(publisherEntry -> publisherEntry.matches(topic))
        .filter(PublisherEntry::hasValue)
        .map(PublisherEntry::<T>getValue)
        .collect(Collectors.toList()));
  }

  private SubscriberContext newSubscriberContext(final SubscriberId subscriberId) {
    return new SubscriberContext() {
      @Override
      public Multimap<Topic<?>, ?> getValuesForMatchingTopics() {
        return DefaultBroker.this.getValuesForMatchingTopics(subscriberId);
      }

      @Override
      public <@NonNull T> Collection<T> getValuesForTopic(final Topic<T> topic) {
        return DefaultBroker.this.getValuesForTopic(topic);
      }
    };
  }

  private void notifySubscriberForAllMatchingTopics(final SubscriberEntry subscriberEntry) {
    final Set<Topic<?>> topics = ImmutableSet.copyOf(publisherEntriesById.values().stream()
        .filter(publisherEntry -> subscriberEntry.matches(publisherEntry.getTopic()))
        .map(PublisherEntry::getTopic)
        .collect(Collectors.toSet()));
    if (!topics.isEmpty()) {
      subscriberEntry.notifySubscriberTopicsChanged(topics);
    }
  }

  private void notifySubscribersForTopic(final Topic<?> topic) {
    final Set<Topic<?>> topics = ImmutableSet.of(topic);
    subscriberEntriesById.values().stream()
        .filter(subscriberEntry -> subscriberEntry.matches(topic))
        .forEach(subscriberEntry -> subscriberEntry.notifySubscriberTopicsChanged(topics));
  }

  void publish(final PublisherId publisherId, final Object value) {
    Optional.ofNullable(publisherEntriesById.get(publisherId)).ifPresentOrElse(
        publisherEntry -> publish(publisherEntry, value),
        () -> logger.warning("attempt to publish value by unregistered publisher (" + publisherId + ")"));
  }

  private void publish(final PublisherEntry publisherEntry, final Object value) {
    final Topic<?> topic = publisherEntry.getTopic();
    checkState(!inFlightTopics.contains(topic), "cycle detected during publication of topic '" + topic + "'");

    inFlightTopics.add(topic);
    publisherEntry.setValue(value);
    notifySubscribersForTopic(topic);
    inFlightTopics.remove(topic);
  }

  @Override
  public <@NonNull P extends Publisher> PublisherToken<P> registerPublisher(
      final Topic<?> topic,
      final PublisherFactory<P> publisherFactory) {
    final PublisherId publisherId = PublisherId.newInstance();
    final P publisher = publisherFactory.newPublisher(value -> publish(publisherId, value));
    publisherEntriesById.put(publisherId, new PublisherEntry(topic));
    return new DefaultPublisherToken<>(this, publisherId, publisher);
  }

  @Override
  public <@NonNull S extends Subscriber> SubscriberToken<S> registerSubscriber(
      final TopicMatcher topicMatcher,
      final SubscriberFactory<S> subscriberFactory) {
    final SubscriberId subscriberId = SubscriberId.newInstance();
    final S subscriber = subscriberFactory.newSubscriber(newSubscriberContext(subscriberId));
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
        publisherEntry -> notifySubscribersForTopic(publisherEntry.getTopic()),
        () -> logger.warning("attempt to unregister unregistered publisher (" + publisherId + ")"));
  }

  void unregisterSubscriber(final SubscriberId subscriberId) {
    if (!Optional.ofNullable(subscriberEntriesById.remove(subscriberId)).isPresent()) {
      logger.warning("attempt to unregister unregistered subscriber (" + subscriberId + ")");
    }
  }
}
