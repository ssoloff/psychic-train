package io.github.ssoloff.psychictrain.internal.engine;

import javax.annotation.concurrent.Immutable;

@Immutable
final class PublisherId {
  private PublisherId() {
  }

  static PublisherId newInstance() {
    return new PublisherId();
  }
}
