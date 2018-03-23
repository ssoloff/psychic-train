package io.github.ssoloff.psychictrain.api.engine;

import org.eclipse.jdt.annotation.NonNull;

public interface SubscriberToken<@NonNull S extends Subscriber> {
  S getSubscriber();

  void unregister();
}
