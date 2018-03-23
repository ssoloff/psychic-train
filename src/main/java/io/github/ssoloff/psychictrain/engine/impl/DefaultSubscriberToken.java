package io.github.ssoloff.psychictrain.engine.impl;

import javax.annotation.concurrent.Immutable;

import org.eclipse.jdt.annotation.NonNull;

import io.github.ssoloff.psychictrain.engine.Subscriber;
import io.github.ssoloff.psychictrain.engine.SubscriberToken;

@Immutable
final class DefaultSubscriberToken<@NonNull S extends Subscriber> implements SubscriberToken<S> {
  private final DefaultBroker broker;
  private final S subscriber;
  private final SubscriberId subscriberId;

  DefaultSubscriberToken(final DefaultBroker broker, final SubscriberId subscriberId, final S subscriber) {
    this.broker = broker;
    this.subscriber = subscriber;
    this.subscriberId = subscriberId;
  }

  @Override
  public S getSubscriber() {
    return subscriber;
  }

  @Override
  public void unregister() {
    broker.unregisterSubscriber(subscriberId);
  }
}
