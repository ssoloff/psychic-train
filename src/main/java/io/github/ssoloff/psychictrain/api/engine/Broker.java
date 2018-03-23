package io.github.ssoloff.psychictrain.api.engine;

import org.eclipse.jdt.annotation.NonNull;

public interface Broker {
  <@NonNull P extends Publisher> PublisherToken<P> registerPublisher(
      Topic<?> topic,
      PublisherFactory<P> publisherFactory);

  <@NonNull S extends Subscriber> SubscriberToken<S> registerSubscriber(
      TopicMatcher topicMatcher,
      SubscriberFactory<S> subscriberFactory);
}
