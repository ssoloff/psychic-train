package io.github.ssoloff.psychictrain.internal.engine;

import javax.annotation.concurrent.Immutable;

@Immutable
final class SubscriberId {
  private SubscriberId() {
  }

  static SubscriberId newInstance() {
    return new SubscriberId();
  }
}
