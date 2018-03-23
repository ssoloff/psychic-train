package io.github.ssoloff.psychictrain.engine.impl;

import javax.annotation.concurrent.Immutable;

import org.eclipse.jdt.annotation.NonNull;

import io.github.ssoloff.psychictrain.engine.Publisher;
import io.github.ssoloff.psychictrain.engine.PublisherToken;

@Immutable
final class DefaultPublisherToken<@NonNull P extends Publisher> implements PublisherToken<P> {
  private final DefaultBroker broker;
  private final P publisher;
  private final PublisherId publisherId;

  DefaultPublisherToken(final DefaultBroker broker, final PublisherId publisherId, final P publisher) {
    this.broker = broker;
    this.publisher = publisher;
    this.publisherId = publisherId;
  }

  @Override
  public P getPublisher() {
    return publisher;
  }

  @Override
  public void unregister() {
    broker.unregisterPublisher(publisherId);
  }
}
