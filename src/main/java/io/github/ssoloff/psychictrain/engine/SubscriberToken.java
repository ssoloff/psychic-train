package io.github.ssoloff.psychictrain.engine;

import org.eclipse.jdt.annotation.NonNull;

public interface SubscriberToken<@NonNull S extends Subscriber> {
  S getSubscriber();

  void unregister();
}
