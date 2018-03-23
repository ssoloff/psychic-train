package io.github.ssoloff.psychictrain.engine;

import org.eclipse.jdt.annotation.NonNull;

@FunctionalInterface
public interface SubscriberFactory<@NonNull S extends Subscriber> {
  S newSubscriber(SubscriberContext subscriberContext);
}
